package edu.dal.corr;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import edu.dal.corr.suggest.ExactContextFeature;
import edu.dal.corr.suggest.Feature;
import edu.dal.corr.suggest.LanguagePopularityFeature;
import edu.dal.corr.suggest.LevenshteinDistanceFeature;
import edu.dal.corr.suggest.LexiLexiconExistenceFeature;
import edu.dal.corr.suggest.NgramBoundedReaderSearcher;
import edu.dal.corr.suggest.RelaxContextFeature;
import edu.dal.corr.suggest.SpecialLexiconExistenceFeature;
import edu.dal.corr.suggest.StringSimilarityFeature;
import edu.dal.corr.suggest.Suggestion;
import edu.dal.corr.suggest.Suggestions;
import edu.dal.corr.suggest.WikiLexiconExistenceFeature;
import edu.dal.corr.util.FileUtils;
import edu.dal.corr.util.IOUtils;
import edu.dal.corr.util.ResourceUtils;
import edu.dal.corr.word.CommonDictionayWordFilter;
import edu.dal.corr.word.CommonPatternFilter;
import edu.dal.corr.word.GoogleTokenizer;
import edu.dal.corr.word.WordFilter;

/**
 * @since 2016.08.10
 */
public class Evaluate
{
  public static void main(String[] args)
    throws IOException
  {
    List<Suggestion> suggests = Suggestions.read(Paths.get("tmp/suggestion"));
    for (Suggestion s : suggests) {
      System.out.println(String.format("%s %d: %d",
            s.text(), s.position(), s.candidates().length));
    }
  }
}
