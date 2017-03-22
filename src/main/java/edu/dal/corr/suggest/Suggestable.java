package edu.dal.corr.suggest;

import edu.dal.corr.word.Word;
import gnu.trove.map.TObjectFloatMap;
import gnu.trove.map.hash.TObjectFloatHashMap;


/**
 * @since 2016.08.10
 */
public interface Suggestable
    extends Searchable, Scoreable {

  /**
   * Suggest correction candidates for an error word.
   * 
   * @param word A word.
   * @return A feature suggestion.
   */
  default TObjectFloatMap<String> suggest(Word word) {
    TObjectFloatMap<String> suggestMap = new TObjectFloatHashMap<>();
    search(word).stream().forEach(candidate -> {
      suggestMap.put(candidate, score(word, candidate));
    });
    return suggestMap;
  }
}
