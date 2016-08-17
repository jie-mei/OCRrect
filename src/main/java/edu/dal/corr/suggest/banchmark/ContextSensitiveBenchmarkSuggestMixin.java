package edu.dal.corr.suggest.banchmark;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import edu.dal.corr.word.Context;
import edu.dal.corr.word.Word;
import gnu.trove.map.TObjectFloatMap;
import gnu.trove.map.hash.TObjectFloatHashMap;

/**
 * @since 2016.08.10
 */
public interface ContextSensitiveBenchmarkSuggestMixin
  extends BenchmarkSuggestMixin
{
  List<TObjectFloatMap<String>> suggest(String first, List<Context> contexts);

  default TObjectFloatMap<String> suggest(Context context)
  {
    return suggest(context.words()[0], Arrays.asList(context)).get(0);
  }

  /**
   * This method is applied as a post-processing step for getting string. A
   * implemented class can override this method to modify the post-processing
   * behaviors.
   * 
   * @param  str  A string.
   * @return A normalized representation of a string.
   */
  default Function<String, String> processSuggestionString()
  {
    return String::toLowerCase;
  }
  
  /**
   * Merge the {@code <k, v>} pairs from two maps into one. Note that input map
   * could potentially be a {@code null} object.
   * 
   * @param  mapA  A map.
   * @param  mapB  Another map.
   * @return A map which includes all the mappings from both input maps.
   */
  default TObjectFloatMap<String> mergeContextSuggests(
      TObjectFloatMap<String> mapA, TObjectFloatMap<String> mapB)
  {
    TObjectFloatMap<String> merged = new TObjectFloatHashMap<>();
    Arrays.asList(mapA, mapB).forEach(map -> {
      if (map != null) {
        map.keySet().forEach(k -> {
          float freq = map.get(k);
          merged.adjustOrPutValue(k, freq, freq);
        });
      }
    });
    return merged;
  }
  
  @Override
  default List<TObjectFloatMap<String>> suggest(List<Word> words)
  {
    // Modify the strings in the input words.
    List<Word> procWords = words.stream()
        .map(w -> w.mapTo(processSuggestionString()))
        .collect(Collectors.toList());

    // Construct a mapping from word to ngram contexts start with such word.
    Map<String, List<Context>> wordContextMap = new HashMap<>();
    procWords.forEach(w -> {
      w.getContexts().forEach(c -> {
        String first = c.words()[0];
        List<Context> contexts = null;
        if ((contexts = wordContextMap.get(first)) == null) {
          contexts = new ArrayList<>();
          wordContextMap.put(first, contexts);
        }
        contexts.add(c);
      });
    });
    
    // Create search benchmarks for contexts separated by their first words.
    // Compute using the parallel streaming approach.
    Map<Context, TObjectFloatMap<String>> suggestMap = new HashMap<>();
    wordContextMap.keySet().parallelStream().forEach(str -> {
      List<Context> contextList = wordContextMap.get(str);
      List<TObjectFloatMap<String>> mapList = suggest(str, contextList);
      for (int i = 0; i < contextList.size(); i++) {
        suggestMap.put(contextList.get(i), mapList.get(i));
      }
    });

    System.out.println("Generated:");
    
    List<Context> contexts = procWords.stream()
        .flatMap(w -> w.getContexts().stream())
        .collect(Collectors.toList());
    System.out.println(contexts.size());

    System.out.println(suggestMap.keySet().size());
    
    suggestMap.keySet().forEach(k -> {
      System.out.println("\t" + k + " -> " + k.index() + " " + contexts.contains(k));

      TObjectFloatMap<String> map = suggestMap.get(k);
      map.keySet().forEach(c -> System.out.println("\t\t" + c + ":" + map.get(c) + ", "));
      System.out.println();
    });
    
    // Collect the results.
    return procWords.stream()
        .map(w -> w.getContexts().stream()
            .map(suggestMap::get)
            .reduce(new TObjectFloatHashMap<String>(), (a, b) -> {
              return mergeContextSuggests(a, b);
            })
        )
        .collect(Collectors.toList());
  }

  @Override
  default TObjectFloatMap<String> suggest(Word word)
  {
    return suggest(Arrays.asList(word)).get(0);
  }
}
