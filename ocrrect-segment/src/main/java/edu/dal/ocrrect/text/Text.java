package edu.dal.ocrrect.text;

import edu.dal.ocrrect.util.TextualUnit;

import java.util.function.Function;

public class Text extends TextualUnit {
  public Text(String text) {
    super(text);
  }

  public Text process(Function<Text, Text> function) {
    return function.apply(this);
  }
}
