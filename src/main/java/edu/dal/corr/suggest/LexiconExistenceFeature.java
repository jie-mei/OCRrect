package edu.dal.corr.suggest;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import edu.dal.corr.suggest.banchmark.ContextInsensitiveBenchmarkDetectMixin;
import edu.dal.corr.util.IOUtils;
import edu.dal.corr.word.Word;
import gnu.trove.set.hash.THashSet;

/**
 * @since 2016.08.10
 */
public class LexiconExistenceFeature
  extends AbstractScoreableFeature
  implements Feature, ContextInsensitiveBenchmarkDetectMixin
{
  private THashSet<String> lexicon;
  
  public LexiconExistenceFeature(Path lexicon)
    throws IOException
  {
    this(IOUtils.readList(lexicon));
  }
  
  public LexiconExistenceFeature(List<String> lexicon)
  {
    this(new THashSet<>(lexicon));
  }
  
  public LexiconExistenceFeature(THashSet<String> lexicon)
  {
    super();
    this.lexicon = lexicon;
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
