package edu.dal.corr.suggest.feature;

import java.io.IOException;

import edu.dal.corr.metric.LCS;
import edu.dal.corr.word.Word;

/**
 * @since 2016.08.10
 */
public class StringSimilarityFeature
    extends WordIsolatedFeature {

  private static final long serialVersionUID = 5387953865452736053L;

  public StringSimilarityFeature() throws IOException {
    super();
  }

  @Override
  public boolean detect(Word word) {
    return getVocab().contains(word.text());
  }

  @Override
  public float score(Word word, String candidate) {
    return (float) LCS.lcs(word.text(), candidate);
  }
}
