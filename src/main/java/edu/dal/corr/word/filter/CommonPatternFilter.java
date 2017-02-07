package edu.dal.corr.word.filter;

import java.util.regex.Pattern;

import edu.dal.corr.word.Word;

/**
 * @since 2016.08.10
 */
public class CommonPatternFilter
  implements WordFilter
{
  private static Pattern NTH_PATTERN = Pattern.compile("[0-9]+(th|rd|nd)");
  private static Pattern SLASH_S_PATTERN = Pattern.compile("'[sS]");
  private static Pattern PUNCT_PATTERN = Pattern.compile("[^a-zA-Z]+");
  private static Pattern ABBR_PATTERN = Pattern.compile("[a-zA-Z]+\\.");

  @Override
  public boolean filter(Word word)
  {
    String text = word.text();
    if(NTH_PATTERN.matcher(text).matches()
        || SLASH_S_PATTERN.matcher(text).matches()
        || PUNCT_PATTERN.matcher(text).matches()
        || ABBR_PATTERN.matcher(text).matches()
        || isRomanNumber(text)) {
      return true;
    } else {
      return false;
    }
  }
  
  private boolean isRomanNumber(String word)
  {
    switch(word) {
      case "i":
      case "ii":
      case "iii":
      case "iv":
      case "v":
      case "vi":
      case "vii":
      case "viii":
      case "ix":
      case "x":
      case "xi":
      case "xii":
      case "xiii":
      case "xiv":
      case "xv":
      case "xvi":
      case "xvii":
      case "xviii":
      case "xix":
        return true;
      default:
        return false;
    }
  }
}
