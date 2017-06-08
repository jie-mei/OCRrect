package edu.dal.ocrrect.suggest.feature;

import java.io.Serializable;

import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * An abstract type of the feature.
 *
 * @author Jie Mei
 * @since 2017.04.23
 */
public class FeatureType implements Serializable {
  private static final long serialVersionUID = 4420562096131275711L;

  private Class<? extends Feature> cls;
  private String name;

  public FeatureType(Class<? extends Feature> featClass, String name) {
    cls = featClass;
    this.name = name;
  }

  public FeatureType(Class<? extends Feature> featClass) {
    this(featClass, null);
  }

  public Class<? extends Feature> featureClass() {
    return cls;
  }

  public void setName(String name) {
    this.name = name;
  }

  /**
   * Get the feature name.
   *
   * <p>The feature name is useful when identifying different feature instance of the same class.
   *
   * @return a string indicates the name of this feature.
   */
  public final String name() {
    return name;
  }

  /**
   * Get the string representation of this feature.
   *
   * @return the class simple name if the feature name is {@code null}. Otherwise the combination of
   *     the class simple name and the feature name.
   */
  @Override
  public final String toString() {
    return name == null
        ? cls.getSimpleName()
        : String.format("%s$%s", cls.getSimpleName(), name());
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder().append(cls).append(name).toHashCode();
  }
}
