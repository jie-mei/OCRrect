package edu.dal.corr.suggest;

import edu.dal.corr.word.Word;

public interface Scoreable
{
  /**
   * Score the candidate string.
   * 
   * @param  word  A word.
   * @param  candidate  A candidate for the given word.
   * @return A score for the specified candidate.
   */
  float score(Word word, String candidate);
}
