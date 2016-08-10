package edu.dal.corr.suggest;

import edu.dal.corr.util.StringSimilarity;
import edu.dal.corr.util.Unigram;
import edu.dal.corr.word.Word;

/**
 * @since 2016.08.10
 */
public class StringSimilarityFeature
  extends AbstractFeature
  implements Feature, ContextInsensitive
{
  private Unigram unigram;
  
  public StringSimilarityFeature()
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
    return (float) StringSimilarity.lcs(word.text(), candidate);
  }
}
