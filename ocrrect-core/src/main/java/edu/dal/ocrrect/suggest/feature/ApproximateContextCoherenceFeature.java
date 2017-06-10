package edu.dal.ocrrect.suggest.feature;

import edu.dal.ocrrect.suggest.NgramBoundedReaderSearcher;
import edu.dal.ocrrect.util.Context;
import gnu.trove.map.TObjectFloatMap;
import gnu.trove.map.hash.TObjectFloatHashMap;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @since 2017.04.20
 */
public class ApproximateContextCoherenceFeature extends ContextCoherenceFeature {
  private static final long serialVersionUID = 7781189502953882942L;

  public static final String MATCH_WORD_SUB = " ";
  public static final String RELAX_WORD_SUB = "\t";

  public ApproximateContextCoherenceFeature(String name, NgramBoundedReaderSearcher reader,
      int ngramSize) {
    super(name, reader, ngramSize);
  }

  @Override
  public List<TObjectFloatMap<String>> suggest(String first, List<Context> contexts) {
    // Initialize a hash map storing mappings from skipped ngram context to all its candidates. The
    // map is separated by the position of the skipped gram in the context, in order to increase the
    // searching speed.
    int maxNgramSize = contexts
        .stream()
        .map(c -> c.words().length)
        .max(Comparator.naturalOrder())
        .get();
    List<HashMap<String, TObjectFloatMap<String>>> rsNgramMaps = new ArrayList<>();
    for (int i = 0; i < maxNgramSize; i++) {
      rsNgramMaps.add(new HashMap<>());
    }
    contexts.forEach(c -> {
      String[] words = c.words();
      int idx = c.index();
      relaxedSkipNgrams(words, idx).forEach(rsNgram -> {
        rsNgramMaps.get(idx).putIfAbsent(rsNgram, new TObjectFloatHashMap<>());
      });
    });

    // Check for the existence of n-grams in corpus.
    try (BufferedReader br = reader.openBufferedRecordsWithFirstWord(first)) {
      if (br != null) {
        for (String line = br.readLine(); line != null; line = br.readLine()) {
          String[] splits = line.split("\t");
          String ngram = splits[0];
          float freq = Float.parseFloat(splits[1]);

          // Construct skip-ngram using the ngram in the current reading line. Check the existence
          // of each skip-ngram in map.
          String[] grams = ngram.split(" ");
          for (int i = 0; i < grams.length; i++) {
            HashMap<String, TObjectFloatMap<String>> map = rsNgramMaps.get(i);
            TObjectFloatMap<String> candidateMap = null;
            for (String rsNgram : relaxedSkipNgrams(grams, i)) {
              if ((candidateMap = map.get(rsNgram)) != null) {
                candidateMap.adjustOrPutValue(grams[i], freq, freq);
              }
            }
          }
        }
      } else {
        // Return a list of empty maps.
        return IntStream
            .range(0, contexts.size())
            .mapToObj(i -> new TObjectFloatHashMap<String>())
            .collect(Collectors.toList());
      }
    } catch (IOException e) {
      throw new RuntimeException();
    }

    return IntStream
        .range(0, contexts.size())
        .mapToObj(i -> {
          // Merge candidates from different skip-grams to construct the final result.
          Context context = contexts.get(i);
          int pos = context.index();
          HashMap<String, TObjectFloatMap<String>> map = rsNgramMaps.get(pos);
          return relaxedSkipNgrams(context.words(), pos)
              .stream()
              .map(map::get)
              .reduce(new TObjectFloatHashMap<String>(), (a, b) -> {
                return mergeContextSuggests(a, b);
              });
          })
        .collect(Collectors.toList());
  }

  /**
   * Generate a list of relaxed skip ngram, which is a n-gram with one word replaced by "{@value
   * RELAX_WORD_SUB}" and one word replaced by "{@value MATCH_WORD_SUB}".
   *
   * @param ngram a list of {@code n} gram strings.
   * @param index the index of the pivot (matching) gram in n-gram.
   * @return a concatenated skip-ngram, which pivot gram is skipped and another gram is substituted
   *     by "{@value RELAX_WORD_SUB}".
   */
  private List<String> relaxedSkipNgrams(String[] ngram, int index) {
    String[] skipNgram = ngram.clone();
    skipNgram[index] = MATCH_WORD_SUB;
    return IntStream.range(1, ngram.length)
        .filter(i -> i != index)
        .mapToObj(i -> {
          String[] relaxdSkipNgram = skipNgram.clone();
          relaxdSkipNgram[i] = RELAX_WORD_SUB;
          return String.join(" ", relaxdSkipNgram);
        })
        .collect(Collectors.toList());
  }
}
