package edu.dal.corr.detect;

import edu.dal.corr.word.Word;
import gnu.trove.set.hash.THashSet;

/**
 * @since 2017.04.26
 */
public class WordValidityFeature implements DetectionFeature {
  private THashSet<String> vocab;

  public WordValidityFeature(THashSet<String> vocabulary) {
    vocab = vocabulary;
  }

  @Override
  public float detect(Word word) {
    return vocab.contains(word.text()) ? 1 : 0;
  }
}
