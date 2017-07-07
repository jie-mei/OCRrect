package edu.dal.ocrrect.expr.suggest;

import edu.dal.ocrrect.expr.Constants;
import edu.dal.ocrrect.expr.ExprUtils;
import edu.dal.ocrrect.io.WordTSVFile;
import edu.dal.ocrrect.suggest.NgramBoundedReaderSearcher;
import edu.dal.ocrrect.suggest.Scoreable;
import edu.dal.ocrrect.suggest.Suggestion;
import edu.dal.ocrrect.suggest.feature.*;
import edu.dal.ocrrect.util.IOUtils;
import edu.dal.ocrrect.util.ResourceUtils;
import edu.dal.ocrrect.util.Unigram;
import edu.dal.ocrrect.util.Word;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class SuggestionEvaluation {

  private static int TOP = 100;

  private static NgramBoundedReaderSearcher getNgramSearch(String pathname, List<Path> dataPath) {
    try {
      NgramBoundedReaderSearcher ngramSearch =
        NgramBoundedReaderSearcher.read(ExprUtils.TEMP_DIR.resolve(Paths.get(pathname)));
      ngramSearch.setNgramPath(dataPath);
      return ngramSearch;
    } catch (IOException e) {
      throw new RuntimeException(
        String.format("Cannot open %s in building NgramBoundedReaderSearcher object.", pathname));
    }
  }

  private static List<Feature> constructFeatures() {
    // Construct unigram.
    Unigram unigram = Unigram.getInstance();

    // Read pre-processed 5-gram search indexing.
    NgramBoundedReaderSearcher bigram = getNgramSearch("2gm.search", ResourceUtils.BIGRAM);
    NgramBoundedReaderSearcher trigram = getNgramSearch("3gm.search", ResourceUtils.TRIGRAM);
    NgramBoundedReaderSearcher fourgram = getNgramSearch("4gm.search", ResourceUtils.FOURGRAM);
    NgramBoundedReaderSearcher fivegram = getNgramSearch("5gm.search", ResourceUtils.FIVEGRAM);

    try {
      return Arrays.asList(
          new DistanceFeature("Levenstein", Scoreable.levenshteinDist()),
          new DistanceFeature("Damerau-Levnstein", Scoreable.damerauLevDist()),
          new DistanceFeature("LCS", Scoreable.lscDist()),
          new DistanceFeature("Optimal-StringAlign", Scoreable.optStrAlignDist()),
          new DistanceFeature("Jaro-Winkler", Scoreable.jaroWinklerDist()),
          new DistanceFeature("ngram-Jaccard-unigram", Scoreable.jaccardDist(1)),
          new DistanceFeature("ngram-Jaccard-bigram", Scoreable.jaccardDist(2)),
          new DistanceFeature("ngram-Jaccard-trigram", Scoreable.jaccardDist(3)),
          new DistanceFeature("ngram-Jaccard-fourgram", Scoreable.jaccardDist(4)),
          new DistanceFeature("ngram-Jaccard-fivegram", Scoreable.jaccardDist(5)),
          new DistanceFeature("qgram-bigram", Scoreable.qgramDist(2)),
          new DistanceFeature("qgram-trigram", Scoreable.qgramDist(3)),
          new DistanceFeature("qgram-fourgram", Scoreable.qgramDist(4)),
          new DistanceFeature("qgram-fivegram", Scoreable.qgramDist(5)),
          new DistanceFeature("ngram-binary-bigram", Scoreable.binNgramDist(2)),
          new DistanceFeature("ngram-binary-trigram", Scoreable.binNgramDist(3)),
          new DistanceFeature("ngram-binary-fourgram", Scoreable.binNgramDist(4)),
          new DistanceFeature("ngram-binary-fivegram", Scoreable.binNgramDist(5)),
          new DistanceFeature("ngram-positional-bigram", Scoreable.posNgramDist(2)),
          new DistanceFeature("ngram-positional-trigram", Scoreable.posNgramDist(3)),
          new DistanceFeature("ngram-positional-fourgram", Scoreable.posNgramDist(4)),
          new DistanceFeature("ngram-positional-fivegram", Scoreable.posNgramDist(5)),
          new DistanceFeature("ngram-comprehensive-bigram", Scoreable.cmphNgramDist(2)),
          new DistanceFeature("ngram-comprehensive-trigram", Scoreable.cmphNgramDist(3)),
          new DistanceFeature("ngram-comprehensive-fourgram", Scoreable.cmphNgramDist(4)),
          new DistanceFeature("ngram-comprehensive-fivegram", Scoreable.cmphNgramDist(5)),
          new StringSimilarityFeature(),
          new LanguagePopularityFeature(unigram),
          new LexiconExistenceFeature("Lexical", IOUtils.readList(ResourceUtils.LEXI_LEXICON)),
          new LexiconExistenceFeature("Special", IOUtils.readList(ResourceUtils.SPECIAL_LEXICON)),
          new LexiconExistenceFeature("Wikipedia", IOUtils.readList(ResourceUtils.WIKI_LEXICON)),
          new ContextCoherenceFeature("Bigram", bigram, 2),
          new ContextCoherenceFeature("Trigram", trigram, 3),
          new ContextCoherenceFeature("Fourgram", fourgram, 4),
          new ContextCoherenceFeature("Fivegram", fivegram, 5),
          new ApproximateContextCoherenceFeature("Bigram", bigram, 2),
          new ApproximateContextCoherenceFeature("Trigram", trigram, 3),
          new ApproximateContextCoherenceFeature("Fourgram", fourgram, 4),
          new ApproximateContextCoherenceFeature("Fivegram", fivegram, 5)
      );
    } catch (IOException e) {
      throw new RuntimeException(); // expect no error
    }
  }

  public static void main(String arg[]) throws IOException {
    // Use mapped words for suggesting in batch mode (mappedIdentical is the subset of mapped).
    List<Word> mapped = new WordTSVFile(PrepareTrainWords.WORDS_MAPPED_PATH).read();
    List<Suggestion> suggests = Suggestion.suggest(mapped, constructFeatures(), TOP, false);
    Suggestion.write(suggests, Constants.SUGGEST_DATA_PATH.resolve("suggest.top" + TOP), "suggest");
  }
}
