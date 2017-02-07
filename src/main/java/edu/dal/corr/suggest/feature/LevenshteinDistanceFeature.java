package edu.dal.corr.suggest.feature;

import edu.dal.corr.metric.EditDistance;
import edu.dal.corr.suggest.banchmark.IsolatedWordBenchmarkDetectMixin;
import edu.dal.corr.word.Word;

/**
 * @since 2016.08.10
 */
public class LevenshteinDistanceFeature
  extends AbstractScoreableFeature
  implements IsolatedWordBenchmarkDetectMixin
{
  private static final long serialVersionUID = -433050428364692186L;

  @Override
  public boolean detect(Word word) {
    return true;
  }

  /**
   * Return the scaled feature score.
   * <p>
   * The score is scaled using {@code (max_distance - distance) / max_distance}.
   */
  @Override
  public float score(Word word, String candidate)
  {
    return (DISTANCE_THRESHOLD - EditDistance.levDist(word.text(), candidate)) /
        (float)DISTANCE_THRESHOLD;
  }
}
