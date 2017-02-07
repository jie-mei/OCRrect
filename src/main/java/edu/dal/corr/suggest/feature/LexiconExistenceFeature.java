package edu.dal.corr.suggest.feature;

import java.util.List;

import edu.dal.corr.suggest.banchmark.IsolatedWordBenchmarkDetectMixin;
import edu.dal.corr.word.Word;
import gnu.trove.set.hash.THashSet;

/**
 * @since 2016.08.10
 */
public class LexiconExistenceFeature
  extends AbstractScoreableFeature
  implements IsolatedWordBenchmarkDetectMixin
{
  private static final long serialVersionUID = -1167737697256350689L;

  private THashSet<String> lexicon;
  
  public LexiconExistenceFeature(String name, THashSet<String> lexicon) {
    this.lexicon = lexicon;
    setName(name);
  }

  public LexiconExistenceFeature(String name, List<String> lexicon) {
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
