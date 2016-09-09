package edu.dal.corr;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import edu.dal.corr.suggest.ExactContextFeature;
import edu.dal.corr.suggest.Feature;
import edu.dal.corr.suggest.LanguagePopularityFeature;
import edu.dal.corr.suggest.LevenshteinDistanceFeature;
import edu.dal.corr.suggest.LexiLexiconExistenceFeature;
import edu.dal.corr.suggest.NgramBoundedReaderSearcher;
import edu.dal.corr.suggest.NgramBoundedReaderSearchers;
import edu.dal.corr.suggest.RelaxContextFeature;
import edu.dal.corr.suggest.SpecialLexiconExistenceFeature;
import edu.dal.corr.suggest.StringSimilarityFeature;
import edu.dal.corr.suggest.Suggestion;
import edu.dal.corr.suggest.WikiLexiconExistenceFeature;
import edu.dal.corr.util.PathUtils;
import edu.dal.corr.util.IOUtils;
import edu.dal.corr.util.ResourceUtils;
import edu.dal.corr.word.CommonWordFilter;
import edu.dal.corr.word.CommonPatternFilter;
import edu.dal.corr.word.GoogleTokenizer;
import edu.dal.corr.word.WordFilter;

/**
 * @since 2016.09.07
 */
public class Main
{
  public static void main(String[] args)
    throws IOException
  {
    // Read pre-processed 5-gram search indexing.
    NgramBoundedReaderSearcher bigram   = getNgramSearch("2gm.search", ResourceUtils.BIGRAM);
    NgramBoundedReaderSearcher trigram  = getNgramSearch("3gm.search", ResourceUtils.TRIGRAM);
    NgramBoundedReaderSearcher fourgram = getNgramSearch("4gm.search", ResourceUtils.FOURGRAM);
    NgramBoundedReaderSearcher fivegram = getNgramSearch("5gm.search", ResourceUtils.FIVEGRAM);

    // Generate suggestions.
    @SuppressWarnings("unused")
    List<Suggestion> suggestions = new DocumentCorrector().correct(
        new GoogleTokenizer(),
        Arrays.asList(new WordFilter[] {
            new CommonPatternFilter(),
            new CommonWordFilter()
        }),
        Arrays.asList(new Feature[] {
            new LevenshteinDistanceFeature(),
            new StringSimilarityFeature(),
            new LexiLexiconExistenceFeature(),
            new SpecialLexiconExistenceFeature(),
            new WikiLexiconExistenceFeature(),
            new LanguagePopularityFeature(),
            new ExactContextFeature.BigramExactContextFeature(bigram),
            new ExactContextFeature.TrigramExactContextFeature(trigram),
            new ExactContextFeature.FourgramExactContextFeature(fourgram),
            new ExactContextFeature.FivegramExactContextFeature(fivegram),
            new RelaxContextFeature.BigramRelaxContextFeature(bigram),
            new RelaxContextFeature.TrigramRelaxContextFeature(trigram),
            new RelaxContextFeature.FourgramRelaxContextFeature(fourgram),
            new RelaxContextFeature.FivegramRelaxContextFeature(fivegram),
        }),
        IOUtils.read(ResourceUtils.INPUT));
  }
  
  public static NgramBoundedReaderSearcher getNgramSearch(
      String pathname,
      List<Path> dataPath)
    throws IOException
  {
    NgramBoundedReaderSearcher ngramSearch = NgramBoundedReaderSearchers.read(
        PathUtils.TEMP_DIR.resolve(Paths.get(pathname)));
    ngramSearch.setNgramPath(dataPath);
    return ngramSearch;
  }
}
