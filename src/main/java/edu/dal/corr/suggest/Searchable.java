package edu.dal.corr.suggest;

import java.util.Set;

import edu.dal.corr.word.Word;

public interface Searchable
{
  /**
   * Search correction candidates for an error word.
   * 
   * @param  word  A word.
   * @return A list of candidate string.
   */
  Set<String> search(Word word);
}
