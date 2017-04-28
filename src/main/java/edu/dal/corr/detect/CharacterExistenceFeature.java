package edu.dal.corr.detect;

import edu.dal.corr.word.Word;

/**
 * @since 2017.04.26
 */
public class CharacterExistenceFeature extends DetectionFeature {
  private char chr;

  public CharacterExistenceFeature(char character) {
    chr = character;
    setName(Character.toString(chr));
  }

  @Override
  public float detect(Word word) {
    return word.text().indexOf(chr) >= 0 ? 1 : 0;
  }
}
