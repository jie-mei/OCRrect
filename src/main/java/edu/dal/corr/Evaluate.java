package edu.dal.corr;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import edu.dal.corr.eval.Evaluations;
import edu.dal.corr.eval.GroundTruthError;
import edu.dal.corr.eval.GroundTruthErrors;
import edu.dal.corr.util.IOUtils;
import edu.dal.corr.util.ResourceUtils;
import edu.dal.corr.word.GoogleTokenizer;
import edu.dal.corr.word.Word;
import edu.dal.corr.word.filter.CommonPatternFilter;
import edu.dal.corr.word.filter.CommonWordFilter;
import edu.dal.corr.word.filter.FuzzyStatisticalGoogleNgramThresholdWordFilter;
import edu.dal.corr.word.filter.HuristicGoogleNgramThresholdWordFilter;
import edu.dal.corr.word.filter.StatisticalGoogleNgramThresholdWordFilter;

/**
 * @since 2017.01.18
 */
public class Evaluate
{
  public static void main(String[] args)
    throws IOException
  {
//    evalDetection(ResourceUtils.GT_ERROR, ResourceUtils.INPUT);
  }

  /**
   * Evaluate the error detection performance using different combination of
   * word filters.
   *
   * @param  gtErrorFile  path to the ground truth.
   * @param  inputFiles   path to the input files.
   *
   * @throws IOException  If I/O error occurs.
   */
//  public static void evalDetection(Path gtErrorFile, List<Path> inputFiles)
//    throws IOException
//  {
//    List<GroundTruthError> errors = GroundTruthErrors.read(gtErrorFile);
//    
//    // Read input and fix the broken words due to the line break.
//    String input = StringUtils.fixLineBrokenWords(IOUtils.read(inputFiles));
//    
//    Evaluations.evalDetection("commonPattern", errors,
//        Word.get(input,
//            new GoogleTokenizer(),
//            new CommonPatternFilter(),
//            new CommonWordFilter()));
//    Evaluations.evalDetection("huristicThreshold", errors,
//        Word.get(input,
//            new GoogleTokenizer(),
//            new CommonPatternFilter(),
//            new HuristicGoogleNgramThresholdWordFilter()));
//    Evaluations.evalDetection("100stddevThreshold", errors,
//        Word.get(input,
//            new GoogleTokenizer(),
//            new CommonPatternFilter(),
//            new StatisticalGoogleNgramThresholdWordFilter(1)));
//    Evaluations.evalDetection("50stddevThreshold", errors,
//        Word.get(input,
//            new GoogleTokenizer(),
//            new CommonPatternFilter(),
//            new StatisticalGoogleNgramThresholdWordFilter(0.5f)));
//    Evaluations.evalDetection("25stddevThreshold", errors,
//        Word.get(input,
//            new GoogleTokenizer(),
//            new CommonPatternFilter(),
//            new StatisticalGoogleNgramThresholdWordFilter(0.25f)));
//    Evaluations.evalDetection("12stddevThreshold", errors,
//        Word.get(input,
//            new GoogleTokenizer(),
//            new CommonPatternFilter(),
//            new StatisticalGoogleNgramThresholdWordFilter(0.12f)));
//    Evaluations.evalDetection("0stddevThreshold", errors,
//        Word.get(input,
//            new GoogleTokenizer(),
//            new CommonPatternFilter(),
//            new StatisticalGoogleNgramThresholdWordFilter(0f)));
//    Evaluations.evalDetection("100stddevFuzzyThreshold", errors,
//        Word.get(input,
//            new GoogleTokenizer(),
//            new CommonPatternFilter(),
//            new FuzzyStatisticalGoogleNgramThresholdWordFilter(1)));
//    Evaluations.evalDetection("50stddevFuzzyThreshold", errors,
//        Word.get(input,
//            new GoogleTokenizer(),
//            new CommonPatternFilter(),
//            new FuzzyStatisticalGoogleNgramThresholdWordFilter(0.5f)));
//    Evaluations.evalDetection("25stddevFuzzyThreshold", errors,
//        Word.get(input,
//            new GoogleTokenizer(),
//            new CommonPatternFilter(),
//            new FuzzyStatisticalGoogleNgramThresholdWordFilter(0.25f)));
//    Evaluations.evalDetection("12stddevFuzzyThreshold", errors,
//        Word.get(input,
//            new GoogleTokenizer(),
//            new CommonPatternFilter(),
//            new FuzzyStatisticalGoogleNgramThresholdWordFilter(0.12f)));
//    Evaluations.evalDetection("0stddevFuzzyThreshold", errors,
//        Word.get(input,
//            new GoogleTokenizer(),
//            new CommonPatternFilter(),
//            new FuzzyStatisticalGoogleNgramThresholdWordFilter(0f)));
//  }
}
