package edu.dal.corr.suggest;

import java.util.List;

import edu.dal.corr.word.Word;

public interface Searchable
{
  /**
   * Search correction candidates for an error word.
   * 
   * @param  word  A word.
   * @return A list of candidate string.
   */
  List<String> search(Word word);
}
