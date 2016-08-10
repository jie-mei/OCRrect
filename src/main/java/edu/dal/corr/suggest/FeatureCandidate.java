package edu.dal.corr.suggest;

import edu.dal.corr.util.TextualUnit;

/**
 * The error candidate provided by a {@link Feature}.
 *
 * @since 2016.07.27
 */
class FeatureCandidate
  extends TextualUnit
{
  private static final long serialVersionUID = 5951851015043707671L;

  private final float score;
  private final Class<? extends Feature> type;
  
  FeatureCandidate(Class<? extends Feature> type, String name, float score)
  {
    super(name);
    this.type = type;
    this.score = score;
  }
  
  Class<? extends Feature> type()
  {
    return type;
  }

  float score()
  {
    return score;
  }
}
