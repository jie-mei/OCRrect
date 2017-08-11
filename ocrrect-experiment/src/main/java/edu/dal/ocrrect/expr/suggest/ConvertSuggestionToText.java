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

  private static void genTrainTexts(Path binPath,
                                    Path mappedPath,
                                    Path mappedIdenticalPath,
                                    List<GroundTruthError> errors,
                                    List<Word> words)
      throws IOException
  {
    List<Suggestion> mapped = Suggestion.readList(binPath);
    if (Files.notExists(mappedPath)) {
      Suggestion.writeText(mapped, errors, mappedPath);
    }
    if (Files.notExists(mappedIdenticalPath)) {
      List<Suggestion> mappedIdentical =
        ConvertSuggestionToTSV.selectIdentical(mapped, words);
      Suggestion.writeText(mappedIdentical, errors, mappedIdenticalPath);
    }
  }

  private static void genTestText(Path binPath, Path textPath, List<GroundTruthError> errors)
      throws IOException
  {
    List<Suggestion> test = Suggestion.readList(binPath);
    if (Files.notExists(textPath)) {
      Suggestion.writeText(test, errors, textPath);
    }
  }

  public static void main(String[] args) throws IOException {
    List<GroundTruthError> errors = GroundTruthError.read(SuggestConstants.ERROR_GT_PATH);
    List<Word> wordMappedIdentical =
        new WordTSVFile(SuggestConstants.TRAIN_WORDS_MAPPED_IDENTICAL_TSV_PATH).read();

    // Training suggestions.
    genTrainTexts(SuggestConstants.TRAIN_BINARY_TOP10_PATH,
                  SuggestConstants.TRAIN_SUGGESTS_MAPPED_TOP10_TEXT_PATH,
                  SuggestConstants.TRAIN_SUGGESTS_MAPPED_IDENTICAL_TOP10_TEXT_PATH,
                  errors, wordMappedIdentical);
    genTrainTexts(SuggestConstants.TRAIN_BINARY_TOP5_PATH,
                  SuggestConstants.TRAIN_SUGGESTS_MAPPED_TOP5_TEXT_PATH,
                  SuggestConstants.TRAIN_SUGGESTS_MAPPED_IDENTICAL_TOP5_TEXT_PATH,
                  errors, wordMappedIdentical);
    genTrainTexts(SuggestConstants.TRAIN_BINARY_TOP3_PATH,
                  SuggestConstants.TRAIN_SUGGESTS_MAPPED_TOP3_TEXT_PATH,
                  SuggestConstants.TRAIN_SUGGESTS_MAPPED_IDENTICAL_TOP3_TEXT_PATH,
                  errors, wordMappedIdentical);
    genTrainTexts(SuggestConstants.TRAIN_BINARY_TOP1_PATH,
                  SuggestConstants.TRAIN_SUGGESTS_MAPPED_TOP1_TEXT_PATH,
                  SuggestConstants.TRAIN_SUGGESTS_MAPPED_IDENTICAL_TOP1_TEXT_PATH,
                  errors, wordMappedIdentical);

    // Testing suggestions.
    genTestText(SuggestConstants.TEST_BINARY_TOP10_PATH,
                SuggestConstants.TEST_SUGGESTS_TOP10_TEXT_PATH,
                errors);
    genTestText(SuggestConstants.TEST_BINARY_TOP5_PATH,
                SuggestConstants.TEST_SUGGESTS_TOP5_TEXT_PATH,
                errors);
    genTestText(SuggestConstants.TEST_BINARY_TOP3_PATH,
                SuggestConstants.TEST_SUGGESTS_TOP3_TEXT_PATH,
                errors);
    genTestText(SuggestConstants.TEST_BINARY_TOP1_PATH,
                SuggestConstants.TEST_SUGGESTS_TOP1_TEXT_PATH,
                errors);
  }
}
