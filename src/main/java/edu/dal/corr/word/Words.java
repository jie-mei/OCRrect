package edu.dal.corr.word;

import java.util.List;

import org.apache.log4j.Logger;

import edu.dal.corr.util.LogUtils;

/**
 * @since 2016.08.10
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
            .map(w -> String.format("%" + lenMax + "s %s", w.text(), w.info()))
            .forEach(LOG::trace);
      }
      return words;
    });
  }
}
