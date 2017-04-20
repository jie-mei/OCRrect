package edu.dal.corr.suggest.feature;

import edu.dal.corr.suggest.NormalizationOption;
import edu.dal.corr.suggest.Scoreable;
import edu.dal.corr.word.Word;
import java.io.IOException;

/**
 * @since 2017.04.20
 */
public class DistanceFeature extends WordIsolatedFeature {
  private static final long serialVersionUID = -433050428364692186L;

  private Scoreable scoreable;
  private NormalizationOption norm;

  public DistanceFeature(String name, Scoreable scoreable, NormalizationOption norm)
      throws IOException {
    setName(name);
    this.scoreable = scoreable;
    this.norm = norm;
  }

  public DistanceFeature(String name, Scoreable scoreable) throws IOException {
    this(name, scoreable, NormalizationOption.RESCALE_AND_NEGATE);
  }

  @Override
  public boolean detect(Word word) {
    return true;
  }

  /**
   * Return the scaled feature score.
   *
   * <p>The score is scaled using {@code distance / max_distance}.
   */
  @Override
  public float score(Word word, String candidate) {
    return scoreable.score(word, candidate);
  }

  public NormalizationOption normalize() {
    return norm;
  }
}
