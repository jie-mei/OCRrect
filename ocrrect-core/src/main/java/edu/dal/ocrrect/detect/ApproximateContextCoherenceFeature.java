package edu.dal.ocrrect.detect;

import edu.dal.ocrrect.suggest.NgramBoundedReaderSearcher;
import edu.dal.ocrrect.util.Context;

/**
 * @since 2017.04.26
 */
public class ApproximateContextCoherenceFeature extends ContextCoherenceFeature
    implements Detectable {
  public ApproximateContextCoherenceFeature(String name, NgramBoundedReaderSearcher reader, int ngramSize) {
    super(name, reader, ngramSize);
  }

  @Override
  protected String substitueWord(String ngram, Context context) {
    String[] grams = ngram.split(" ");
    // Detect whether this ngram contains a word substitution.
    String sub = grams[context.index()];
    boolean unmatch = false;
    for (int i = 0; i < ngramSize(); i++) {
      if (context.index() != i && !context.words()[i].equals(grams[i])) { // context gram unmatch
        if (unmatch) { // already have an unmatching gram
          sub = null;
          break;
        }
        unmatch = true;
      }
    }
    return sub;
  }
}
