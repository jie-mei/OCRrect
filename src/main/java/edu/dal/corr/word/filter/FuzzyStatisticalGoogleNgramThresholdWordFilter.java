package edu.dal.corr.word.filter;

import gnu.trove.list.array.TLongArrayList;

/**
 * @since 2017.04.20
 */
public class FuzzyStatisticalGoogleNgramThresholdWordFilter
  extends StatisticalGoogleNgramThresholdWordFilter {
  public FuzzyStatisticalGoogleNgramThresholdWordFilter(float stddevFactor) {
    super(stddevFactor, (freqsByLen, len) -> {
      TLongArrayList prev = (len == 1
          ? new TLongArrayList()
          : freqsByLen.get(len - 1));
      TLongArrayList curr = freqsByLen.get(len);
      TLongArrayList next = (len == freqsByLen.size() - 1
          ? new TLongArrayList()
          : freqsByLen.get(len));
      TLongArrayList result = new TLongArrayList();
      result.addAll(prev);
      result.addAll(curr);
      result.addAll(next);
      return result;
    });
  }
}
