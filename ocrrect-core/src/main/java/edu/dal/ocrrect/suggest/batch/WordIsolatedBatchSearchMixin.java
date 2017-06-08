package edu.dal.ocrrect.suggest.batch;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import edu.dal.ocrrect.word.Word;

/**
 * @since 2017.04.20
 */
public interface WordIsolatedBatchSearchMixin
  extends BatchSearchMixin {

  /**
   * Avoid re-computation for words with the identical text representation.
   */
  @Override
  default List<Set<String>> search(List<Word> words) {
    Map<String, Set<String>> cache = new ConcurrentHashMap<>();
    return words
        .parallelStream()
        .map(w -> {
          Set<String> result = null;
          if ((result = cache.get(w.text())) == null) {
            result = search(w);
            cache.put(w.text(), result);
          }
          return result;
        })
        .collect(Collectors.toList());
  }
}
