package edu.dal.corr.suggest;

import edu.dal.corr.suggest.feature.FeatureType;
import edu.dal.corr.util.TextualUnit;
import java.io.Serializable;
import java.util.List;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * The error candidate.
 *
 * @since 2017.04.24
 */
public class Candidate extends TextualUnit implements Serializable {
  private static final long serialVersionUID = 1741530240536667097L;

  private final FeatureRegistry featureRegistry;
  private final float[] scores;

  Candidate(String name, FeatureRegistry featureRegistry, float[] scores) {
    super(name);
    if (featureRegistry.types().size() != scores.length) {
      throw new IllegalArgumentException("unequal number of features and scores: "
          + featureRegistry.types().size() + ", " + scores.length);
    }
    this.featureRegistry = featureRegistry;
    this.scores = scores;
  }

  public List<FeatureType> types() {
    return featureRegistry.types();
  }

  public float[] scores() {
    return scores;
  }

  public float[] scores(List<FeatureType> types) {
    float[] out = new float[types.size()];
    for (int i = 0; i < types.size(); i++) {
      out[i] = score(types.get(i));
    }
    return out;
  }

  public float score(FeatureType type) {
    return scores[featureRegistry.getIndex(type)];
  }

  @Override
  protected HashCodeBuilder buildHash() {
    return new HashCodeBuilder().append(scores);
  }
}
