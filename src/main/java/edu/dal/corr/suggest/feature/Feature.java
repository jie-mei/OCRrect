package edu.dal.corr.suggest.feature;

import edu.dal.corr.suggest.NormalizationOption;
import edu.dal.corr.suggest.Suggestable;
import edu.dal.corr.suggest.banchmark.BenchmarkDetectMixin;
import edu.dal.corr.suggest.banchmark.BenchmarkSuggestMixin;

/**
 * @since 2016.08.10
 */
public abstract class Feature
  implements Detectable, Suggestable, BenchmarkDetectMixin,
      BenchmarkSuggestMixin
{
  private FeatureType type;
  
  public Feature(String name) {
    this.type = new FeatureType(this.getClass(), name);
  }
  
  public Feature() {
    this(null);
  }
  
  public void setName(String name) {
    this.type.setName(name);
  }
  
  public FeatureType type() {
    return type;
  }

  /**
   * The normalization option applied to the final feature score.
   * @return a normalization option.
   */
  public NormalizationOption normalize() {
    return NormalizationOption.NONE;
  }
}
