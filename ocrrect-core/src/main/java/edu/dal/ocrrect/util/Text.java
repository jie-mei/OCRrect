package edu.dal.ocrrect.util;

import edu.dal.ocrrect.util.TextualUnit;
import edu.dal.ocrrect.util.Word;

import java.util.ArrayList;
import java.util.List;

public class Text extends TextualUnit {

  private List<Word> words;

  public Text(String text) {
    super(text);
    this.words = new ArrayList<>(words);
  }

  public void segment()

  public List<Word> getWords() {
    return words;
  }
}
