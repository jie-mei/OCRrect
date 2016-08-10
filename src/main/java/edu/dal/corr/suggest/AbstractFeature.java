package edu.dal.corr.suggest;

import java.util.List;

import edu.dal.corr.word.Word;

abstract class AbstractFeature
  implements Feature
{
  private static int DISTANCE_THRESHOLD = 3;

  private LevenshteinDistance ld;
  
  AbstractFeature()
  {
    ld = new LevenshteinDistance(DISTANCE_THRESHOLD);
  }
  
  protected LevenshteinDistance levenshtein()
  {
    return ld;
  }

  @Override
  public List<String> search(Word word) {
    return ld.search(word.text());
  }
}
