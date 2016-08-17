package edu.dal.corr.suggest;

import edu.dal.corr.word.Word;

/**
 * @since 2016.08.11
 */
class LevenshteinDistanceScorer
  implements Scoreable
{
  @Override
  public float score(Word word, String candidate) {
    return LevenshteinDistance.compute(word.text(), candidate);
  }
}
