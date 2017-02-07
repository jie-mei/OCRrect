package edu.dal.corr.suggest.feature;

import edu.dal.corr.metric.LCS;
import edu.dal.corr.suggest.banchmark.IsolatedWordBenchmarkDetectMixin;
import edu.dal.corr.util.Unigram;
import edu.dal.corr.word.Word;

/**
 * @since 2016.08.10
 */
public class StringSimilarityFeature
  extends AbstractScoreableFeature
  implements IsolatedWordBenchmarkDetectMixin
{
  private static final long serialVersionUID = 5387953865452736053L;

  private Unigram unigram;
  
  public StringSimilarityFeature(Unigram unigram) {
    this.unigram = unigram;
  }

  @Override
  public boolean detect(Word word)
  {
    return unigram.contains(word.text());
  }

  @Override
  public float score(Word word, String candidate) {
    return (float) LCS.lcs(word.text(), candidate);
  }
}
