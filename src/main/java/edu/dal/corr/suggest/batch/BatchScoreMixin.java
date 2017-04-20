package edu.dal.corr.suggest.batch;

import edu.dal.corr.suggest.Scoreable;
import edu.dal.corr.word.Word;
import gnu.trove.map.TObjectFloatMap;
import gnu.trove.map.hash.TObjectFloatHashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @since 2017.04.20
 */
public interface BatchScoreMixin extends Scoreable {

  default List<TObjectFloatMap<String>> score(List<Word> words, List<Set<String>> candidateLists) {
    return IntStream
        .range(0, words.size())
        .parallel()
        .mapToObj(i -> {
          Word word = words.get(i);
          Set<String> candidates = candidateLists.get(i);
          TObjectFloatMap<String> scores = new TObjectFloatHashMap<>();
          candidates.forEach(c -> {
            scores.put(c, score(word, c));
          });
          return scores;
        })
        .collect(Collectors.toList());
  }
}
