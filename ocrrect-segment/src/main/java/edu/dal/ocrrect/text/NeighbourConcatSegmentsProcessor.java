package edu.dal.ocrrect.text;

import edu.dal.ocrrect.util.lexicon.Lexicon;
import edu.stanford.nlp.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;

public class NeighbourConcatSegmentsProcessor implements Processor<TextSegments> {

  private Lexicon vocab;

  public NeighbourConcatSegmentsProcessor(Lexicon vocab) {
    this.vocab = vocab;
  }

  @Override
  public TextSegments process(TextSegments textSegments) {
    List<Segment> segments = new ArrayList<>();
    boolean prevIsError = false;
    Segment prev = null;
    for (Segment curr: textSegments) {
      if (! vocab.contains(curr.text())) {
        if (prevIsError && prev != null) {
          // Update the previously added semgent in the list.
          int offset = curr.position() - prev.position() - prev.text().length();
          prev = new Segment(prev.text() + StringUtils.repeat(' ', offset) + curr.text(),
            prev.position());
          segments.set(segments.size() - 1, prev);
        } else {
          segments.add(curr);
          prev = curr;
        }
        prevIsError = true;
      } else {
        segments.add(curr);
        prev = curr;
      }
    }
    return new TextSegments(segments);
  }
}
