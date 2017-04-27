package edu.dal.corr.detect;

import edu.dal.corr.word.Word;

/**
 * A interface for quantitive calculation of word correctness.
 * 
 * @since 2017.04.20
 */
public interface DetectionFeature {
  /**
   * Quantitive analysis the correctness of a word.
   *
   * @param word A word.
   * @return a float value in the range from 0 to 1.
   */
  float detect(Word word);
}
