package edu.dal.ocrrect.text;

import edu.dal.ocrrect.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class SplitSegmenter extends PennTreebankSegmenter {

  private final static Pattern SPLIT_PATTERN = Pattern.compile("([^-]+|-)");

  private enum CharType {
    LETTER, DIGIT, PUNCTUATION, WHITESPACE, SOF, EOF
  }

  private CharType getCharType(char c) {
    if (Character.isLetter(c)) {
      return CharType.LETTER;
    } else if (Character.isDigit(c)) {
      return CharType.DIGIT;
    } else if (Character.isWhitespace(c)) {
      return CharType.WHITESPACE;
    } else {
      return CharType.PUNCTUATION;
    }
  }

  @Override
  public TextSegments segment(Text text) {
    List<Segment> segments = new ArrayList<>();
    CharType prev = CharType.SOF;
    String cache = "";
    int pos = 0;
    for (int i = 0; i < text.text().length(); i++) {
      char c = text.text().charAt(i);
      CharType curr = (i == text.text().length() - 1 ? CharType.EOF: getCharType(c));
      if (curr != prev) {
        switch (prev) {
          case LETTER:
          case DIGIT:
              segments.add(new Segment(cache, pos));
              break;
          case PUNCTUATION:
              for (int j = 0; j < cache.length(); j++) {
                segments.add(new Segment(cache.charAt(j) + "", pos + j));
              }
              break;
          case WHITESPACE:
          default:
        }
        pos += cache.length();
        cache = "";
      }
      cache += c;
      prev = curr;
    }
    return new TextSegments(segments);
  }
}
