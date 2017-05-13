package edu.dal.corr;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

import edu.dal.corr.suggest.NgramBoundedReaderSearcher;
import edu.dal.corr.util.PathUtils;
import edu.dal.corr.util.ResourceUtils;
import edu.dal.corr.word.Context;
import edu.dal.corr.word.Word;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TObjectLongHashMap;

public class CandidateGeneration {
  private static Path errorPath = ResourceUtils.getPath("data/error.gt.tsv");
  private static Path tokenPath = ResourceUtils.getPath("data/ocr.token.tsv");
  
  /*
   * Constructs words from data. Note that:
   * -  Line-broken tokens are fixed when used in context.
   * -  Insertion errors, which error names are an empty strings, are filtered
   *    from the final errors.
   */
  private static List<Word> constructWords() throws IOException {
    // construct a mapping from token starting positions to the according tokens
    TIntIntHashMap posTidxMap = new TIntIntHashMap();
    List<String> tokens = new ArrayList<String>();
    try (BufferedReader br = Files.newBufferedReader(tokenPath)) {
      for (String line = br.readLine(); line != null; line = br.readLine()) {
        if (line.length() > 0) {
          String[] splits = line.split("\t");
          String word = splits[0];
          if (word.contains("-↵")) { // fix line-broken word
            System.err.println("\t" + word + " -> " + (word = word.replace("-↵", "")));
          }
          posTidxMap.put(Integer.parseInt(splits[1]), tokens.size());
          tokens.add(word);
        }
      }
    }
    // construct Word
    List<Word> words = new ArrayList<>();
    try (BufferedReader br = Files.newBufferedReader(errorPath)) {
      for (String line = br.readLine(); line != null; line = br.readLine()) {
        String[] splits = line.split("\t");
        if (splits[1].length() == 0) continue; // omit insertion errors
        int pos = Integer.parseInt(splits[0]);
        String token = tokens.get(posTidxMap.get(pos));
        if (! token.equals(splits[1])) {
          throw new RuntimeException("Token unmatch at position " + splits[0] + ": "
              + token + ", " + splits[1]);
        }
        int tidx = posTidxMap.get(pos);
        words.add(new Word(pos, 
            tokens.get(tidx - 4),
            tokens.get(tidx - 3),
            tokens.get(tidx - 2),
            tokens.get(tidx - 1),
            tokens.get(tidx),
            tokens.get(tidx + 1),
            tokens.get(tidx + 2),
            tokens.get(tidx + 3)
            ));
      }
    }
    System.out.println("Word construction done!");
    return words;
  }

  private static NgramBoundedReaderSearcher getNgramSearch(String pathname, List<Path> dataPath) {
    try {
      NgramBoundedReaderSearcher ngramSearch =
          NgramBoundedReaderSearcher.read(PathUtils.TEMP_DIR.resolve(Paths.get(pathname)));
      ngramSearch.setNgramPath(dataPath);
      System.out.println("Ngram reader load done!");
      return ngramSearch;
    } catch (IOException e) {
      throw new RuntimeException(
          String.format("Cannot open %s in building NgramBoundedReaderSearcher object.", pathname));
    }
  }

   private static String subExactWord(String ngram, Context context) {
    String[] grams = ngram.split(" ");
    // Detect whether this ngram contains a word substitution.
    String sub = grams[context.index()];
    for (int i = 0; i < ngram.length(); i++) {
      if (context.index() != i && !context.words()[i].equals(grams[i])) { // not a valid substitution
        sub = null;
        break;
      }
    }
    return sub;
  }

  private static String subApproxWord(String ngram, Context context) {
    String[] grams = ngram.split(" ");
    // Detect whether this ngram contains a word substitution.
    String sub = grams[context.index()];
    boolean unmatch = false;
    for (int i = 0; i < ngram.length(); i++) {
      if (context.index() != i && !context.words()[i].equals(grams[i])) { // context gram unmatch
        if (unmatch) { // already have an unmatching gram
          sub = null;
          break;
        }
        unmatch = true;
      }
    }
    return sub;
  }

  
  /*
   * Find candidates with according frequencies.
   */
  @SuppressWarnings("unchecked")
  private static TObjectLongHashMap<String>[] findCandidates(
      Word word, NgramBoundedReaderSearcher reader, int ngramSize) {
    TObjectLongHashMap<String> exactFreqMap = new TObjectLongHashMap<>();
    TObjectLongHashMap<String> approxFreqMap = new TObjectLongHashMap<>();
    List<Context> contexts = word.getContexts(ngramSize);
    for (Context c: contexts) {
      try (BufferedReader br = reader.openBufferedRecordsWithFirstWord(c.words()[0])) {
        if (br != null) {
          for (String line = br.readLine(); line != null; line = br.readLine()) {
            String[] splits = line.split("\t");
            { // exact contexts
              String sub = subExactWord(splits[0], c);
              if (sub != null) {
                long val = Long.parseLong(splits[1]);
                exactFreqMap.adjustOrPutValue(sub, val, val);
              }
            }
            if (ngramSize > 2) { // approximate contexts
              String sub = subApproxWord(splits[0], c);
              if (sub != null) {
                long val = Long.parseLong(splits[1]);
                approxFreqMap.adjustOrPutValue(sub, val, val);
              }
            }
          }
        }
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
    return new TObjectLongHashMap[]{ exactFreqMap, approxFreqMap };
  }
  
  /*
   * Write to error candidates to file. Candidates are sorted according to the frequency.
   */
  private static void writeCandidates(
      Path path, Word word, TObjectLongHashMap<String> candFreq) throws IOException {
    try (BufferedWriter bw = Files.newBufferedWriter(path, StandardOpenOption.CREATE)) {
      StringBuilder sb = new StringBuilder();
      candFreq.keySet()
              .stream()
              .sorted((c1, c2) -> (int)(candFreq.get(c2) - candFreq.get(c1)))
              .forEachOrdered(c -> {
                sb.append(c).append('\t');
                sb.append(candFreq.get(c)).append('\n');
              });
      bw.write(sb.toString());
    }
  }
  
  /*
   * Search and write exact candidates to files. Performed in parallel.
   */
  private static void searchAndWriteCandidates(
      Path exactOutFolder, Path approxOutFolder, List<Word> words, 
      NgramBoundedReaderSearcher reader, int ngramSize)
      throws IOException {
    Files.createDirectories(exactOutFolder);
    Files.createDirectories(approxOutFolder);
    words.parallelStream()
         .forEach(w -> {
           try {
            TObjectLongHashMap<String>[] maps = findCandidates(w, reader, ngramSize);
            writeCandidates(exactOutFolder.resolve("error." + w.position() + ".tsv"), w, maps[0]);
            writeCandidates(approxOutFolder.resolve("error." + w.position() + ".tsv"), w, maps[1]);
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
         });
  }

  public static void main(String[] args) throws IOException {
    List<Word> words = constructWords();
    searchAndWriteCandidates(Paths.get("candidate.2gm.exact"), Paths.get("candidate.2gm.approx"), words,
        getNgramSearch("2gm.search", ResourceUtils.BIGRAM), 2);
    searchAndWriteCandidates(Paths.get("candidate.3gm.exact"), Paths.get("candidate.3gm.approx"), words,
        getNgramSearch("3gm.search", ResourceUtils.TRIGRAM), 3);
    searchAndWriteCandidates(Paths.get("candidate.4gm.exact"), Paths.get("candidate.4gm.approx"), words,
        getNgramSearch("4gm.search", ResourceUtils.FOURGRAM), 4);
    searchAndWriteCandidates(Paths.get("candidate.5gm.exact"), Paths.get("candidate.5gm.approx"), words,
        getNgramSearch("5gm.search", ResourceUtils.FIVEGRAM), 5);
  }
}
