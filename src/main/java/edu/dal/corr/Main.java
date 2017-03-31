package edu.dal.corr;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import edu.dal.corr.eval.GroundTruthError;
import edu.dal.corr.eval.GroundTruthErrors;
import edu.dal.corr.suggest.Candidate;
import edu.dal.corr.suggest.NgramBoundedReaderSearcher;
import edu.dal.corr.suggest.Scoreable;
import edu.dal.corr.suggest.Suggestion;
import edu.dal.corr.suggest.feature.ContextCoherenceFeature;
import edu.dal.corr.suggest.feature.DistanceFeature;
import edu.dal.corr.suggest.feature.Feature;
import edu.dal.corr.suggest.feature.LanguagePopularityFeature;
import edu.dal.corr.suggest.feature.LexiconExistenceFeature;
import edu.dal.corr.suggest.feature.ApproximateContextCoherenceFeature;
import edu.dal.corr.suggest.feature.StringSimilarityFeature;
import edu.dal.corr.util.PathUtils;
import edu.dal.corr.util.IOUtils;
import edu.dal.corr.util.ResourceUtils;
import edu.dal.corr.util.Unigram;
import edu.dal.corr.word.GoogleTokenizer;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

/**
 * @since 2017.03.30
 */
public class Main
{
  private static final int TOP = 100;

  public static NgramBoundedReaderSearcher getNgramSearch(
      String pathname,
      List<Path> dataPath)
  {
    try {
      NgramBoundedReaderSearcher ngramSearch = NgramBoundedReaderSearcher.read(
          PathUtils.TEMP_DIR.resolve(Paths.get(pathname)));
      ngramSearch.setNgramPath(dataPath);
      return ngramSearch;
    } catch (IOException e) {
      throw new RuntimeException(String.format(
          "Cannot open %s in building NgramBoundedReaderSearcher object.",
          pathname));
    }
  }

  public static void runCorrection() throws IOException {
    // Construct unigram.
    Unigram unigram = Unigram.getInstance();

    // Read pre-processed 5-gram search indexing.
    NgramBoundedReaderSearcher bigram   = getNgramSearch("2gm.search", ResourceUtils.BIGRAM);
    NgramBoundedReaderSearcher trigram  = getNgramSearch("3gm.search", ResourceUtils.TRIGRAM);
    NgramBoundedReaderSearcher fourgram = getNgramSearch("4gm.search", ResourceUtils.FOURGRAM);
    NgramBoundedReaderSearcher fivegram = getNgramSearch("5gm.search", ResourceUtils.FIVEGRAM);

    // Construct features.
    Feature[] features = new Feature[]{
            new DistanceFeature("Levenstein", Scoreable.levenshteinDist()),
            new DistanceFeature("Damerau-Levnstein", Scoreable.damerauLevDist()),
            new DistanceFeature("LCS", Scoreable.lscDist()),
            new DistanceFeature("Optimal-StringAlign", Scoreable.optStrAlignDist()),
            new DistanceFeature("Jaro-Winkler", Scoreable.jaroWinklerDist()),

            new DistanceFeature("ngram-Jaccard-unigram", Scoreable.jaccardDist(1)),
            new DistanceFeature("ngram-Jaccard-bigram", Scoreable.jaccardDist(2)),
            new DistanceFeature("ngram-Jaccard-trigram", Scoreable.jaccardDist(3)),
            new DistanceFeature("ngram-Jaccard-fourgram", Scoreable.jaccardDist(4)),
            new DistanceFeature("ngram-Jaccard-fivegram", Scoreable.jaccardDist(5)),

            new DistanceFeature("qgram-bigram", Scoreable.qgramDist(2)),
            new DistanceFeature("qgram-trigram", Scoreable.qgramDist(3)),
            new DistanceFeature("qgram-fourgram", Scoreable.qgramDist(4)),
            new DistanceFeature("qgram-fivegram", Scoreable.qgramDist(5)),

            new DistanceFeature("ngram-binary-bigram", Scoreable.binNgramDist(2)),
            new DistanceFeature("ngram-binary-trigram", Scoreable.binNgramDist(3)),
            new DistanceFeature("ngram-binary-fourgram", Scoreable.binNgramDist(4)),
            new DistanceFeature("ngram-binary-fivegram", Scoreable.binNgramDist(5)),
            new DistanceFeature("ngram-positional-bigram", Scoreable.posNgramDist(2)),
            new DistanceFeature("ngram-positional-trigram", Scoreable.posNgramDist(3)),
            new DistanceFeature("ngram-positional-fourgram", Scoreable.posNgramDist(4)),
            new DistanceFeature("ngram-positional-fivegram", Scoreable.posNgramDist(5)),
            new DistanceFeature("ngram-comprehensive-bigram", Scoreable.cmphNgramDist(2)),
            new DistanceFeature("ngram-comprehensive-trigram", Scoreable.cmphNgramDist(3)),
            new DistanceFeature("ngram-comprehensive-fourgram", Scoreable.cmphNgramDist(4)),
            new DistanceFeature("ngram-comprehensive-fivegram", Scoreable.cmphNgramDist(5)),

            new StringSimilarityFeature(),
            new LanguagePopularityFeature(unigram),

            new LexiconExistenceFeature("Lexical", IOUtils.readList(ResourceUtils.LEXI_LEXICON)),
            new LexiconExistenceFeature("Special", IOUtils.readList(ResourceUtils.SPECIAL_LEXICON)),
            new LexiconExistenceFeature("Wikipedia", IOUtils.readList(ResourceUtils.WIKI_LEXICON)),

            new ContextCoherenceFeature("Bigram", bigram, 2),
            new ContextCoherenceFeature("Trigram", trigram, 3),
            new ContextCoherenceFeature("Fourgram", fourgram, 4),
            new ContextCoherenceFeature("Fivegram", fivegram, 5),

            new ApproximateContextCoherenceFeature("Bigram", bigram, 2),
            new ApproximateContextCoherenceFeature("Trigram", trigram, 3),
            new ApproximateContextCoherenceFeature("Fourgram", fourgram, 4),
            new ApproximateContextCoherenceFeature("Fivegram", fivegram, 5),
        };

    // Generate suggestions.
    new DocumentCorrector().correct(
        new GoogleTokenizer(),
        null,
        Arrays.asList(features),
        IOUtils.read(ResourceUtils.INPUT),
        TOP
    );
  }
  
  private static List<Integer> TOP_VALS = Arrays.asList(3, 5, 10, 20, 50, 100);
  
  public static void runWriteText(int top) throws IOException {
    if (! TOP_VALS.contains(top)) {
      throw new RuntimeException();
    }
    List<GroundTruthError> errors = GroundTruthErrors.read(Paths.get("data/error.gt.tsv"));
    List<Path> files = ResourceUtils
        .getPathsInDir("suggestion.part***.top." + top, "tmp")
        .stream()
        .map(b -> ResourceUtils.getPathsInDir("suggest.*", b.toString()))
        .flatMap(Collection::stream)
        .collect(Collectors.toList());
    List<Suggestion> suggests = files
        .parallelStream()
        .map(Suggestion::read)
        .collect(Collectors.toList());
    Suggestion.writeText(suggests, errors, Paths.get("tmp/suggestion.top." + top));
  }
  
  private static boolean correctSuggested(Suggestion suggest, TIntObjectMap<GroundTruthError> errMap,
        BitSet errPos) {
    // Check whether the suggested word overlaps any error.
    boolean overlap = false;
    for (int i = suggest.position(); i < suggest.position() + suggest.text().length(); i++) {
      if (errPos.get(i)) {
        overlap = true;
        break;
      }
    }
    // Setup the target word according the overlapping status.
    int pos = suggest.position();
    String word = suggest.text();
    List<String> targets = null;
    if (overlap) {
      GroundTruthError err = errMap.get(pos);
      if (err != null) {
        targets = Arrays.asList(err.gtText(), err.gtTextAscii());
      }
    } else {
      targets = Arrays.asList(word);
    }

    // Check if any candidates matches the target word.
    if (targets == null) {
      return false;
    }
    for (Candidate cand : suggest.candidates()) {
      if (targets.contains(cand.text())) {
        return true;
      }
    }
    return false;
  }
  
  public static void evaluateSuggestRecall(int top) throws IOException {
    // Map each error starting position to the according error.
    List<GroundTruthError> errors = GroundTruthErrors.read(Paths.get("data/error.gt.tsv"));
    TIntObjectMap<GroundTruthError> errMap = new TIntObjectHashMap<>();
    for (GroundTruthError err : errors) {
      errMap.put(err.position(), err);
    }

    // Representing error location using a bit set.
    BitSet errPos = new BitSet(IOUtils.read(ResourceUtils.INPUT).length());
    for (GroundTruthError err : errors) {
      errPos.set(err.position(), err.position() + err.text().length(), true);
    }
    
    // Detect whether a correction contains in the suggestion, or the original
    // word in the suggestion for a correction word.
    List<Path> files = ResourceUtils
        .getPathsInDir("suggestion.part***.top." + top, "tmp")
        .stream()
        .map(b -> ResourceUtils.getPathsInDir("suggest.*", b.toString()))
        .flatMap(Collection::stream)
        .collect(Collectors.toList());
    long numOfCorr = files
        .parallelStream()
        .map(f -> correctSuggested(Suggestion.read(f), errMap, errPos))
        .filter(b -> b)
        .count();
    System.out.println(String.format("Correctly suggested with top %3d: %8d / %8d = %.4f",
        top, numOfCorr, files.size(), (float)numOfCorr / files.size()));
  }

  public static void evaluateSuggestRecall(int top, String folder) throws IOException {
    // Map each error starting position to the according error.
    List<GroundTruthError> errors = GroundTruthErrors.read(Paths.get("data/error.gt.tsv"));
    TIntObjectMap<GroundTruthError> errMap = new TIntObjectHashMap<>();
    for (GroundTruthError err : errors) {
      errMap.put(err.position(), err);
    }

    // Representing error location using a bit set.
    BitSet errPos = new BitSet(IOUtils.read(ResourceUtils.INPUT).length());
    for (GroundTruthError err : errors) {
      errPos.set(err.position(), err.position() + err.text().length(), true);
    }
    
    // Detect whether a correction contains in the suggestion, or the original
    // word in the suggestion for a correction word.
    List<Path> files = ResourceUtils
        .getPathsInDir("suggestion.top." + top, folder)
        .stream()
        .map(b -> ResourceUtils.getPathsInDir("suggest.*", b.toString()))
        .flatMap(Collection::stream)
        .collect(Collectors.toList());
    long numOfCorr = files
        .parallelStream()
        .map(f -> correctSuggested(Suggestion.read(f), errMap, errPos))
        .filter(b -> b)
        .count();
    System.out.println(String.format("Correctly suggested with top %3d: %8d / %8d = %.4f",
        top, numOfCorr, files.size(), (float)numOfCorr / files.size()));
  }

  public static void main(String[] args) throws IOException {
    // runCorrection();
    //
    // runWriteText(3);
    // runWriteText(5);
    // runWriteText(10);
    // runWriteText(20);
    runWriteText(50);
    runWriteText(100);

    evaluateSuggestRecall(3);
    evaluateSuggestRecall(5);
    evaluateSuggestRecall(10);
    evaluateSuggestRecall(20);
    evaluateSuggestRecall(50);
    evaluateSuggestRecall(100);

    evaluateSuggestRecall(3,   "tmp.Mar17");
    evaluateSuggestRecall(5,   "tmp.Mar17");
    evaluateSuggestRecall(10,  "tmp.Mar17");
    evaluateSuggestRecall(20,  "tmp.Mar17");
    evaluateSuggestRecall(50,  "tmp.Mar17");
    evaluateSuggestRecall(100, "tmp.Mar17");
  }
}
