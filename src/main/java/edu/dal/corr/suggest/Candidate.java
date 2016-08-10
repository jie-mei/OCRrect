package edu.dal.corr.suggest;

import java.io.Serializable;

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

  private final TObjectIntHashMap<Class<? extends Feature>> typeMap;
  private final float[] scores;
  
  Candidate(String name, TObjectIntHashMap<Class<? extends Feature>> typeMap,
      float[] scores)
  {
  	super(name);
  	this.typeMap = typeMap;
  	this.scores = scores;
  }
  
  public float[] score()
  {
    return scores;
  }
  
  public float score(Class<Feature> feature)
  {
    return scores[typeMap.get(feature)];
  }
}
