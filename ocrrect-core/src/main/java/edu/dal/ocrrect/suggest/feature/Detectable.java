package edu.dal.ocrrect.suggest.feature;

import edu.dal.ocrrect.word.Word;

/**
 * @since 2017.04.20
 */
public interface Detectable {
  /**
   * Check whether a word is an error and requires further correction.
   *
   * @param word A word.
   * @return {@code true} if this word is an error; {@code false} otherwise.
   */
  boolean detect(Word word);
}
