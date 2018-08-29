package edu.dal.ocrrect.text;

import edu.dal.ocrrect.util.lexicon.Lexicon;

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang.StringUtils.repeat;

public class NonWordConcatProcessor implements Processor<TextSegments> {

  private Lexicon vocab;
  private boolean caseSensitive;

  public NonWordConcatProcessor(Lexicon vocab, boolean caseSensitive) {
    this.vocab = vocab;
    this.caseSensitive = caseSensitive;
  }

  public NonWordConcatProcessor(Lexicon vocab) {
    this(vocab, false);
  }

  private boolean isNonWord(Segment segment) {
    String word = segment.text();
    if (caseSensitive) {
      return ! vocab.contains(word);
    } else {
      return ! vocab.contains(word) && ! vocab.contains(word.toLowerCase());
    }
  }

  private Segment merge(Segment s1, Segment s2) {
    String pad = repeat(" ", s2.position() - s1.position() - s1.text().length());
    return new Segment(s1.text() + pad + s2.text(), s1.position());
  }

  @Override
  public TextSegments process(TextSegments textSegments) {
    List<Segment> segments = new ArrayList<>();
    Segment prev = null;
    for (Segment curr: textSegments) {
      if (isNonWord(curr)) {
        if (prev != null) {
          // Merge with previous non-word segment.
          prev = merge(prev, curr);
        } else {
          prev = curr;
        }
      } else {
        if (prev != null) {
          segments.add(prev);
          prev = null;
        }
        segments.add(curr);
      }
    }
    return new TextSegments(segments);
  }
}
