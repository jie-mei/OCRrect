package edu.dal.corr.suggest;

import edu.dal.corr.suggest.feature.Feature;
import edu.dal.corr.util.TextualUnit;

/**
 * The error candidate provided by a {@link Feature}.
 *
 * @since 2016.07.27
 */
public class FeatureCandidate
  extends TextualUnit
{
  private static final long serialVersionUID = 5951851015043707671L;

  private final float score;
  private final Feature feature;
  
  FeatureCandidate(Feature feature, String name, float score)
  {
    super(name);
    this.feature = feature;
    this.score = score;
  }
  
  public Feature feature()
  {
    return feature;
  }

  public float score()
  {
    return score;
  }
}
