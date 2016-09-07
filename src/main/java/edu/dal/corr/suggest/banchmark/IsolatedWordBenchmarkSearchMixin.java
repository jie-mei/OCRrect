package edu.dal.corr.suggest.banchmark;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import edu.dal.corr.suggest.Features;
import edu.dal.corr.util.LogUtils;
import edu.dal.corr.util.Timer;
import edu.dal.corr.word.Word;

/**
 * @since 2016.08.10
 */
public interface IsolatedWordBenchmarkSearchMixin
  extends BenchmarkSearchMixin
{
  @Override
  default List<List<String>> search(List<Word> words)
  {
    Map<String, List<String>> cache = new ConcurrentHashMap<>();
    return words.parallelStream()
        .map(w -> {
          List<String> result = null;
          if ((result = cache.get(w.text())) == null) {
            if (Features.getLogger().isTraceEnabled()) {
              Timer t = new Timer();
              result = search(w);
              if (LogUtils.isTraceEnabled()) {
                LogUtils.trace(String.format("%10s: %-7d [%6.2f seconds]",
                    w.text(), result.size(), t.interval()));
              }
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
