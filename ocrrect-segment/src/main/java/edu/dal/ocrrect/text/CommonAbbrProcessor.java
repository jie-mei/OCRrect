package edu.dal.ocrrect.text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;

public class CommonAbbrProcessor implements Processor<TextSegments> {

  private static final HashSet<String> ABBR = new HashSet<>(Arrays.asList(
      // volume
      "Vol",
      // page
      "p", "pp", "PP",
      // title
      "Mr.", "Dr.", "Prof.",
      // edition
      "Ed.",
      // month
      "Jan.", "Feb.", "Mar.", "Apr.", "Jun.", "Jul.", "Aug.", "Sep.", "Oct.", "Nov.", "Dec.",
      // time
      "a.m.", "p.m.",
      // etc
      "etc.",
      "St."
  ));

  @Override
  public TextSegments process(TextSegments textSegments) {
    List<Segment> segments = new ArrayList<>();
    Segment prev = null;
    for (Segment curr: textSegments) {
      if (curr.text().equals(".") && ABBR.contains(prev.text())
          && prev.position() + prev.text().length() == curr.position()) {
        // Update the previously added semgent in the list.
        prev = new Segment(prev.text() + curr.text(), prev.position());
        segments.set(segments.size() - 1, prev);
      } else {
        segments.add(curr);
        prev = curr;
      }
    }
    return new TextSegments(segments);
  }
}
