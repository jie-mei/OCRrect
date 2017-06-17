package edu.dal.ocrrect.text;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class FragmentConcatProcessor implements Processor<TextSegments> {

  private static final Pattern TEXT = Pattern.compile("^"
    + "(?<bracketLeft>[('\"]?)"
    + "(?<content>.*?)"
    + "(?<punctTail>(([)'\"]?[:;,.!?]?|[:;,.!?][)'\"])\\*?)?)$"
  );

  private static final Pattern CONTENT = Pattern.compile("^"
    + "(?<part>.*?)"
    + "(?<hyphen>-)"
    + "(?<remain>.*?)$"
  );

  private static final Pattern ERROR = Pattern.compile("[^\\p{Alnum}]");

  private static boolean isError(String part) {
    return ! ERROR.matcher(part).find();
  }

  private List<Segment> defragment(List<Segment> fragments) {
    if (fragments.size() == 1) {
      return fragments;
    }
    String text = fragments.stream().map(Segment::text).collect(Collectors.joining());
    List<Segment> segments = new ArrayList<>();
    Matcher m = TEXT.matcher(text);
    m.matches();

    // Retrieve de-hyphenated content splits
    List<String> splits = new ArrayList<>();
    String content = m.group("content");
    for (Matcher pm = CONTENT.matcher(content); pm.matches();
         content = pm.group("remain"), pm = CONTENT.matcher(content)) {
      splits.add(pm.group("part"));
      splits.add(("hyphen"));
    }
    splits.add(content);

    int pos = fragments.get(0).position();
    String group;

    // Retrieve the left bracket. The first content part concatenates this part if it is an error.
    group = m.group("bracketLeft");
    if (group.length() > 0) {
      segments.add(new Segment(group, pos));
      pos += group.length();
    }

    // Add content
    String content = m.group("content");
    for (Matcher pm = CONTENT.matcher(content); pm.matches();
         content = pm.group("remain"), pm = CONTENT.matcher(content)) {
      String part = pm.group("part");
      segments.add(new Segment(part, pos));
      pos += part.length();
      String hyphen = pm.group("hyphen");
      segments.add(new Segment(hyphen, pos));
      pos += 1;
    }
    segments.add(new Segment(content, pos));
    pos += content.length();

    // Add right bracket
    group = m.group("bracketRight");
    if (group.length() > 0) {
      segments.add(new Segment(group, pos));
      pos += group.length();
    }

    // Add punctuation, each character should be an separated segment.
    group = m.group("punctTail");
    for (int i = 0; i < group.length(); i++) {
      segments.add(new Segment(group.charAt(i) + "", pos));
      pos += 1;
    }

    System.out.print(text + " @ ");
    for (int i = 0; i < segments.size(); i++) {
      System.out.print(segments.get(i).text() + ":" + segments.get(i).position() + "  ");
    }
    System.out.println();

    return segments;
  }

  @Override
  public TextSegments process(TextSegments textSegments) {
    List<Segment> segments = new ArrayList<>();

    List<Segment> fragments = new ArrayList<>();
    Segment prev = null;
    for (Segment curr: textSegments) {
      if (prev != null && curr.position() > prev.position() + prev.text().length()) {
        segments.addAll(defragment(fragments));
        fragments = new ArrayList<>();
      }
      prev = curr;
      fragments.add(curr);
    }
    return new TextSegments(segments);
  }
}
