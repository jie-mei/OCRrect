package edu.dal.corr.word;

import java.util.regex.Pattern;

import edu.dal.corr.util.Dictionary;
import edu.dal.corr.util.WebsterDictionary;

/**
 * @since 2016.08.10
 */
public class DictionaryWordFilter
  implements WordFilter
{
  private static Pattern NON_ENGLISH = Pattern.compile("[^a-zA-Z]+");
  
  private Dictionary dict;
  
  public DictionaryWordFilter() 
  {
    dict = WebsterDictionary.getInstance();
  }

  @Override
  public boolean filter(Word word)
  {
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
