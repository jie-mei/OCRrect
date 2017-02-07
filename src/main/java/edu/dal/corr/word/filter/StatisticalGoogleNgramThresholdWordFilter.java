package edu.dal.corr.word.filter;

import java.util.ArrayList;
import java.util.List;

import edu.dal.corr.util.Unigram;
import gnu.trove.list.array.TLongArrayList;

/**
 * @since 2016.08.10
 */
public class StatisticalGoogleNgramThresholdWordFilter
  extends AbstractNgramThresholdWordFilter
{
  private static Unigram unigram = Unigram.getInstance();
  
  public StatisticalGoogleNgramThresholdWordFilter(float stddevFactor) {
    this(stddevFactor, (freqsByLen, len) -> freqsByLen.get(len));
  }

  protected StatisticalGoogleNgramThresholdWordFilter(
      float stddevFactor,
      StatisticsSelector selector)
  {
    super(unigram, computeThreshold(stddevFactor, selector));
  }
  
  static float[] computeThreshold(
      float stddevFactor,
      StatisticsSelector selector)
  {
    // Collect frequencies by the length of their according words.
    List<TLongArrayList> freqsByLen = new ArrayList<>();
    for (String gram : unigram.keys()) {
      int len = gram.length();
      TLongArrayList list = null;
      while (list == null) {
        try {
          list = freqsByLen.get(len);
        } catch (IndexOutOfBoundsException e) {
          freqsByLen.add(new TLongArrayList());
        }
      }
      list.add(unigram.freq(gram));
    }
    // Compute the sum of mean and standard deviation of frequencies.
    float[] thresholds = new float[freqsByLen.size() + 1];
    for (int i = 1; i < thresholds.length; i++) {
      try {
        TLongArrayList list = selector.get(freqsByLen, i);
        long mean = mean(list);
        double stddev = stddev(list, mean);
        thresholds[i] = (float)(mean + stddev * stddevFactor);
      } catch (IndexOutOfBoundsException e) {
        thresholds[i] = 0;
      }
    }
    return thresholds;
  }
  
  private static long mean(TLongArrayList vals)
  {
    int sum = 0;
    int num = vals.size();
    for (int i = 0; i < num; i++) {
      sum += vals.get(i);
    }
    return sum / num;
  }
  
  private static double stddev(TLongArrayList vals, long mean)
  {
    long devSum = 0;
    int num = vals.size();
    for (int i = 0; i < num; i++) {
      devSum += Math.pow(vals.get(i) - mean, 2);
    }
    return Math.sqrt(devSum / num);
  }
  
  protected interface StatisticsSelector {
    TLongArrayList get(List<TLongArrayList> freqsByLen, int len);
  }
}
