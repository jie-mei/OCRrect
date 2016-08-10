package edu.dal.corr.suggest;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import edu.dal.corr.util.Timer;
import edu.dal.corr.word.Word;

public interface ContextInsensitive
  extends BenchmarkBehavior
{
  default List<Boolean> detect(List<Word> words)
  {
    Map<String, Boolean> cache = new ConcurrentHashMap<>();
    return words.stream()
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

  default List<List<String>> search(List<Word> words)
  {
    Map<String, List<String>> cache = new ConcurrentHashMap<>();
    return words.parallelStream()
        .map(w -> {
          List<String> result = null;
          if ((result = cache.get(w.text())) == null) {
            if (Features.LOG.isTraceEnabled()) {
              Timer t = new Timer();
              result = search(w);
              Features.LOG.trace(String.format("%10s: %-7d [%6.2f seconds]",
                  w.text(), result.size(), t.interval()));
            } else {
              result = search(w);
            }
            cache.put(w.text(), result);
          }
          return result;
        })
        .collect(Collectors.toList());
  }
}
