package edu.dal.ocrrect.expr.suggest;

import edu.dal.ocrrect.eval.GroundTruthError;
import edu.dal.ocrrect.io.StringTSVFile;
import edu.dal.ocrrect.io.WordTSVFile;
import edu.dal.ocrrect.suggest.Candidate;
import edu.dal.ocrrect.suggest.Suggestion;
import edu.dal.ocrrect.suggest.feature.FeatureType;
import edu.dal.ocrrect.util.IOUtils;
import edu.dal.ocrrect.util.Word;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

public class ConvertSuggestionToText {

  /**
   * Generate text given binary folder path.
   */
  private static void genText(Path binPath, Path textPath, List<GroundTruthError> errors)
      throws IOException {
    List<Suggestion> test = Suggestion.readList(binPath);
    if (Files.notExists(textPath)) {
      Suggestion.writeText(test, errors, textPath);
    }
  }

  /**
   * Generate text given binary part prefix. This method calls {@code #genText(Path, Path, List)}.
   */
  private static void genText(String binPartPrefix,
                              String txtPartPrefix,
                              int numParts,
                              List<GroundTruthError> errors)
      throws IOException {
    for (int i = 1; i <= numParts; i++) {
      String binFilename = String.format(binPartPrefix + ".part%02d", i);
      String txtFilename = String.format(txtPartPrefix + ".part%02d.txt", i);
      Path binPath = SuggestConstants.DATA_PATH.resolve(binFilename);
      Path txtPath = SuggestConstants.DATA_PATH.resolve(txtFilename);
      System.out.println("bin path: " + binPath);
      System.out.println("txt path: " + txtPath);
      genText(binPath, txtPath, errors);
    }
  }

  public static void main(String[] args) throws IOException {
    List<GroundTruthError> errors = GroundTruthError.read(SuggestConstants.ERROR_GT_PATH);

    int[] tops = {1, 3, 5, 10, 100};
    for (int top: tops) {
      String prefix;
      prefix = String.format("suggest.train.top%d", top);
      genText(prefix, prefix, 13, errors);
      prefix = String.format("suggest.test.top%d", top);
      genText(prefix, prefix, 4, errors);
    }
  }
}
