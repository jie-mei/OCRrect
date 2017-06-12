package edu.dal.ocrrect.util.filter;

import edu.dal.ocrrect.util.lexicon.Lexicon;
import edu.dal.ocrrect.util.lexicon.WebsterDictionary;
import edu.dal.ocrrect.util.Word;

import java.util.regex.Pattern;

/**
 * @since 2017.04.20
 */
public class DictionaryWordFilter implements WordFilter {
  private static Pattern NON_ENGLISH = Pattern.compile("[^a-zA-Z]+");

  private Lexicon dict;

  public DictionaryWordFilter() {
    dict = WebsterDictionary.getInstance();
  }

  @Override
  public boolean filter(Word word) {
    String name = word.text().toLowerCase();
    if(name.length() <= 1
        || dict.contains(name)
        || NON_ENGLISH.matcher(name).matches()) {
      return true;
    } else {
      return false;
    }
  }
}
