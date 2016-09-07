package edu.dal.corr.suggest;

import edu.dal.corr.suggest.banchmark.IsolatedWordBenchmarkDetectMixin;
import edu.dal.corr.util.Unigram;
import edu.dal.corr.word.Word;

/**
 * @since 2016.08.10
 */
public class LanguagePopularityFeature
  extends AbstractScoreableFeature
  implements IsolatedWordBenchmarkDetectMixin
{
  private Unigram unigram;
  
  public LanguagePopularityFeature()
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
    return unigram.freq(candidate);
  }

  @Override
  public NormalizationOption normalize() {
    return NormalizationOption.MAX;
  }
}
