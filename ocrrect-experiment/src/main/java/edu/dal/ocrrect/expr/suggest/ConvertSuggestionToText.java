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

  public static void main(String[] args) throws IOException {
    List<GroundTruthError> errors = GroundTruthError.read(SuggestConstants.ERROR_GT_PATH);

    // Training suggestions.
    {
      List<Suggestion> mapped = Suggestion.readList(SuggestConstants.TRAIN_BINARY_TOP10_PATH);
      if (Files.notExists(SuggestConstants.TRAIN_SUGGESTS_MAPPED_TOP10_TEXT_PATH)) {
        Suggestion.writeText(mapped, errors, SuggestConstants.TRAIN_SUGGESTS_MAPPED_TOP10_TEXT_PATH);
      }
      if (Files.notExists(SuggestConstants.TRAIN_SUGGESTS_MAPPED_IDENTICAL_TOP10_TEXT_PATH)) {
        List<Word> wordMappedIdentical =
            new WordTSVFile(SuggestConstants.TRAIN_WORDS_MAPPED_IDENTICAL_TSV_PATH).read();
        List<Suggestion> mappedIdentical =
            ConvertSuggestionToTSV.selectIdentical(mapped, wordMappedIdentical);
        Suggestion.writeText(mappedIdentical, errors,
            SuggestConstants.TRAIN_SUGGESTS_MAPPED_IDENTICAL_TOP10_TEXT_PATH);
      }
    }
    // Testing suggestions.
    {
      List<Suggestion> test = Suggestion.readList(SuggestConstants.TEST_BINARY_TOP10_PATH);
      if (Files.notExists(SuggestConstants.TEST_SUGGESTS_TOP10_TEXT_PATH)) {
        Suggestion.writeText(test, errors, SuggestConstants.TEST_SUGGESTS_TOP10_TEXT_PATH);
      }
    }
  }
}
