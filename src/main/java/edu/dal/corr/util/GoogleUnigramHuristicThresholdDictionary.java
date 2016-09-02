package edu.dal.corr.util;

public class GoogleUnigramHuristicThresholdDictionary
  implements Dictionary
{
  private static GoogleUnigramHuristicThresholdDictionary instance;
  
  public static GoogleUnigramHuristicThresholdDictionary getInstance()
  {
    if (instance == null) {
      instance = new GoogleUnigramHuristicThresholdDictionary();
    }
    return instance;
  }
  
  private Unigram unigram;

  private GoogleUnigramHuristicThresholdDictionary()
  {
    unigram = Unigram.getInstance();
  }

  @Override
  public boolean contains(String word) {
    return unigram.contains(word);
  }
}
