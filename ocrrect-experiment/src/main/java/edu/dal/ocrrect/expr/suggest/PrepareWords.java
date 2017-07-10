package edu.dal.ocrrect.expr.suggest;

import edu.dal.ocrrect.expr.Constants;
import edu.dal.ocrrect.expr.ExprUtils;
import edu.dal.ocrrect.io.IntegerTSVFile;
import edu.dal.ocrrect.io.StringTSVFile;
import edu.dal.ocrrect.io.WordTSVFile;
import edu.dal.ocrrect.util.ResourceUtils;
import edu.dal.ocrrect.util.Token;
import edu.dal.ocrrect.util.Word;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * The training processing uses the ground-truth errors before the split point. Thus the
 * pre-processing is conducted as follows.
 */
public class PrepareWords {

  private static Path TRAIN_WORDS_SEGMENTED_PATH = Paths.get("tmp/detect/data/words.train.tsv");
  private static Path TEST_WORDS_SEGMENTED_PATH = Paths.get("tmp/detect/data/words.test.tsv");
  private static Path TEST_LABELS_DETECT_PATH = Paths.get("tmp/detect/label/labels.tsv");

  private static class ErrorToken {
    private Token error;
    private String correction;
    public ErrorToken(Token error, String correction) {
      this.error = error;
      this.correction = correction;
    }
  }

  private static class ErrorWord {
    private Word error;
    private String correction;
    public ErrorWord(Word error, String correction) {
      this.error = error;
      this.correction = correction;
    }
  }

  private static List<ErrorToken> extractGTErrorTokens() {
    // Read GT error tokens from file.
    List<ErrorToken> errors = new ArrayList<>();
    try (BufferedReader br = Files.newBufferedReader(SuggestConstants.ERROR_GT_PATH)) {
      for (String line = br.readLine(); line != null; line = br.readLine()) {
        String[] splits = line.split("\t");
        if (splits[1].length() == 0) {
          continue; // omit the insertion errors
        }
        int pos = Integer.parseInt(splits[0]);
        Token error = new Token(splits[1], pos);
        if (splits[1].length() == 0 || splits.length < 3) continue; // omit inserting or deletion error.
        String correction = splits.length >= 4 && splits[3].length() > 0 ? splits[3]: splits[2];
        errors.add(new ErrorToken(error, correction));
      }
    } catch (IOException e) {
      throw new RuntimeException(e); // expect no error
    }
    return errors;
  }

  private static List<List<ErrorWord>> matchSegmentedWords(List<ErrorToken> errorTokens) {
    // Read segmented words generated from the segmentation step.
    List<Word> words;
    try {
      words = new WordTSVFile(TRAIN_WORDS_SEGMENTED_PATH).read();
    } catch (IOException e) {
      throw new RuntimeException(e); // expect no error
    }
    // Construct a mapping from the starting position to the according word index.
    TIntIntHashMap pos2idx = new TIntIntHashMap();
    for (int i = 0; i < words.size(); i++) {
      pos2idx.put(words.get(i).position(), i);
    }
    // Map tokens to word segments.
    List<ErrorWord> mapped = new ArrayList<>();          // words which texts contains tokens
    List<ErrorWord> mappedIdentical = new ArrayList<>(); // words which texts are identical to tokens
    int c = 0;
    for (ErrorToken etk: errorTokens) {
      if (pos2idx.containsKey(etk.error.position())) {
        Word w = words.get(pos2idx.get(etk.error.position()));
        ErrorWord ew = new ErrorWord(w, etk.correction);
        mapped.add(ew);
        if (w.text().equals(etk.error.text())) {
          mappedIdentical.add(ew);
        }
      }
    }
    return Arrays.asList(mapped, mappedIdentical);
  }

  private static void writeErrorWords(List<ErrorWord> errorWords, Path wordTSV, Path corrTSV)
      throws IOException {
    List<Word> words = new ArrayList<>();
    List<String> corrs = new ArrayList<>();
    for (ErrorWord ew: errorWords) {
      words.add(ew.error);
      corrs.add(ew.correction);
    }
    new WordTSVFile(wordTSV).write(words);
    new StringTSVFile(corrTSV).write(corrs);
  }

  private static List<Word> extractTestWords() throws IOException {
    List<Word> words = new WordTSVFile(TEST_WORDS_SEGMENTED_PATH).read();
    List<Integer> labels = new IntegerTSVFile(TEST_LABELS_DETECT_PATH).read();
    return IntStream
      .range(0, words.size())
      .filter(i -> labels.get(i) > 0)
      .mapToObj(words::get)
      .collect(Collectors.toList());
  }

  private static List<String> matchCorrections(List<Word> words, List<ErrorToken> errors) {
    // Construct a map from position to correction.
    TIntObjectHashMap<String> pos2corr = new TIntObjectHashMap<>();
    for (ErrorToken etk: errors) {
      pos2corr.put(etk.error.position(), etk.correction);
    }
    // Map corrections to words.
    List<String> corrs = new ArrayList<>();
    int count = 0;
    for (Word w: words) {
      if (! pos2corr.containsKey(w.position())) {
        corrs.add("");  // gives am empty string for the non-mapping error
      } else {
        corrs.add(pos2corr.get(w.position()));
      }
    }
    return corrs;
  }

  public static void main(String[] args) throws IOException {
    List<ErrorToken> errorTokens = extractGTErrorTokens();

    {
      // Extract the ground truth errors from file.
      List<ErrorToken> trainErrorTokens =
        errorTokens
          .stream()
          .filter(err -> err.error.position() < Constants.SPLIT_POS)
          .collect(Collectors.toList());

      // Find the context words for each error from the serialized segments generated in the
      // segmentation stage.
      List<ErrorWord> mapped;
      List<ErrorWord> mappedIdentical;
      {
        List<List<ErrorWord>> lists = matchSegmentedWords(trainErrorTokens);
        mapped = lists.get(0);
        mappedIdentical = lists.get(1);
      }

      // Serialize the training words and corrections to a TSV file.
      ExprUtils.ensurePath(SuggestConstants.DATA_PATH);
      writeErrorWords(mapped,
        SuggestConstants.TRAIN_WORDS_MAPPED_TSV_PATH,
        SuggestConstants.TRAIN_CORRS_MAPPED_TSV_PATH);
      writeErrorWords(mappedIdentical,
        SuggestConstants.TRAIN_WORDS_MAPPED_IDENTICAL_TSV_PATH,
        SuggestConstants.TRAIN_CORRS_MAPPED_IDENTICAL_TSV_PATH);
    }
    {
      List<Word> testErrorWords = extractTestWords();
      List<String> corrections = matchCorrections(testErrorWords, errorTokens);
      new WordTSVFile(SuggestConstants.TEST_WORDS_MAPPED_TSV_PATH).write(testErrorWords);
      new StringTSVFile(SuggestConstants.TEST_CORRS_MAPPED_TSV_PATH).write(corrections);
    }
  }
}
