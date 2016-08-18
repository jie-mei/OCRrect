package edu.dal.corr.word;

import java.util.regex.Pattern;

/**
 * @since 2016.08.10
 */
public class CommonPatternFilter
  implements WordFilter
{
  private static Pattern NTH_PATTERN = Pattern.compile("[0-9]+(th|rd|nd)");
  private static Pattern SLASH_S_PATTERN = Pattern.compile("'[sS]");
  private static Pattern ABBR_PATTERN = Pattern.compile("[a-zA-Z]+\\.");

  @Override
  public boolean filter(Word word)
  {
    String text = word.text();
    if(NTH_PATTERN.matcher(text).matches()
        || SLASH_S_PATTERN.matcher(text).matches()
        || ABBR_PATTERN.matcher(text).matches()) {
      return true;
    } else {
      return false;
    }
  }
}
