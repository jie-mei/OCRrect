package edu.dal.ocrrect.detect;

import edu.dal.ocrrect.util.Word;
import edu.dal.ocrrect.util.lexicon.Lexicon;
import gnu.trove.set.hash.THashSet;

/**
 * @since 2017.04.26
 */
public class WordValidityFeature extends DetectionFeature {
  private Lexicon vocab;

  public WordValidityFeature(Lexicon vocabulary) {
    vocab = vocabulary;
  }

  @Override
  public float detect(Word word) {
    return vocab.contains(word.getText()) ? 1 : 0;
  }
}
