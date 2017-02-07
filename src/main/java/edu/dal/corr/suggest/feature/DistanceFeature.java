package edu.dal.corr.suggest.feature;

import edu.dal.corr.suggest.banchmark.IsolatedWordBenchmarkDetectMixin;
import edu.dal.corr.word.Word;

/**
 * @since 2016.08.10
 */
public class DistanceFeature
  extends AbstractScoreableFeature
  implements IsolatedWordBenchmarkDetectMixin
{
  private static final long serialVersionUID = -433050428364692186L;
  
  private Scoreable scoreable;
  private boolean rescale;
  
  public DistanceFeature(String name, Scoreable scoreable, boolean rescale) {
    setName(name);
    this.scoreable = scoreable;
    this.rescale = rescale;
  }

  @Override
  public boolean detect(Word word) {
    return true;
  }

  /**
   * Return the scaled feature score.
   * <p>
   * The score is scaled using {@code distance / max_distance}.
   */
  @Override
  public float score(Word word, String candidate) {
    return rescale
        ? scoreable.score(word, candidate) / (float)DISTANCE_THRESHOLD
        : scoreable.score(word, candidate);
  }
}