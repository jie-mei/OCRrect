package edu.dal.corr.word;

import java.util.List;

import org.apache.log4j.Logger;

import edu.dal.corr.util.LogUtils;

/**
 * An abstract representation of a word.
 * An abstract word contains the following informations:
 * <ul>
 *  <li> The string representation of this word.
 *  <li> The string representation of the correct word according to the error
 *       word.
 *  <li> The offset from the beginning of the original text to the the position
 *       of the first character in the error word.
 *  <li> Addition note of this errors.
 * </ul>
 *
 * @since 2016.09.07
 */
public class Words
{
  private static final Logger LOG = Logger.getLogger(Words.class);
  
  private Words() {}

  public static List<Word> get(String content, Tokenizer tokenizer, WordFilter... filters)
  {
    return LogUtils.logMethodTime(1, () ->
    {
      List<Word> words = WordTokenizers.tokenize(content, tokenizer);
      WordFilters.filter(words, filters);

      if (LOG.isTraceEnabled()) {
        int lenMax = words.stream()
            .mapToInt(w -> w.text().length())
            .max()
            .getAsInt();
        words.stream()
            .map(w -> String.format("%" + lenMax + "s %s", w.text(), w.contextToString()))
            .forEach(LOG::trace);
      }
      return words;
    });
  }
}
