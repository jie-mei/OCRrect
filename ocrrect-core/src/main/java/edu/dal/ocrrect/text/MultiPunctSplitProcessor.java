package edu.dal.ocrrect.text;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MultiPunctSplitProcessor implements Processor<TextSegments> {

  private static final Pattern MULTI_PUNCT = Pattern.compile("^[^\\p{Alnum}]{2,}$");

  @Override
  public TextSegments process(TextSegments textSegments) {
    List<Segment> segments = new ArrayList<>();
    for (Segment curr: textSegments) {
      Matcher m = MULTI_PUNCT.matcher(curr.text());
      if (m.matches()) {
        for (int i = 0; i < curr.text().length(); i++) {
          segments.add(new Segment(Character.toString(curr.text().charAt(i)), curr.position() + i));
        }
      } else {
        segments.add(curr);
      }
    }
    return new TextSegments(segments);
  }
}
