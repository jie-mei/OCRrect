package edu.dal.ocrrect.word.dictionary;

import edu.dal.ocrrect.util.Unigram;

public class GoogleUnigramHuristicThresholdDictionary implements Dictionary {
  private static GoogleUnigramHuristicThresholdDictionary instance;

  private Unigram unigram;

  public static GoogleUnigramHuristicThresholdDictionary getInstance() {
    if (instance == null) {
      instance = new GoogleUnigramHuristicThresholdDictionary();
    }
    return instance;
  }

  private GoogleUnigramHuristicThresholdDictionary() {
    unigram = Unigram.getInstance();
  }

  @Override
  public boolean contains(String word) {
    return unigram.contains(word);
  }
}
