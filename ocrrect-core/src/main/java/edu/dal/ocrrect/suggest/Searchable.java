package edu.dal.ocrrect.suggest;

import java.util.Set;

import edu.dal.ocrrect.util.Word;

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
