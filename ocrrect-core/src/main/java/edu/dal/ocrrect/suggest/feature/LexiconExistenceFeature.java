package edu.dal.ocrrect.suggest.feature;

import java.io.IOException;
import java.util.List;

import edu.dal.ocrrect.word.Word;
import gnu.trove.set.hash.THashSet;

/**
 * @since 2016.08.10
 */
public class LexiconExistenceFeature
    extends WordIsolatedFeature {

  private static final long serialVersionUID = -1167737697256350689L;

  private THashSet<String> lexicon;
  
  public LexiconExistenceFeature(String name, THashSet<String> lexicon) throws IOException {
    this.lexicon = lexicon;
    setName(name);
  }

  public LexiconExistenceFeature(String name, List<String> lexicon) throws IOException {
    this(name, new THashSet<>(lexicon));
  }

  @Override
  public boolean detect(Word word) {
    return lexicon.contains(word.text());
  }

  @Override
  public float score(Word word, String candidate) {
    return lexicon.contains(candidate) ? 1 : 0;
  }
}
