package edu.dal.ocrrect.text;

import edu.dal.ocrrect.Text;
import edu.dal.ocrrect.util.Token;
import edu.dal.ocrrect.util.lexicon.Lexicon;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GoogleGramSegmenter extends PennTreebankSegmenter {

  private final static Pattern SPLIT_PATTERN = Pattern.compile("([^-]+|-)");

  private Lexicon vocab;

  public GoogleGramSegmenter(Lexicon vocab) {
    this.vocab = vocab;
  }

  @Override
  public TextSegments segment(Text text) {
    List<Segment> segments = new ArrayList<>();
    for (Token tk : super.segment(text)) {
      Matcher m = SPLIT_PATTERN.matcher(tk.text());
      while (m.find()) {
        segments.add(new Segment(m.group(), tk.position() + m.start()));
      }
    }
    return new TextSegments(segments);
  }
}
