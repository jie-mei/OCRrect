package edu.dal.corr.util;

public class GoogleUnigramDictionary
  implements Dictionary
{
  private static GoogleUnigramDictionary instance;
  
  public static GoogleUnigramDictionary getInstance()
  {
    if (instance == null) {
      instance = new GoogleUnigramDictionary();
    }
    return instance;
  }
  
  private Unigram unigram;

  private GoogleUnigramDictionary()
  {
    unigram = Unigram.getInstance();
  }

  @Override
  public boolean contains(String word) {
    return unigram.contains(word);
  }
}
