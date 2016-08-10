package edu.dal.corr.suggest;

import edu.dal.corr.util.Unigram;
import edu.dal.corr.word.Word;

public class LanguagePopularityFeature
  extends AbstractFeature
  implements Feature, Normalized, ContextInsensitive
{
  private Unigram unigram;
  
  public LanguagePopularityFeature()
  {
    super();
    unigram = Unigram.getInstance();
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
  public Normalization normalize() {
    return Normalization.MAX;
  }
}
