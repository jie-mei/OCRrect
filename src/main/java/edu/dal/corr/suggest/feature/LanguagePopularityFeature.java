package edu.dal.corr.suggest.feature;

import edu.dal.corr.suggest.NormalizationOption;
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
  private static final long serialVersionUID = -7219239074897074972L;

  private Unigram unigram;
  
  public LanguagePopularityFeature(String name, Unigram unigram) {
    this.unigram = unigram;
  }

  public LanguagePopularityFeature(Unigram unigram) {
    this(null, unigram);
  }

  @Override
  public boolean detect(Word word) {
    return unigram.contains(word.text());
  }

  @Override
  public float score(Word word, String candidate) {
    return unigram.freq(candidate);
  }

  @Override
  public NormalizationOption normalize() {
    return NormalizationOption.MAX;
  }
}
