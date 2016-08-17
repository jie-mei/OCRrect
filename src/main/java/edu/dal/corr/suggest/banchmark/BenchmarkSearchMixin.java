package edu.dal.corr.suggest.banchmark;

import java.util.List;
import java.util.stream.Collectors;

import edu.dal.corr.suggest.Searchable;
import edu.dal.corr.word.Word;

/**
 * @since 2016.08.10
 */
public interface BenchmarkSearchMixin
  extends Searchable
{
  /**
   * Search correction candidates for a list of error words.
   * 
   * @param  words  A list of words.
   * @return A list of candidate string for each word.
   */
  default List<List<String>> search(List<Word> words)
  {
    return words.stream()
        .map(w -> search(w))
        .collect(Collectors.toList());
  }
}
