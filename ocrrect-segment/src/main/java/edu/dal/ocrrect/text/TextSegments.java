package edu.dal.ocrrect.text;

import java.util.List;

public class TextSegments {
  private List<Segment> segments;

  public TextSegments(List<Segment> segments) {
    this.segments = segments;
  }

  public TextSegments process(Processor<TextSegments> processor) {
    return processor.process(segments);
  }
}
