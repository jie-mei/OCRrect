package edu.dal.corr.word;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import edu.dal.corr.util.IOUtils;
import edu.dal.corr.util.ResourceUtils;
import gnu.trove.map.hash.TObjectByteHashMap;

public class DictionaryWordFilter
  implements WordFilter
{
  private static Pattern NON_ENGLISH = Pattern.compile("[^a-zA-Z]+");
  private static final List<String> SUFFIXES = Arrays.asList(
      "ing", "ings", "s", "es", "er", "or", "est", "ed");

  private TObjectByteHashMap<String> dict;
  
  private static List<String> getDerivations(String word)
  {
    List<String> der = new ArrayList<>();
    char last = word.charAt(word.length() - 1);
    String wordWOLast = word.substring(0, word.length() - 1);

    SUFFIXES.stream()
        .map(suffix -> word + suffix)
        .forEachOrdered(der::add);
    SUFFIXES.stream()
        .map(suffix -> word + last + suffix)
        .forEachOrdered(der::add);
    SUFFIXES.stream()
        .map(suffix -> wordWOLast + suffix)
        .forEachOrdered(der::add);

    if (last == 'y') {
      der.add(wordWOLast + "ies");
      der.add(wordWOLast + "ied");
    }
    return der;
  }
  
  public DictionaryWordFilter() 
  {
    PennTreebankTokenizer tkz = new PennTreebankTokenizer();

    dict = new TObjectByteHashMap<>();
    try (BufferedReader br = IOUtils.newBufferedReader(ResourceUtils.WEBSTER_DICTIONARY)){

      // The input text is a dictionary. Each word follows a detailed definition
      // and explanation. We extract the word in the word section line and its
      // common derivations (i.e. -ing, -s/-es, -er/-or, -est, -ed) from word
      // definitions and explanation.
      List<String> derivations = null;
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
              if (derivations.contains(word)) {
                dict.put(word, (byte) 0);
                derivations.remove(word);
              }
            }
          }

          // For the current word, add the word to dictionary and reset the
          // derivations.
          String word = text.toLowerCase();
          dict.put(word, (byte) 0);
          derivations = getDerivations(word);
          
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
