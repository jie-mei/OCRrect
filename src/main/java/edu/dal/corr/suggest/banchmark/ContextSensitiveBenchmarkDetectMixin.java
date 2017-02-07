package edu.dal.corr.suggest.banchmark;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import edu.dal.corr.word.Context;
import edu.dal.corr.word.Word;
import gnu.trove.map.TObjectByteMap;
import gnu.trove.map.hash.TObjectByteHashMap;

/**
 * @since 2016.09.09
 */
public interface ContextSensitiveBenchmarkDetectMixin
  extends BenchmarkDetectMixin
{
  /**
   * Generate detection result for contexts with the same first gram.
   * 
   * @param  first    the first gram.
   * @param  context  a list of context starting which first gram is {@code first}.
   * @return a map from suggested candidates to their according suggestion
   *         confidence values.
   */
  TObjectByteMap<Context> detect(String first, TObjectByteMap<Context> contextMap);

  /**
   * Define the size of the suggestion context. This method will be used in
   * context creation during benchmark.
   * 
   * @return the size of the n-gram in context.
   */
  int detectionContextSize();

  /**
   * Get the final detection decision given context detection results.
   * Implemented class should override this method to modify the detection rule.
   * 
   * @return {@code true} if none of the context result is true. Otherwise,
   *    {@code false}.
   */
  default boolean detectByContextResult(List<Boolean> detected) {
    return detected.stream().noneMatch(b -> b.booleanValue());
  }

  /**
   * This method is applied as a post-processing step for getting string. A
   * implemented class can override this method to modify the post-processing
   * behaviors.
   * 
   * @param  str  A string.
   * @return A normalized representation of a string.
   */
  default Function<String, String> processDetectionString() {
    // return String::toLowerCase;
    return String::toString;
  }

  @Override
  default List<Boolean> detect(List<Word> words)
  {
    // Modify the strings in the input words.
    List<Word> procWords = words.stream()
      .map(w -> w.mapTo(processDetectionString()))
      .collect(Collectors.toList());
    
    // Construct a mapping from word to ngram contexts start with such word.
    Map<String, TObjectByteMap<Context>> wordContextMap = new HashMap<>();
    procWords.forEach(w -> {
      w.getContexts(detectionContextSize()).forEach(c -> {
        String first = c.words()[0];
        TObjectByteMap<Context> contextMap = null;
        if ((contextMap = wordContextMap.get(first)) == null) {
          contextMap = new TObjectByteHashMap<>();
          wordContextMap.put(first, contextMap);
        }
        contextMap.put(c, (byte) 0);
      });
    });
    
    // Create detection benchmarks for contexts separated by their first words.
    // Compute using the parallel streaming approach.
    wordContextMap.keySet().parallelStream().forEach(str -> {
      TObjectByteMap<Context> newMap = detect(str, wordContextMap.get(str));
      wordContextMap.put(str, newMap);
    });
    
    // Collect the results.
    List<Boolean> results = procWords.stream()
      .map(w -> w.getContexts(detectionContextSize()))
      .map(contexts -> {
        List<Boolean> detected = contexts.stream()
          .map(c -> {
            String first = c.words()[0];
            byte result = wordContextMap.get(first).get(c);
            return result == (byte) 1;
          })
          .collect(Collectors.toList());
        return detectByContextResult(detected);
      })
      .collect(Collectors.toList());
    
    return results;
  }

  @Override
  default boolean detect(Word word) {
    return detect(Arrays.asList(word)).get(0);
  }
}
