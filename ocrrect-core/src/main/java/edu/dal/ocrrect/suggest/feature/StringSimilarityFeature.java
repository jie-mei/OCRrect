package edu.dal.ocrrect.suggest.feature;

import java.io.IOException;

import edu.dal.ocrrect.metric.LCS;
import edu.dal.ocrrect.word.Word;

/**
 * @since 2017.04.20
 */
public class StringSimilarityFeature extends WordIsolatedFeature {
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
