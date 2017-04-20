package edu.dal.corr.suggest.batch;

import edu.dal.corr.word.Context;
import edu.dal.corr.word.Word;
import gnu.trove.set.hash.THashSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @since 2017.04.20
 */
public interface ContextSensitiveBatchSearchMixin extends BatchSearchMixin {

  /**
   * Generate search result for contexts with the same first gram.
   *
   * @param first the first gram.
   * @param context a list of context starting which first gram is {@code first}.
   * @return a set of candidates for each context.
   */
  List<Set<String>> search(String first, List<Context> contexts);

  /**
   * Define the size of the suggestion context. This method will be used in context creation during
   * benchmark.
   *
   * @return the size of the n-gram in context.
   */
  int searchContextSize();

  @Override
  default List<Set<String>> search(List<Word> words) {
    // Construct a mapping from word to a set of ngram contexts starting with
    // such word.
    Map<String, List<Context>> wordContextMap = new HashMap<>();
    words.forEach(w -> {
      w.getContexts(searchContextSize()).forEach(c -> {
        String first = c.words()[0];
        List<Context> contextMap = null;
        if ((contextMap = wordContextMap.get(first)) == null) {
          contextMap = new ArrayList<>();
          wordContextMap.put(first, contextMap);
        }
        contextMap.add(c);
      });
    });
    // Create search benchmarks for contexts separated by their first words.
    // Compute using the parallel streaming approach.
    Map<Context, Set<String>> candMap = new HashMap<>();
    wordContextMap.keySet().parallelStream().forEach(str -> {
      List<Context> contextList = wordContextMap.get(str);
      List<Set<String>> candSetList = search(str, contextList);
      for (int i = 0; i < contextList.size(); i++) {
        candMap.put(contextList.get(i), candSetList.get(i));
      }
    });
    // Collect the results.
    return words.stream()
        .map(w ->
            w.getContexts(searchContextSize())
                .stream()
                .map(candMap::get)
                .reduce(new THashSet<String>(), (a, b) -> {
                  // Merge candidates suggested by different contexts.
                  Set<String> merged = new THashSet<>();
                  merged.addAll(a);
                  merged.addAll(b);
                  return merged;
                }))
      .collect(Collectors.toList());
  }

  @Override
  default Set<String> search(Word word) {
    return search(Arrays.asList(word)).get(0);
  }
}
