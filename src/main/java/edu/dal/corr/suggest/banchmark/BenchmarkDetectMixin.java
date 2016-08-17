package edu.dal.corr.suggest.banchmark;

import java.util.List;
import java.util.stream.Collectors;

import edu.dal.corr.suggest.Detectable;
import edu.dal.corr.word.Word;

/**
 * @since 2016.08.10
 */
public interface BenchmarkDetectMixin
  extends Detectable
{
  /**
   * Check whether a word is an error and requires further correction.
   * 
   * @param  words  A list of words.
   * @return A list of judgments for word listed in ordered. {@code true} if
   *    this word is an error; {@code false} otherwise.
   */
  default List<Boolean> detect(List<Word> words)
  {
    return words.stream()
        .map(w -> detect(w))
        .collect(Collectors.toList());
  }
}
