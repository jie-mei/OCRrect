package edu.dal.ocrrect.suggest.feature;

import edu.dal.ocrrect.suggest.NormalizationOption;
import edu.dal.ocrrect.suggest.Scoreable;
import edu.dal.ocrrect.suggest.Searchable;
import edu.dal.ocrrect.suggest.Suggestable;
import edu.dal.ocrrect.suggest.batch.BatchDetectMixin;
import edu.dal.ocrrect.suggest.batch.BatchScoreMixin;
import edu.dal.ocrrect.suggest.batch.BatchSearchMixin;
import edu.dal.ocrrect.suggest.batch.BatchSuggestMixin;


/**
 * An abstract feature that able to detect error words and suggest candidates corrections. Basic
 * batch processing procedures are available for feature operations.
 *
 * @since 2017.04.22
 */
public abstract class Feature
    implements Detectable,
        Searchable,
        Scoreable,
        Suggestable,
        BatchDetectMixin,
        BatchSearchMixin,
        BatchScoreMixin,
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

  @Override
  public final String toString() {
    return type.toString();
  }
}
