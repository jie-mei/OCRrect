package edu.dal.corr.suggest;

import edu.dal.corr.word.Word;
import gnu.trove.map.TObjectFloatMap;

/**
 * @since 2016.08.10
 */
public interface Suggestable
{
  /**
   * Suggest correction candidates for an error word.
   * 
   * @param  word  A word.
   * @return A feature suggestion.
   */
  TObjectFloatMap<String> suggest(Word word);
}
