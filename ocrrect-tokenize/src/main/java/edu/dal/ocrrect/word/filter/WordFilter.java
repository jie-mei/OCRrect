package edu.dal.ocrrect.word.filter;

import java.util.List;
import java.util.stream.Stream;
import org.apache.log4j.Logger;

import edu.dal.ocrrect.word.Word;

/**
 * @since 2017.04.20
 */
public interface WordFilter {
  static final Logger LOG = Logger.getLogger(WordFilter.class);

  boolean filter(Word word);

  /**
   * Filter the specified word list using a {@link WordFilter}.
   *
   * @param words a list of words.
   * @param filters a list of word filter.
   */
  static void filter(List<Word> words, WordFilter... filters) {
    if (filters == null || filters.length == 0) {
      return;
    }
    int lenMax = words
        .stream()
        .mapToInt(w -> w.text().length())
        .max()
        .getAsInt();
    for (int i = 0; i < words.size();) {
      Word word = words.get(i);
      if (filter(word, filters)) {
        words.remove(word);
        LOG.debug(word.toString(lenMax));
      } else {
        i++;
      }
    }
  }

  /**
   * Check the filtering conditions of all filters for the given word.
   *
   * @param word a word.
   * @param filters a list of word filter.
   * @return {@code true} if the given word do not need further correction. {@code false} otherwise.
   */
  public static boolean filter(Word word, WordFilter... filters) {
    return Stream.of(filters).anyMatch(f -> f.filter(word));
  }
}
