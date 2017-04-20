package edu.dal.corr.suggest;

/**
 * @since 2017.04.20
 */
public enum NormalizationOption {
  NONE,
  DIVIDE_MAX,
  RESCALE,
  RESCALE_AND_NEGATE,
  LOG_AND_RESCALE,
  TO_PROB
}
