package edu.dal.corr.suggest.feature;

import edu.dal.corr.word.Word;

/**
 * @since 2016.08.10
 */
public interface Detectable
{
  /**
   * Check whether a word is an error and requires further correction.
   * 
   * @param  word  A word.
   * @return {@code true} if this word is an error; {@code false} otherwise.
   */
  boolean detect(Word word);
}
