package edu.dal.ocrrect.util.lexicon;

import edu.dal.ocrrect.util.Unigram;

public class GoogleUnigramLexicon implements Lexicon {
  private Unigram unigram;

  public GoogleUnigramLexicon() {
    unigram = Unigram.getInstance();
  }

  @Override
  public boolean contains(String word) {
    return unigram.contains(word);
  }
}
