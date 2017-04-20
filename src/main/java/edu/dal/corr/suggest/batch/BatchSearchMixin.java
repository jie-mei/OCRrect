package edu.dal.corr.suggest.batch;

import edu.dal.corr.suggest.Searchable;
import edu.dal.corr.word.Word;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @since 2017.04.20
 */
public interface BatchSearchMixin extends Searchable {
  /**
   * Search correction candidates for a list of error words.
   *
   * @param words A list of words.
   * @return A list of candidate string for each word.
   */
  default List<Set<String>> search(List<Word> words) {
    return words
        .parallelStream()
        .map(w -> search(w))
        .collect(Collectors.toList());
  }
}
