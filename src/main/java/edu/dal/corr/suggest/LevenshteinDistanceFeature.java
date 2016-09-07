package edu.dal.corr.suggest;

import edu.dal.corr.suggest.banchmark.IsolatedWordBenchmarkDetectMixin;
import edu.dal.corr.word.Word;

/**
 * @since 2016.08.10
 */
public class LevenshteinDistanceFeature
  extends AbstractScoreableFeature
  implements Feature, IsolatedWordBenchmarkDetectMixin
{
  private LevenshteinDistanceScorer levDistance;

  public LevenshteinDistanceFeature()
  {
    levDistance = new LevenshteinDistanceScorer();
  }

  @Override
  public boolean detect(Word word)
  {
    return true;
  }

  @Override
  public float score(Word word, String candidate)
  {
    return 1f / levDistance.score(word, candidate);
  }
}
