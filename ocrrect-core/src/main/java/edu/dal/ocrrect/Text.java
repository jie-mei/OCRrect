package edu.dal.ocrrect;

import edu.dal.ocrrect.text.Processor;
import edu.dal.ocrrect.text.WordSegmenter;
import edu.dal.ocrrect.text.TextSegments;
import edu.dal.ocrrect.util.TextualUnit;

public class Text extends TextualUnit {

  public Text(String text) {
    super(text);
  }

  public Text process(Processor<Text> processor) {
    return processor.process(this);
  }

  public TextSegments segment(WordSegmenter segmentor) {
    return segmentor.segment(this);
  }
}
