package edu.dal.ocrrect.text;

import edu.dal.ocrrect.util.Token;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;

import java.io.StringReader;
import java.util.List;
import java.util.stream.Collectors;

public class PennTreebankSegmenter implements TextSegmenter {
  private static final String OPTIONS = "ptb3Escaping=false,normalizeOtherBrackets=false";

  @Override
  public List<Token> segment(Text text) {
    PTBTokenizer<CoreLabel> ptb = new PTBTokenizer<>(
        new StringReader(text.text()), new CoreLabelTokenFactory(), OPTIONS);
    return ptb.tokenize()
      .stream()
      .map(cl -> new Token(cl.toString(), cl.beginPosition()))
      .collect(Collectors.toList());
  }
}
