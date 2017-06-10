package edu.dal.ocrrect.word.filter;

import edu.dal.ocrrect.util.Unigram;
import edu.dal.ocrrect.util.Word;

/**
 * @since 2017.04.20
 */
public class AbstractNgramThresholdWordFilter implements WordFilter {
  private float[] thresholds;
  private Unigram unigram;

  public AbstractNgramThresholdWordFilter(Unigram unigram, float[] thresholds) {
    this.unigram = unigram;
    this.thresholds = thresholds;
  }

  @Override
  public boolean filter(Word word) {
    String name = word.text();
    int len = name.length();
    if (len < thresholds.length) {
      return unigram.freq(name) > thresholds[len];
    }
    return false;
  }
}
