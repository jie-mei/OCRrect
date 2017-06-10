package edu.dal.ocrrect.suggest.batch;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import edu.dal.ocrrect.util.Word;

/**
 * @since 2017.04.20
 */
public interface WordIsolatedBatchDetectMixin
  extends BatchDetectMixin
{
  /**
   * Avoid re-computation for words with the identical text representation.
   */
  default List<Boolean> detect(List<Word> words) {
    Map<String, Boolean> cache = new ConcurrentHashMap<>();
    return words
        .parallelStream()
        .map(w -> {
          Boolean result = null;
          if ((result = cache.get(w.text())) == null) {
            result = detect(w);
            cache.put(w.text(), result);
          }
          return result;
        })
        .collect(Collectors.toList());
  }
}
