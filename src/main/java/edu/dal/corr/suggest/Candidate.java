package edu.dal.corr.suggest;

import java.io.Serializable;
import java.util.List;

import org.apache.commons.lang.builder.HashCodeBuilder;

import edu.dal.corr.suggest.feature.Feature;
import edu.dal.corr.suggest.feature.FeatureType;
import edu.dal.corr.util.TextualUnit;
import gnu.trove.map.hash.TObjectIntHashMap;

/**
 * The error candidate.
 *
 * @since 2016.08.10
 */
public class Candidate
  extends TextualUnit
  implements Serializable
{
  private static final long serialVersionUID = 1741530240536667097L;

  private final TObjectIntHashMap<FeatureType> featMap;
  private final float[] scores;
  
  Candidate(String name,
            TObjectIntHashMap<FeatureType> featMap,
            float[] scores) {
  	super(name);
  	this.featMap = featMap;
  	this.scores = scores;
  }
  
  public float[] score() {
    return scores;
  }

  public float[] score(List<FeatureType> types) {
    float[] out = new float[types.size()];
    for (int i = 0; i < types.size(); i++) {
      out[i] = score(types.get(i));
    }
    return out;
  }
  
  public float score(FeatureType type) {
    return scores[featMap.get(type)];
  }
  
  public float score(Feature feature) {
    return score(feature.type());
  }
  
  @Override
  protected HashCodeBuilder buildHash() {
    return new HashCodeBuilder().append(scores);
  }
}
