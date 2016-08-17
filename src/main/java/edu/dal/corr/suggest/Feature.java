package edu.dal.corr.suggest;

import edu.dal.corr.suggest.banchmark.BenchmarkDetectMixin;
import edu.dal.corr.suggest.banchmark.BenchmarkSuggestMixin;

/**
 * @since 2016.08.10
 */
public interface Feature
  extends Detectable, Suggestable, BenchmarkDetectMixin, BenchmarkSuggestMixin
{
  NormalizationOption normalize();
}
