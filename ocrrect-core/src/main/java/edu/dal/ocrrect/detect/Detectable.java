package edu.dal.ocrrect.detect;

import java.util.List;
import java.util.stream.Collectors;

import com.google.common.primitives.Floats;

import edu.dal.ocrrect.util.Word;

/**
 * A interface for quantitative calculation of word correctness.
 *
 * @since 2017.04.20
 */
public interface Detectable {
  /**
   * Quantitative analysis the correctness of a word.
   *
   * @param word A word.
   * @return a float value in the range from 0 to 1.
   */
  float detect(Word word);

  /**
   * Quantitative analysis the correctness of a list of words.
   *
   * @param words a list of word.
   * @return a list of float values in the range from 0 to 1.
   */
  default float[] detect(List<Word> words) {
    List<Float> scores = words
        .parallelStream()
        .map(w -> detect(w))
        .collect(Collectors.toList());
    return Floats.toArray(scores);
  }
}
