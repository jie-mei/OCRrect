package edu.dal.corr.suggest;

import edu.dal.corr.suggest.banchmark.ContextInsensitiveBenchmarkDetectMixin;
import edu.dal.corr.util.Unigram;
import edu.dal.corr.word.Word;

/**
 * @since 2016.08.10
 */
public class StringSimilarityFeature
  extends AbstractScoreableFeature
  implements Feature, ContextInsensitiveBenchmarkDetectMixin
{
  private Unigram unigram;
  
  public StringSimilarityFeature()
  {
    unigram = Unigram.getInstance();
  }

  @Override
  public boolean detect(Word word)
  {
    return unigram.contains(word.text());
  }

  @Override
  public float score(Word word, String candidate)
  {
    return (float) StringSimilarityScorer.lcs(word.text(), candidate);
  }
}
