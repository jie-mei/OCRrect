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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GenerateTrainSuggestion {


  private static NgramBoundedReaderSearcher getNgramSearch(String pathname, List<Path> dataPath) {
    try {
      NgramBoundedReaderSearcher ngramSearch =
        NgramBoundedReaderSearcher.read(Constants.DATA_PATH.resolve(Paths.get(pathname)));
      ngramSearch.setNgramPath(dataPath);
      return ngramSearch;
    } catch (IOException e) {
      throw new RuntimeException(
        String.format("Cannot open %s in building NgramBoundedReaderSearcher object.", pathname));
    }
  }

  private static List<Feature> constructFeatures() {
    // Override the default resource paths.
    // TODO: fix jar resource error.
    ResourceUtils.VOCAB = Paths.get("ocrrect-core/src/main/resources/lexicon/wiki.txt");
    ResourceUtils.SPECIAL_LEXICON = Paths.get("ocrrect-core/src/main/resources/lexicon/special.txt");
    ResourceUtils.LEXI_LEXICON = Paths.get("ocrrect-core/src/main/resources/lexicon/lexicon.txt");
    ResourceUtils.WIKI_LEXICON = Paths.get("ocrrect-core/src/main/resources/lexicon/wiki.txt");

    // Construct unigram.
    Unigram unigram = Unigram.getInstance();

    // Read pre-processed 5-gram search indexing.
    NgramBoundedReaderSearcher bigram = getNgramSearch("2gm.search.bin", ResourceUtils.BIGRAM);
    NgramBoundedReaderSearcher trigram = getNgramSearch("3gm.search.bin", ResourceUtils.TRIGRAM);
    NgramBoundedReaderSearcher fourgram = getNgramSearch("4gm.search.bin", ResourceUtils.FOURGRAM);
    NgramBoundedReaderSearcher fivegram = getNgramSearch("5gm.search.bin", ResourceUtils.FIVEGRAM);

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
      throw new RuntimeException(e); // expect no error
    }
  }

  private static void rewriteTopIfNotExists(Path in, Path out, int top) throws IOException {
    if (Files.notExists(out)) {
      System.out.println("Rewrite to " + out.getFileName());
      Suggestion.rewriteTop(in, out, top);
    }
  }

  private static Path getPart(Path path, int idx) {
    String order = String.format("%02d", idx);
    return path.getParent().resolve(path.getFileName().toString() + order);
  }


  private static List<Path> toParts(Path path, int numParts) {
    List<Path> parts = new ArrayList<>();
    for (int i = 1; i <= numParts; i++) {
      String order = String.format("%02d", i);
      String filename = path.getFileName() + ".part" + order;
      parts.add(path.getParent().resolve(filename));
    }
    return parts;
  }

  private static void genCandidates(Path inputWordPath,
                                    Path outputBinaryPath,
                                    Path outputTop100Path,
                                    Path outputTop10Path,
                                    Path outputTop5Path,
                                    Path outputTop3Path,
                                    Path outputTop1Path,
                                    int numParts,
                                    List<Feature> features)
      throws IOException {
    List<Path> wordParts   = toParts(inputWordPath, numParts);
    List<Path> binParts    = toParts(outputBinaryPath, numParts);
    List<Path> top100Parts = toParts(outputTop100Path, numParts);
    List<Path> top10Parts  = toParts(outputTop10Path, numParts);
    List<Path> top5Parts   = toParts(outputTop5Path, numParts);
    List<Path> top3Parts   = toParts(outputTop3Path, numParts);
    List<Path> top1Parts   = toParts(outputTop1Path, numParts);

    for (int i = 0; i < binParts.size(); i++) {
      Path binPath    = binParts.get(i);
      Path wordPath   = wordParts.get(i);
      Path top100Path = top100Parts.get(i);
      Path top10Path  = top10Parts.get(i);
      Path top5Path   = top5Parts.get(i);
      Path top3Path   = top3Parts.get(i);
      Path top1Path   = top1Parts.get(i);

      if (Files.notExists(binPath)) {
        System.out.println("Generate train suggestions: " + binPath.getFileName());
        List<Word> words = new WordTSVFile(wordParts.get(i)).read();
        List<Suggestion> suggests = Suggestion.suggest(words, features,
            SuggestConstants.SUGGEST_TOP_NUM, false);
        Suggestion.write(suggests, binPath, "suggest");
      }
      rewriteTopIfNotExists(binPath, top100Path, 100);
      rewriteTopIfNotExists(binPath, top10Path,  10);
      rewriteTopIfNotExists(binPath, top5Path,   5);
      rewriteTopIfNotExists(binPath, top3Path,   3);
      rewriteTopIfNotExists(binPath, top1Path,   1);
    }
  }

  public static void main(String arg[]) throws IOException {
    List<Feature> features = constructFeatures();
    /*
    genCandidates(SuggestConstants.DETECT_TRAIN_WORDS,
                  SuggestConstants.TRAIN_BINARY_PATH,
                  SuggestConstants.TRAIN_BINARY_TOP100_PATH,
                  SuggestConstants.TRAIN_BINARY_TOP10_PATH,
                  SuggestConstants.TRAIN_BINARY_TOP5_PATH,
                  SuggestConstants.TRAIN_BINARY_TOP3_PATH,
                  SuggestConstants.TRAIN_BINARY_TOP1_PATH,
                  13,
                  features
        );
        */
    genCandidates(SuggestConstants.DETECT_TEST_WORDS,
                  SuggestConstants.TEST_BINARY_PATH,
                  SuggestConstants.TEST_BINARY_TOP100_PATH,
                  SuggestConstants.TEST_BINARY_TOP10_PATH,
                  SuggestConstants.TEST_BINARY_TOP5_PATH,
                  SuggestConstants.TEST_BINARY_TOP3_PATH,
                  SuggestConstants.TEST_BINARY_TOP1_PATH,
                  4,
                  features
        );
  }
}
