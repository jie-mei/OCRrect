package edu.dal.ocrrect.text;

import edu.dal.ocrrect.Text;

public interface WordSegmenter {
  TextSegments segment(Text text);
}
