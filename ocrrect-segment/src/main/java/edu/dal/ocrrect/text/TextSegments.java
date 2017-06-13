package edu.dal.ocrrect.text;

import edu.dal.ocrrect.util.Token;
import edu.dal.ocrrect.util.Word;
import edu.dal.ocrrect.util.Words;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TextSegments extends ArrayList<Segment> {
  public TextSegments(List<Segment> segments) {
    super(segments);
  }

  public TextSegments process(Processor<TextSegments> processor) {
    return processor.process(this);
  }

  public List<Word> toWords() {
    List<Token> tokens = this
        .stream()
        .map(s -> (Token) s)
        .collect(Collectors.toList());
    return Words.toWords(tokens);
  }
}
