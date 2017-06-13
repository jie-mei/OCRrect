package edu.dal.ocrrect.text;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class TextSegmentsConcatProcessor implements Processor<TextSegments> {

  private static final Pattern SPLITTED = Pattern.compile("[^a-zA-Z():;,.\\-'\"!?]");

  @Override
  public TextSegments process(TextSegments textSegments) {
    List<Segment> segments = new ArrayList<>();
    Segment prev = null;
    for (Segment curr: textSegments) {
      if (prev != null
          && prev.position() + prev.text().length() == curr.position()
          && (SPLITTED.matcher(prev.text()).matches()
              || SPLITTED.matcher(curr.text()).matches())) {
        // Override the previous semgent in the list.
//        System.out.printf("CONCAT: %s + %s = %s\n",
//            prev.text(), curr.text(), prev.text() + curr.text());
        segments.set(segments.size() - 1,
            new Segment(prev.text() + curr.text(), prev.position()));
      } else {
        segments.add(curr);
        prev = curr;
      }
    }
    return new TextSegments(segments);
  }
}
