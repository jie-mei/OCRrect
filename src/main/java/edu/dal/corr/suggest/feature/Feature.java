package edu.dal.corr.suggest.feature;

import edu.dal.corr.suggest.NormalizationOption;
import edu.dal.corr.suggest.Scoreable;
import edu.dal.corr.suggest.Searchable;
import edu.dal.corr.suggest.Suggestable;
import edu.dal.corr.suggest.batch.BatchDetectMixin;
import edu.dal.corr.suggest.batch.BatchScoreMixin;
import edu.dal.corr.suggest.batch.BatchSearchMixin;
import edu.dal.corr.suggest.batch.BatchSuggestMixin;


/**
 * An abstract feature that able to detect error words and suggest candidates corrections.
 * Basic batch processing procedures are available for feature operations.
 *
 * @since 2017.03.22
 */
public abstract class Feature
    implements Detectable, Searchable, Scoreable, Suggestable,
               BatchDetectMixin, BatchSearchMixin, BatchScoreMixin,
               BatchSuggestMixin {

  private static final long serialVersionUID = 8323543739086967501L;

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
   * 
   * @return a normalization option.
   */
  public NormalizationOption normalize() {
    return NormalizationOption.RESCALE;
  }
}
