package edu.dal.corr.suggest;

import java.io.Serializable;

import org.apache.commons.lang.builder.HashCodeBuilder;

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
  
  public void printTypeMap() {
    System.out.println("MAP SIZE: " + typeMap.keySet().size());
    for (Class<? extends Feature> type : typeMap.keySet()) {
      System.out.println(type + " " + typeMap.get(type));
    }
  }
  
  public float score(Class<? extends Feature> feature)
  {
    return scores[typeMap.get(feature)];
  }
  
  @Override
  protected HashCodeBuilder buildHash() {
    return new HashCodeBuilder().append(scores);
  }
}
