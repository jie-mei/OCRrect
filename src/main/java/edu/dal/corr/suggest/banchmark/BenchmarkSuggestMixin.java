package edu.dal.corr.suggest.banchmark;

import java.util.List;
import java.util.stream.Collectors;

import edu.dal.corr.suggest.Suggestable;
import edu.dal.corr.word.Word;
import gnu.trove.map.TObjectFloatMap;

/**
 * @since 2016.08.10
 */
public interface BenchmarkSuggestMixin
  extends Suggestable
{
  /**
   * Search correction candidates for a list of error words.
   * 
   * @param  words  A list of words.
   * @return A list of candidate string for each word.
   */
  default List<TObjectFloatMap<String>> suggest(List<Word> words)
  {
    return words.stream()
        .map(w -> suggest(w))
        .collect(Collectors.toList());
  }
}
