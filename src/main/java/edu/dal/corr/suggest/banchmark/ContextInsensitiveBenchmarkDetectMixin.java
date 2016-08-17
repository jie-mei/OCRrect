package edu.dal.corr.suggest.banchmark;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import edu.dal.corr.word.Word;

/**
 * @since 2016.08.10
 */
public interface ContextInsensitiveBenchmarkDetectMixin
  extends BenchmarkDetectMixin
{
  default List<Boolean> detect(List<Word> words)
  {
    Map<String, Boolean> cache = new ConcurrentHashMap<>();
    return words.parallelStream()
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
