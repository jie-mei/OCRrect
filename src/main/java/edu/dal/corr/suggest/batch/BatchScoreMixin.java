package edu.dal.corr.suggest.batch;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import edu.dal.corr.DocumentCorrector;
import edu.dal.corr.suggest.Scoreable;
import edu.dal.corr.util.LogUtils;
import edu.dal.corr.util.Timer;
import edu.dal.corr.word.Word;
import gnu.trove.map.TObjectFloatMap;
import gnu.trove.map.hash.TObjectFloatHashMap;


/**
 * @since 2017.03.23
 */
public interface BatchScoreMixin extends Scoreable {

  default List<TObjectFloatMap<String>> score(
      List<Word> words,
      List<Set<String>> candidateLists)
  {
    return IntStream
        .range(0, words.size())
        .parallel()
        .mapToObj(i -> {
          Timer t = new Timer().start();
          Word word = words.get(i);
          Set<String> candidates = candidateLists.get(i);
          TObjectFloatMap<String> scores = new TObjectFloatHashMap<>();
          candidates.forEach(c -> {
            scores.put(c, score(word, c));
          });
          LogUtils.info(String.format(
              "%20s: %d candidates [%6.3f second]",
              word.text(), candidates.size(), t.interval()));
          return scores;
        })
        .collect(Collectors.toList());
  }
}
