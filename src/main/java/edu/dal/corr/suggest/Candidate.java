package edu.dal.corr.suggest;

import java.io.Serializable;

import org.apache.commons.lang.builder.HashCodeBuilder;

import edu.dal.corr.suggest.feature.Feature;
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

  private final TObjectIntHashMap<Feature> featMap;
  private final float[] scores;
  
  Candidate(String name, TObjectIntHashMap<Feature> featMap,
      float[] scores)
  {
  	super(name);
  	this.featMap = featMap;
  	this.scores = scores;
  }
  
  public float[] score()
  {
    return scores;
  }
  
  public float score(Feature feature)
  {
    return scores[featMap.get(feature)];
  }
  
  @Override
  protected HashCodeBuilder buildHash() {
    return new HashCodeBuilder().append(scores);
  }
}
