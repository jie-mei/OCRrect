package edu.dal.corr.word;

import java.util.List;
import java.util.stream.Stream;

import org.apache.log4j.Logger;

import edu.dal.corr.util.LogUtils;

/**
 * @since 2016.09.01
 */
class WordFilters
{
  private static final Logger LOG = Logger.getLogger(WordFilters.class);
  
  private WordFilters() {}

  /**
   * Filter the specified word list using a {@link WordFilter}.
   * 
   * @param  words    a list of words.
   * @param  filters  a list of word filter.
   */
  static void filter(List<Word> words, WordFilter... filters)
 {
    LogUtils.logMethodTime(2, () ->
    {
      int lenMax = words.stream()
        .mapToInt(w -> w.text().length())
        .max()
        .getAsInt();
      for (int i = 0; i < words.size();) {
        Word word = words.get(i);
        if (filter(word, filters)) {
          words.remove(word);
          LOG.debug(String.format("%" + lenMax + "s %s", 
              word.text(), word.contextToString()));
        } else {
          i++;
        }
      }
    });
  }
  
  /**
   * Check the filtering conditions of all filters for the given word.
   * 
   * @param  word     a word.
   * @param  filters  a list of word filter.
   * @return {@code true} if the given word do not need further correction.
   *    {@code false} otherwise.
   */
  static boolean filter(Word word, WordFilter... filters)
  {
    return Stream.of(filters).anyMatch(f -> f.filter(word));
  }
}
