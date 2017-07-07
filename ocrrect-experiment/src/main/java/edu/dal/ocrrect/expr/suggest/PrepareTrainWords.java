package edu.dal.ocrrect.expr.suggest;

import edu.dal.ocrrect.expr.Constants;
import edu.dal.ocrrect.expr.ExprUtils;
import edu.dal.ocrrect.io.WordTSVFile;
import edu.dal.ocrrect.util.ResourceUtils;
import edu.dal.ocrrect.util.Token;
import edu.dal.ocrrect.util.Word;
import gnu.trove.map.hash.TIntIntHashMap;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The training processing uses the ground-truth errors before the split point. Thus the
 * pre-processing is conducted as follows.
 */
public class PrepareTrainWords {

  private static Path ERROR_GT_PATH = ResourceUtils.getResource("mibio-ocr/error.gt.tsv");
  private static Path WORD_SEGMENTED_PATH = Paths.get("tmp/detect/data/words.train.tsv");
  private static Path OUTPUT_PATH = Paths.get("tmp/suggest/data");
  public static Path WORDS_MAPPED_PATH = OUTPUT_PATH.resolve("words.mapped.tsv");
  public static Path WORDS_MAPPED_IDENTICAL_PATH = OUTPUT_PATH.resolve("words.mapped.identical.tsv");

  private static List<Token> extractGTErrorTrainingTokens() {
    // Read GT error tokens from file.
    List<Token> errors = new ArrayList<>();
    try (BufferedReader br = Files.newBufferedReader(ERROR_GT_PATH)) {
      for (String line = br.readLine(); line != null; line = br.readLine()) {
        String[] splits = line.split("\t");
        if (splits[1].length() == 0) {
          continue; // omit the insertion errors
        }
        Token error = new Token(splits[1], Integer.parseInt(splits[0]));
        errors.add(error);

      }
    } catch (IOException e) {
      throw new RuntimeException(e); // expect no error
    }
    // Keep the tokens that are used for training.
    return errors
      .stream()
      .filter(tk -> tk.position() < Constants.SPLIT_POS)
      .collect(Collectors.toList());
  }

  private static List<List<Word>> matchSegmentedWords(List<Token> tokens) {
    // Read segmented words generated from the segmentation step.
    List<Word> words;
    try {
      words = new WordTSVFile(WORD_SEGMENTED_PATH).read();
    } catch (IOException e) {
      throw new RuntimeException(e); // expect no error
    }
    // Construct a mapping from the starting position to the according word index.
    TIntIntHashMap pos2idx = new TIntIntHashMap();
    for (int i = 0; i < words.size(); i++) {
      pos2idx.put(words.get(i).position(), i);
    }
    // Map tokens to word segments.
    List<Word> mapped = new ArrayList<>();          // words which texts contains tokens
    List<Word> mappedIdentical = new ArrayList<>(); // words which texts are identical to tokens
    int c = 0;
    for (Token tk: tokens) {
      if (pos2idx.containsKey(tk.position())) {
        Word w = words.get(pos2idx.get(tk.position()));
        mapped.add(w);
        if (w.text().equals(tk.text())) {
          mappedIdentical.add(w);
        }
      }
    }
    return Arrays.asList(mapped, mappedIdentical);
  }

  public static void main(String[] args) throws IOException {
    // Extract the ground truth errors from file.
    List<Token> trainErrorTokens = extractGTErrorTrainingTokens();

    // Find the context words for each error from the serialized segments generated in the
    // segmentation stage.
    List<Word> mapped;
    List<Word> mappedIdentical;
    {
      List<List<Word>> lists = matchSegmentedWords(trainErrorTokens);
      mapped = lists.get(0);
      mappedIdentical = lists.get(1);
    }

    // Serialize the words to a TSV file.
    ExprUtils.ensurePath(OUTPUT_PATH);
    new WordTSVFile(WORDS_MAPPED_PATH).write(mapped);
    new WordTSVFile(WORDS_MAPPED_IDENTICAL_PATH).write(mappedIdentical);
  }
}
