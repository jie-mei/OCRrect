package edu.dal.corr;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import edu.dal.corr.eval.Evaluations;
import edu.dal.corr.eval.GroundTruthError;
import edu.dal.corr.eval.GroundTruthErrors;
import edu.dal.corr.util.IOUtils;
import edu.dal.corr.util.ResourceUtils;
import edu.dal.corr.util.StringUtils;
import edu.dal.corr.word.CommonPatternFilter;
import edu.dal.corr.word.CommonWordFilter;
import edu.dal.corr.word.FuzzyStatisticalGoogleNgramThresholdWordFilter;
import edu.dal.corr.word.GoogleTokenizer;
import edu.dal.corr.word.HuristicGoogleNgramThresholdWordFilter;
import edu.dal.corr.word.StatisticalGoogleNgramThresholdWordFilter;
import edu.dal.corr.word.Words;

/**
 * @since 2016.08.10
 */
public class Evaluate
{
  public static void main(String[] args)
    throws IOException
  {
     evalDetection(ResourceUtils.GT_ERROR, ResourceUtils.INPUT);
  }

  public static void evalDetection(Path gtErrorFile, List<Path> inputFiles)
    throws IOException
  {
    List<GroundTruthError> errors = GroundTruthErrors.read(gtErrorFile);

    String input = StringUtils.fixLineBrokenWords(IOUtils.read(inputFiles));
    
    Evaluations.evalDetection("commonPattern", errors,
        Words.get(input,
            new GoogleTokenizer(),
            new CommonPatternFilter(),
            new CommonWordFilter()));
    Evaluations.evalDetection("huristicThreshold", errors,
        Words.get(input,
            new GoogleTokenizer(),
            new CommonPatternFilter(),
            new HuristicGoogleNgramThresholdWordFilter()));
    Evaluations.evalDetection("100stddevThreshold", errors,
        Words.get(input,
            new GoogleTokenizer(),
            new CommonPatternFilter(),
            new StatisticalGoogleNgramThresholdWordFilter(1)));
    Evaluations.evalDetection("50stddevThreshold", errors,
        Words.get(input,
            new GoogleTokenizer(),
            new CommonPatternFilter(),
            new StatisticalGoogleNgramThresholdWordFilter(0.5f)));
    Evaluations.evalDetection("25stddevThreshold", errors,
        Words.get(input,
            new GoogleTokenizer(),
            new CommonPatternFilter(),
            new StatisticalGoogleNgramThresholdWordFilter(0.25f)));
    Evaluations.evalDetection("12stddevThreshold", errors,
        Words.get(input,
            new GoogleTokenizer(),
            new CommonPatternFilter(),
            new StatisticalGoogleNgramThresholdWordFilter(0.12f)));
    Evaluations.evalDetection("0stddevThreshold", errors,
        Words.get(input,
            new GoogleTokenizer(),
            new CommonPatternFilter(),
            new StatisticalGoogleNgramThresholdWordFilter(0f)));
    Evaluations.evalDetection("100stddevFuzzyThreshold", errors,
        Words.get(input,
            new GoogleTokenizer(),
            new CommonPatternFilter(),
            new FuzzyStatisticalGoogleNgramThresholdWordFilter(1)));
    Evaluations.evalDetection("50stddevFuzzyThreshold", errors,
        Words.get(input,
            new GoogleTokenizer(),
            new CommonPatternFilter(),
            new FuzzyStatisticalGoogleNgramThresholdWordFilter(0.5f)));
    Evaluations.evalDetection("25stddevFuzzyThreshold", errors,
        Words.get(input,
            new GoogleTokenizer(),
            new CommonPatternFilter(),
            new FuzzyStatisticalGoogleNgramThresholdWordFilter(0.25f)));
    Evaluations.evalDetection("12stddevFuzzyThreshold", errors,
        Words.get(input,
            new GoogleTokenizer(),
            new CommonPatternFilter(),
            new FuzzyStatisticalGoogleNgramThresholdWordFilter(0.12f)));
    Evaluations.evalDetection("0stddevFuzzyThreshold", errors,
        Words.get(input,
            new GoogleTokenizer(),
            new CommonPatternFilter(),
            new FuzzyStatisticalGoogleNgramThresholdWordFilter(0f)));
  }
}
