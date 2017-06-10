package edu.dal.ocrrect.detect;

import edu.dal.ocrrect.util.Word;
import gnu.trove.set.hash.THashSet;

/**
 * @since 2017.04.26
 */
public class WordValidityFeature extends DetectionFeature {
  private THashSet<String> vocab;

  public WordValidityFeature(THashSet<String> vocabulary) {
    vocab = vocabulary;
  }

  @Override
  public float detect(Word word) {
    return vocab.contains(word.text()) ? 1 : 0;
  }
}
