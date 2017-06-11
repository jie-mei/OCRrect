package edu.dal.ocrrect.text;

import edu.dal.ocrrect.util.Token;

import java.util.List;

public interface TextSegmenter {
  List<Token> segment(Text text);
}
