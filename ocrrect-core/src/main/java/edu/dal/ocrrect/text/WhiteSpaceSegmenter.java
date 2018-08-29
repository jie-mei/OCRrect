package edu.dal.ocrrect.text;

import edu.dal.ocrrect.Text;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.WhitespaceTokenizer;

import java.io.StringReader;
import java.util.stream.Collectors;

public class WhiteSpaceSegmenter implements WordSegmenter {

  @Override
  public TextSegments segment(Text text) {
    WhitespaceTokenizer<CoreLabel> ws = new WhitespaceTokenizer<>(
        new CoreLabelTokenFactory(),
        new StringReader(text.text()),
        false);
    return new TextSegments(ws.tokenize()
      .stream()
      .map(cl -> new Segment(cl.toString(), cl.beginPosition()))
      .collect(Collectors.toList()));
  }
}
