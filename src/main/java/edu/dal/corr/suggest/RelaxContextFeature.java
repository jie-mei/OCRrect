package edu.dal.corr.suggest;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import edu.dal.corr.suggest.banchmark.ContextSensitiveBenchmarkDetectMixin;
import edu.dal.corr.suggest.banchmark.ContextSensitiveBenchmarkSuggestMixin;
import edu.dal.corr.word.Context;
import gnu.trove.map.TObjectFloatMap;
import gnu.trove.map.hash.TObjectFloatHashMap;

/**
 * @since 2016.08.10
 */
public class RelaxContextFeature
  extends ExactContextFeature
  implements ContextSensitiveBenchmarkDetectMixin,
             ContextSensitiveBenchmarkSuggestMixin
{
  public RelaxContextFeature(NgramBoundedReaderSearcher searcher)
  {
    super(searcher);
  }
  
  public RelaxContextFeature(List<Path> ngrams)
    throws FileNotFoundException, IOException
  {
    this(new NgramBoundedReaderSearcher(ngrams));
  }

  @Override
  public List<TObjectFloatMap<String>> suggest(String first,
      List<Context> contexts)
  {
    // Initialize a hash map storing mappings from skipped ngram context to all
    // its candidates. The map is separated by the position of the skipped gram
    // in the context, in order to increase the searching speed.
    int maxNgramSize = contexts.stream().map(c -> c.words().length)
        .max(Comparator.naturalOrder()).get();
    List<HashMap<String, TObjectFloatMap<String>>> rsNgramMaps = new ArrayList<>();
    for (int i = 0; i < maxNgramSize; i++) {
      rsNgramMaps.add(new HashMap<>());
    }
    contexts.forEach(c -> {
      String[] words = c.words();
      int idx = c.index();
      relaxedSkipNgrams(words, idx).forEach(rsNgram -> {
        rsNgramMaps.get(idx).putIfAbsent(
            rsNgram,
            new TObjectFloatHashMap<>());
      });
    });

    // Check for the existence of n-grams in corpus.
    try (BufferedReader br = searcher.openBufferedRecordsWithFirstWord(first)) {
      if (br != null) {
        for (String line = br.readLine(); line != null; line = br.readLine()) {
          String[] splits = line.split("\t");
          String ngram = splits[0];
          float freq = Float.parseFloat(splits[1]);
          
          // Construct skip-ngram using the ngram in the current reading line.
          // Check the existence of each skip-ngram in map.
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
        return IntStream.range(0, contexts.size())
            .mapToObj(i -> new TObjectFloatHashMap<String>())
            .collect(Collectors.toList());
      }
    } catch (IOException e) {
      throw new RuntimeException();
    }

    return IntStream.range(0, contexts.size())
        .mapToObj(i -> {
          // Merge candidates from different skip-grams to construct the final
          // result.
          Context context = contexts.get(i);
          int pos = context.index();
          HashMap<String, TObjectFloatMap<String>> map = rsNgramMaps.get(pos);
          return relaxedSkipNgrams(context.words(), pos).stream()
              .map(map::get)
              .reduce(new TObjectFloatHashMap<String>(), (a, b) -> {
                return mergeContextSuggests(a, b);
              });
        })
        .collect(Collectors.toList());
  }
  
  /**
   * Generate a list of relaxed skip ngram, which is a n-gram with one word
   * replaced by a whitespace character and one word replaced by a tab
   * character.
   * 
   * @param  ngram     A list of {@code n} gram strings.
   * @param  position  The index of the skipped gram in ngram array.
   * @return A concatenated skip-ngram, which skipped gram is replaced by an
   *    space character.
   */
  private List<String> relaxedSkipNgrams(String[] ngram, int position)
  {
    String[] skipNgram = ngram.clone();
    skipNgram[position] = " ";
    return IntStream.range(0, ngram.length)
        .filter(i -> i != position)
        .mapToObj(i -> {
          String[] relaxdSkipNgram = skipNgram.clone();
          relaxdSkipNgram[i] = "\t";
          return String.join(" ", relaxdSkipNgram);
        })
        .collect(Collectors.toList());
  }
}
