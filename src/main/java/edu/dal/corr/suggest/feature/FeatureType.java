package edu.dal.corr.suggest.feature;

public class FeatureType
{
  public Class<? extends Feature> cls;
  public String name;

  public FeatureType(Feature feature) {
    cls = feature.getClass();
    name = feature.name();
  }
  
  public Class<? extends Feature> featureClass() {
    return cls;
  }
  
  public String featureName() {
    return name;
  }
  
  @Override
  public String toString() {
    return String.format("%s(%s)", cls.getSimpleName(), name);
  }
}
