package edu.dal.ocrrect.expr.detect;

import edu.dal.ocrrect.expr.ExprUtils;
import edu.dal.ocrrect.io.WordTSVFile;
import edu.dal.ocrrect.Text;
import edu.dal.ocrrect.util.IOUtils;
import edu.dal.ocrrect.util.ResourceUtils;
import edu.dal.ocrrect.util.Word;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Consider there has positioning errors in the generated words created in the segmentation process.
 * This process align the generated words to the position in the original OCR files to eliminate the
 * potential problems in the analyzing pipeline.
 */
public class FixGeneratedWordPosition {

  private static final Path OUTPUT_DIR = ExprUtils.TEMP_DIR.resolve("detect").resolve("data");
  private static final Path WORDS_TRAIN_TSV = OUTPUT_DIR.resolve("words.train.tsv");
  private static final Path WORDS_TEST_TSV  = OUTPUT_DIR.resolve("words.train.tsv");
  private static final List<Path> OCR_TEXT_PATHS = ResourceUtils.getResourceInDir(
      "*.txt", "mibio-ocr/ocr");

  private static final Path WORDS_TRAIN_FIXED_TSV = OUTPUT_DIR.resolve("words.train.fixed.tsv");
  private static final Path WORDS_TEST_FIXED_TSV  = OUTPUT_DIR.resolve("words.train.fixed.tsv");

  /**
   * Align the given words to the according textual units in the text.
   * @return Aligned words.
   */
  private static List<Word> realignWords(String text, List<Word> unaligned) {
    int tPos = 0;
    List<Word> aligned = new ArrayList<>();
    for (Word word: unaligned) {
      for (int i = 0; i < word.text().length(); i++) {
        char c = word.text().charAt(i);
        while (Character.isWhitespace(text.charAt(tPos))) {
          tPos++;
        }
        if (c != text.charAt(tPos)) {
          throw new RuntimeException("Character unmatched at '" + word.text() +
                                     "': " + c + ", " + text.charAt(tPos));
        }
        if (i == 0) {
          aligned.add(new Word(tPos, word.context()));
        }
      }
    }
    return aligned;
  }

  public static void main(String args[]) throws IOException {
    String ocrText = new Text(IOUtils.read(OCR_TEXT_PATHS)).text();

    // Combine train and test together and align with the entire text.
    List<Word> trainWords = new WordTSVFile(WORDS_TRAIN_TSV).read();
    List<Word> testWords  = new WordTSVFile(WORDS_TEST_TSV).read();

    realignWords(ocrText, trainWords);
  }
}
