package edu.dal.ocrrect.suggest;

/**
 * An abstract candidate correctness labeler.
 *
 * @author Jie Mei
 * @since 2017.04.24
 */
public interface Labeler {
  /**
   * Judge whether the candidate word is a correction to the error word.
   *
   * @param error an error word.
   * @param candidate a candidate word.
   * @return {@code true} if the candidate word is a correction of the error word, or {@code false}
   *     otherwise.
   */
  boolean isCorrect(String error, String candidate);
}
