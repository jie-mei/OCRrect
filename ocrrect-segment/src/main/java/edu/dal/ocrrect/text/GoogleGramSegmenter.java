package edu.dal.ocrrect.text;

import edu.dal.ocrrect.util.Token;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GoogleGramSegmenter extends PennTreebankSegmenter {
  private final static Pattern SPLIT_PATTERN = Pattern.compile("([^-]+|-)");

  @Override
  public List<Token> segment(Text text) {
    List<Token> tokens = new ArrayList<>();
    for (Token tk : super.segment(text)) {
      Matcher m = SPLIT_PATTERN.matcher(tk.text());
      while (m.find()) {
        System.out.println(m.group());
        tokens.add(new Token(m.group(), tk.position() + m.start()));
      }
    }
    return tokens;
  }
}
