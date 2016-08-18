package edu.dal.corr.word;

import java.util.List;

import org.apache.log4j.Logger;

import edu.dal.corr.util.LogUtils;

/**
 * @since 2016.08.10
 */
class WordFilters
{
  private static final Logger LOG = Logger.getLogger(WordFilters.class);
  
  private WordFilters() {}

  /**
   * Filter the specified word list using a {@link WordFilter}.
   * 
   * @param  words   A list of words.
   * @param  filter  A word filter.
   */
  static void filter(List<Word> words, WordFilter... filters)
 {
    LogUtils.logMethodTime(2, () ->
    {
      int lenMax = words.stream()
          .mapToInt(w -> w.text().length())
          .max()
          .getAsInt();

      for (WordFilter filter : filters) {
        for (int i = 0; i < words.size();) {
          Word word = words.get(i);
          if (filter.filter(word)) {
            words.remove(word);
            LOG.debug(String.format("%" + lenMax + "s %s", 
                word.text(), word.info()));
          } else {
            i++;
          }
        }
      }
    });
  }
}
