package edu.dal.ocrrect.text;

import edu.dal.ocrrect.util.TextualUnit;
import edu.dal.ocrrect.util.Token;

import java.util.List;

public class Text extends TextualUnit {
  public Text(String text) {
    super(text);
  }

  public Text process(Processor<Text> processor) {
    return processor.process(this);
  }

  public TextSegments segment(TextSegmenter segmentor) {
    return segmentor.segment(this);
  }
}
