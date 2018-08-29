package edu.dal.ocrrect.text;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class TextSegmentsConcatProcessor implements Processor<TextSegments> {

  private static final Pattern PUNCT_COMMON = Pattern.compile("[:;,.!?]");
  private static final Pattern PUNCT_RHALF = Pattern.compile("[)'\"]");
  private static final Pattern FRAGMENT = Pattern.compile("[^0-9a-zA-Z():;,.\\-'\"!?*]");

  private boolean isPunctuation(Segment prev, Segment curr, Segment next) {
    switch (curr.text()) {
      case ":": case ";": case ".": case ",": case "!": case "?":
        System.out.println("case1: " + prev.text() + " " + curr.text() + " " +
            (next == null ? "" : next.text()));
        return
          // Return true if curr is not adjacent to the next segment.
          (next == null || (curr.position() + curr.text().length() < next.position())) ||
          // Return true if curr is followed by the segment that is [)'"].
          PUNCT_RHALF.matcher(next.text()).matches();
      case ")": case "\"":
        System.out.println("case2: " + prev.text() + " " + curr.text() + " " + next.text());
        return // true if:
          // curr is not adjacent to the next segment.
          (next == null || (curr.position() + curr.text().length() < next.position())) &&
          // Return true if curr is following the segment that is [:;,.!?].
          PUNCT_COMMON.matcher(prev.text()).matches();
      case "-":
        System.out.println("case3: " + prev.text() + " " + curr.text() + " " + next.text());
        return
            false;
//          // Return true if curr is not adjacent to the neighbour segments.
//          (next != null && prev != null &&
//              (prev.position() + prev.text().length() == curr.position()) &&
//              (curr.position() + curr.text().length() == next.position()));
      default:
        System.out.println("case4: " + prev.text() + " " + curr.text() + " " + next.text());
        return false;
    }
  }

  private boolean toMergeWithPrev(Segment prev, Segment curr, Segment next) {
    return prev != null
        && prev.position() + prev.text().length() == curr.position()
        && (FRAGMENT.matcher(prev.text()).find() || FRAGMENT.matcher(curr.text()).find())
        && ! isPunctuation(prev, curr, next);
  }

  @Override
  public TextSegments process(TextSegments textSegments) {
    List<Segment> segments = new ArrayList<>();
    Segment prev = null;

    for (int i = 0; i < textSegments.size(); i++) {
      Segment curr = textSegments.get(i);
      Segment next = i + 1 < textSegments.size() ? textSegments.get(i + 1) : null;

      if (toMergeWithPrev(prev, curr, next)) {
        // Update the previously added semgent in the list.
        prev = new Segment(prev.text() + curr.text(), prev.position());
        segments.set(segments.size() - 1, prev);
        System.out.println(prev.text() + " : " + curr.text() + " : " + next.text());
      } else {
        segments.add(curr);
        prev = curr;
      }
    }
    return new TextSegments(segments);
  }
}
