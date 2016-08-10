package edu.dal.corr.suggest;

import java.util.List;

import edu.dal.corr.word.Word;

public interface BenchmarkBehavior
    extends Feature
{
  /**
   * Check whether a word is an error and requires further correction.
   * 
   * @param  words  A list of words.
   * @return A list of judgments for word listed in ordered. {@code true} if
   *    this word is an error; {@code false} otherwise.
   */
  List<Boolean> detect(List<Word> words);

  /**
   * Search correction candidates for a list of error words.
   * 
   * @param  words  A list of words.
   * @return A list of candidate string for each word.
   */
  List<List<String>> search(List<Word> words);
}
