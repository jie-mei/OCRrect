package edu.dal.corr.suggest;

import java.util.List;

import edu.dal.corr.word.Word;

/**
 * @since 2016.08.10
 */
public interface Feature
{
  /**
   * Check whether a word is an error and requires further correction.
   * 
   * @param  word  A word.
   * @return {@code true} if this word is an error; {@code false} otherwise.
   */
  boolean detect(Word word);

  /**
   * Search correction candidates for an error word.
   * 
   * @param  word  A word.
   * @return A list of candidate string.
   */
  List<String> search(Word word);

  /**
   * Score the candidate string.
   * 
   * @param  word  A word.
   * @param  candidate  A candidate for the given word.
   * @return A score for the specified candidate.
   */
  float score(Word word, String candidate);
}
