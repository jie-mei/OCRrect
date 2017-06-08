package edu.dal.ocrrect.suggest;

import edu.dal.ocrrect.suggest.feature.Feature;
import edu.dal.ocrrect.suggest.feature.FeatureType;
import edu.dal.ocrrect.util.TextualUnit;

/**
 * The error candidate provided by a {@link Feature}.
 *
 * @since 2017.04.19
 */
public class FeatureCandidate extends TextualUnit {
  private static final long serialVersionUID = 5951851015043707671L;

  private final float score;
  private final FeatureType type;

  FeatureCandidate(FeatureType type, String name, float score) {
    super(name);
    this.type = type;
    this.score = score;
  }

  FeatureCandidate(Feature feature, String name, float score) {
    this(feature.type(), name, score);
  }

  public FeatureType type() {
    return type;
  }

  public float score() {
    return score;
  }
}
