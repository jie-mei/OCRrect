package edu.dal.corr.word;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.regex.Pattern;

import edu.dal.corr.util.IOUtils;
import edu.dal.corr.util.ResourceUtils;
import gnu.trove.map.hash.TObjectByteHashMap;

/**
 * @since 2016.08.10
 */
public class CommonWordFilter
  implements WordFilter
{
  private static Pattern NON_ENGLISH = Pattern.compile("[^a-zA-Z]+");

  private TObjectByteHashMap<String> dict;
  
  public CommonWordFilter() 
  {
    PennTreebankTokenizer tkz = new PennTreebankTokenizer();

    dict = new TObjectByteHashMap<>();
    try (BufferedReader br = IOUtils.newBufferedReader(ResourceUtils.WEBSTER_DICTIONARY)){

      // The input text is a dictionary. Each word follows a detailed definition
      // and explanation. We extract the word in the word section line and all
      // lower-case English words in the definition and explanation.
      StringBuilder sb = null;
      for (String line; (line = br.readLine()) != null;) {

        String text = line.trim();
        if (text.length() == 0) {
          continue;
        }

        boolean isWord = true;
        for (int i = 0; i < text.length(); i++) {
          // A word section line contains only upper-case English letters.
          if (! Character.isUpperCase(text.charAt(i))) {
            isWord = false;
            break;
          }
        }
        if (isWord) {
          // For the previous word, find derivations from the content.
          if (sb != null) {
            String content = sb.toString();
            tkz.tokenize(content);
            while (tkz.hasNextToken()) {
              String word = tkz.nextToken().text().toLowerCase();
              if (word.matches("[a-z]+")) {
                dict.put(word, (byte) 0);
              }
            }
          }

          // For the current word, add the word to dictionary and reset the
          // derivations.
          String word = text.toLowerCase();
          dict.put(word, (byte) 0);
          
          sb = new StringBuilder();
        } else {
          if (sb != null) {
            sb.append(line);
          }
        }
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
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
