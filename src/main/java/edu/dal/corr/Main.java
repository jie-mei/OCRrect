package edu.dal.corr;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import edu.dal.corr.suggest.Suggestion;
import edu.dal.corr.suggest.Feature;
import edu.dal.corr.suggest.LanguagePopularityFeature;
import edu.dal.corr.suggest.LevenshteinDistanceFeature;
import edu.dal.corr.suggest.StringSimilarityFeature;
import edu.dal.corr.util.IOUtils;
import edu.dal.corr.util.LogUtils;
import edu.dal.corr.util.ResourceUtils;
import edu.dal.corr.word.CommonWordFilter;
import edu.dal.corr.word.GoogleTokenizer;

/**
 * @since 2016.08.10
 */
public class Main
{
  public static void main(String[] args)
    throws IOException
  {
    List<Suggestion> suggestions = new DocumentCorrector().correct(
        new GoogleTokenizer(),
        new CommonWordFilter(),
        Arrays.asList(new Feature[] {
            new LevenshteinDistanceFeature(),
            new LanguagePopularityFeature(),
            new StringSimilarityFeature()
        }),
        IOUtils.read(ResourceUtils.TEST_INPUT_SEGMENT));
  }
}
