package edu.dal.corr.suggest;

import edu.dal.corr.word.Word;

public class LevenshteinDistanceFeature
  extends AbstractFeature
  implements Feature, ContextInsensitive
{
  public LevenshteinDistanceFeature()
  {
    super();
  }

  @Override
  public boolean detect(Word word) {
    return true;
  }

  @Override
  public float score(Word word, String candidate) {
    return 1f / levenshtein().distance(word.text(), candidate);
  }
}
