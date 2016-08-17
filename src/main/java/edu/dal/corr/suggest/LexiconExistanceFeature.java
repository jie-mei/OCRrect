package edu.dal.corr.suggest;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.dal.corr.suggest.banchmark.ContextInsensitiveBenchmarkDetectMixin;
import edu.dal.corr.word.Word;

/**
 * @since 2016.08.10
 */
public class LexiconExistanceFeature
  extends AbstractScoreableFeature
  implements Feature, ContextInsensitiveBenchmarkDetectMixin
{
  private Set<String> lexicon;
  
  public LexiconExistanceFeature(List<String> lexicon)
  {
    super();
    this.lexicon = new HashSet<>(lexicon);
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
