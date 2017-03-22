package edu.dal.corr.suggest.batch;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import edu.dal.corr.suggest.Scoreable;
import edu.dal.corr.word.Word;
import gnu.trove.map.TObjectFloatMap;
import gnu.trove.map.hash.TObjectFloatHashMap;


/**
 * @since 2016.08.10
 */
public interface BatchScoreMixin extends Scoreable {

  default List<TObjectFloatMap<String>> score(
      List<Word> words,
      List<Set<String>> candidateLists)
  {
    List<TObjectFloatMap<String>> scoreLists = new ArrayList<>();
    for (int i = 0; i < words.size(); i++) {
      Word word = words.get(i);
      Set<String> candidates = candidateLists.get(i);
      TObjectFloatMap<String> scores = new TObjectFloatHashMap<>();
      candidates.forEach(c -> {
        scores.put(c, score(word, c));
      });
      scoreLists.add(scores);
    }
    return scoreLists;
  }
}
