package edu.dal.corr.suggest.feature;

import java.io.Serializable;

import edu.dal.corr.suggest.NormalizationOption;
import edu.dal.corr.suggest.Suggestable;
import edu.dal.corr.suggest.banchmark.BenchmarkDetectMixin;
import edu.dal.corr.suggest.banchmark.BenchmarkSuggestMixin;

/**
 * @since 2016.08.10
 */
public abstract class Feature
  implements Detectable, Suggestable, BenchmarkDetectMixin,
      BenchmarkSuggestMixin, Serializable
{
  private static final long serialVersionUID = 9093819739655345866L;

  private String name;

  /**
   * The normalization option applied to the final feature score.
   * @return a normalization option.
   */
  public NormalizationOption normalize() {
    return NormalizationOption.NONE;
  }

  /**
   * Get the feature name.
   * <p>
   * The feature name is useful when identifying different feature instance of
   * the same class.
   * 
   * @return  a string indicates the name of this feature.
   */
  public final String name() {
    return name;
  }
  
  /*
   * Set feature name.
   */
  protected void setName(String name) {
    this.name = name;
  }
  
  /**
   * Get the string representation of this feature.
   * <p>
   * @return the class simple name if the feature name is {@code null}.
   *    Otherwise the combination of the class simple name and the feature name.
   */
  @Override
  public final String toString() {
    return name == null
        ? getClass().getSimpleName()
        : String.format("%s(%s)", getClass().getSimpleName(), name());
  }
}
