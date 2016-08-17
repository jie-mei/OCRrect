package edu.dal.corr.suggest;

abstract class AbstractFeature
  implements Feature
{
  @Override
  public NormalizationOption normalize()
  {
    return NormalizationOption.NONE;
  }
}
