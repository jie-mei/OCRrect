package edu.dal.corr.util;

public class GoogleUnigramStatisticalThresholdDictionary implements Dictionary {
  private static GoogleUnigramStatisticalThresholdDictionary instance;

  private Unigram unigram;

  public static GoogleUnigramStatisticalThresholdDictionary getInstance() {
    if (instance == null) {
      instance = new GoogleUnigramStatisticalThresholdDictionary();
    }
    return instance;
  }

  private GoogleUnigramStatisticalThresholdDictionary() {
    unigram = Unigram.getInstance();
  }

  @Override
  public boolean contains(String word) {
    return unigram.contains(word);
  }
}
