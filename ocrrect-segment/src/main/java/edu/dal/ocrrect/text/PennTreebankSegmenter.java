package edu.dal.ocrrect.text;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;

import java.io.StringReader;
import java.util.stream.Collectors;

public class PennTreebankSegmenter implements TextSegmenter {
  private static final String OPTIONS = "ptb3Escaping=false,normalizeOtherBrackets=false";

  @Override
  public TextSegments segment(Text text) {
    PTBTokenizer<CoreLabel> ptb = new PTBTokenizer<>(
        new StringReader(text.text()), new CoreLabelTokenFactory(), OPTIONS);
    return new TextSegments(ptb.tokenize()
      .stream()
      .map(cl -> new Segment(cl.toString(), cl.beginPosition()))
      .collect(Collectors.toList()));
  }
}
