package edu.dal.ocrrect.suggest.batch;

import edu.dal.ocrrect.suggest.Suggestable;
import edu.dal.ocrrect.word.Word;
import gnu.trove.map.TObjectFloatMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @since 2017.04.20
 */
public interface BatchSuggestMixin extends Suggestable {
  /**
   * Suggest correction candidates for a list of error words.
   *
   * @param  words  A list of words.
   * @return A list of candidate string for each word.
   */
  default List<TObjectFloatMap<String>> suggest(List<Word> words) {
    return words
        .parallelStream()
        .map(w -> suggest(w))
        .collect(Collectors.toList());
  }
}
