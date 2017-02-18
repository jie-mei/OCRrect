package edu.dal.corr;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import edu.dal.corr.suggest.NgramBoundedReaderSearcher;
import edu.dal.corr.suggest.Suggestion;
import edu.dal.corr.suggest.feature.ContextCoherenceFeature;
import edu.dal.corr.suggest.feature.DistanceFeature;
import edu.dal.corr.suggest.feature.Feature;
import edu.dal.corr.suggest.feature.LanguagePopularityFeature;
import edu.dal.corr.suggest.feature.LexiconExistenceFeature;
import edu.dal.corr.suggest.feature.Scoreable;
import edu.dal.corr.suggest.feature.ApproximateContextCoherenceFeature;
import edu.dal.corr.suggest.feature.StringSimilarityFeature;
import edu.dal.corr.util.PathUtils;
import edu.dal.corr.util.IOUtils;
import edu.dal.corr.util.ResourceUtils;
import edu.dal.corr.util.Unigram;
import edu.dal.corr.word.GoogleTokenizer;
import edu.dal.corr.word.filter.CommonPatternFilter;
import edu.dal.corr.word.filter.CommonWordFilter;
import edu.dal.corr.word.filter.WordFilter;

/**
 * @since 2017.02.17
 */
public class Main
{
  public static NgramBoundedReaderSearcher getNgramSearch(
      String pathname,
      List<Path> dataPath)
  {
    try {
      NgramBoundedReaderSearcher ngramSearch = NgramBoundedReaderSearcher.read(
          PathUtils.TEMP_DIR.resolve(Paths.get(pathname)));
      ngramSearch.setNgramPath(dataPath);
      return ngramSearch;
    } catch (IOException e) {
      throw new RuntimeException(String.format(
          "Cannot open %s in building NgramBoundedReaderSearcher object.",
          pathname));
    }
  }

  public static void runCorrection()
      throws IOException
  {
    // Construct unigram.
    Unigram unigram = Unigram.getInstance();

    // Read pre-processed 5-gram search indexing.
    NgramBoundedReaderSearcher bigram   = getNgramSearch("2gm.search", ResourceUtils.BIGRAM);
    NgramBoundedReaderSearcher trigram  = getNgramSearch("3gm.search", ResourceUtils.TRIGRAM);
    NgramBoundedReaderSearcher fourgram = getNgramSearch("4gm.search", ResourceUtils.FOURGRAM);
    NgramBoundedReaderSearcher fivegram = getNgramSearch("5gm.search", ResourceUtils.FIVEGRAM);

    // Construct features.
    Feature[] features = new Feature[]{
            new DistanceFeature("Levenstein",
                Scoreable.levenshteinDist(), true),
            new DistanceFeature("Damerau-Levnstein",
                Scoreable.damerauLevDist(), true),
            new DistanceFeature("LCS",
                Scoreable.lscDist(), true),
            new DistanceFeature("Optimal-StringAlign",
                Scoreable.optStrAlignDist(), true),
            new DistanceFeature("Jaro-Winkler",
                Scoreable.jaroWinklerDist(), false),

            new DistanceFeature("ngram-Jaccard-unigram",
                Scoreable.jaccardDist(1), false),
            new DistanceFeature("ngram-Jaccard-bigram",
                Scoreable.jaccardDist(2), false),
            new DistanceFeature("ngram-Jaccard-trigram",
                Scoreable.jaccardDist(3), false),
            new DistanceFeature("ngram-Jaccard-fourgram",
                Scoreable.jaccardDist(4), false),
            new DistanceFeature("ngram-Jaccard-fivegram",
                Scoreable.jaccardDist(5), false),

            new DistanceFeature("ngram-binary-bigram",
                Scoreable.binNgramDist(2), false),
            new DistanceFeature("ngram-binary-trigram",
                Scoreable.binNgramDist(3), false),
            new DistanceFeature("ngram-binary-fourgram",
                Scoreable.binNgramDist(4), false),
            new DistanceFeature("ngram-binary-fivegram",
                Scoreable.binNgramDist(5), false),
            new DistanceFeature("ngram-positional-bigram",
                Scoreable.posNgramDist(2), false),
            new DistanceFeature("ngram-positional-trigram",
                Scoreable.posNgramDist(3), false),
            new DistanceFeature("ngram-positional-fourgram",
                Scoreable.posNgramDist(4), false),
            new DistanceFeature("ngram-positional-fivegram",
                Scoreable.posNgramDist(5), false),
            new DistanceFeature("ngram-comprehensive-bigram",
                Scoreable.cmphNgramDist(2), false),
            new DistanceFeature("ngram-comprehensive-trigram",
                Scoreable.cmphNgramDist(3), false),
            new DistanceFeature("ngram-comprehensive-fourgram",
                Scoreable.cmphNgramDist(4), false),
            new DistanceFeature("ngram-comprehensive-fivegram",
                Scoreable.cmphNgramDist(5), false),

            new DistanceFeature("qgram-bigram",
                Scoreable.qgramDist(2), false),
            new DistanceFeature("qgram-trigram",
                Scoreable.qgramDist(3), false),
            new DistanceFeature("qgram-fourgram",
                Scoreable.qgramDist(4), false),
            new DistanceFeature("qgram-fivegram",
                Scoreable.qgramDist(5), false),


            new StringSimilarityFeature(unigram),
            new LanguagePopularityFeature(unigram),

            new LexiconExistenceFeature("Lexical",
                IOUtils.readList(ResourceUtils.LEXI_LEXICON)),
            new LexiconExistenceFeature("Special",
                IOUtils.readList(ResourceUtils.SPECIAL_LEXICON)),
            new LexiconExistenceFeature("Wikipedia",
                IOUtils.readList(ResourceUtils.WIKI_LEXICON)),

            new ContextCoherenceFeature("Bigram", bigram, 2),
            new ContextCoherenceFeature("Trigram", trigram, 3),
            new ContextCoherenceFeature("Fourgram", fourgram, 4),
            new ContextCoherenceFeature("Fivegram", fivegram, 5),

            new ApproximateContextCoherenceFeature("Bigram", bigram, 2),
            new ApproximateContextCoherenceFeature("Trigram", trigram, 3),
            new ApproximateContextCoherenceFeature("Fourgram", fourgram, 4),
            new ApproximateContextCoherenceFeature("Fivegram", fivegram, 5),
        };

    // Generate suggestions.
    List<Suggestion> suggestions = new DocumentCorrector().correct(
        new GoogleTokenizer(),
        Arrays.asList(new WordFilter[] {
            new CommonPatternFilter(),
            new CommonWordFilter()
        }),
        Arrays.asList(features),
        IOUtils.read(ResourceUtils.INPUT)
    );
  }
  
  public static void runRewrite()
    throws IOException
  {
    Suggestion.writeText(
        Suggestion.readList(Paths.get("tmp/suggestion.top.3")),
        Paths.get("tmp/test.top3.txt"));

//      GroundTruthErrors.read(ResourceUtils.GT_ERROR).forEach(err -> {
//        System.out.println(err.errorText() + "\t" + err.gtText());
//      });
  }

  public static void main(String[] args)
    throws IOException
  {
    runCorrection();
  }
}
