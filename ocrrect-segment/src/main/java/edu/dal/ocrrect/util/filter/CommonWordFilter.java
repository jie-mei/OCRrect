package edu.dal.ocrrect.util.filter;

import edu.dal.ocrrect.text.PennTreebankSegmenter;
import edu.dal.ocrrect.Text;
import edu.dal.ocrrect.text.WordSegmenter;
import edu.dal.ocrrect.util.IOUtils;
import edu.dal.ocrrect.util.ResourceUtils;
import edu.dal.ocrrect.util.Token;
import edu.dal.ocrrect.util.Word;
import gnu.trove.map.hash.TObjectByteHashMap;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.regex.Pattern;

/**
 * @since 2017.04.20
 */
public class CommonWordFilter implements WordFilter {
  private TObjectByteHashMap<String> dict;

  public CommonWordFilter() {
    WordSegmenter segmenter = new PennTreebankSegmenter();

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
            for (Token token: segmenter.segment(new Text(content))) {
              String word = token.text().toLowerCase();
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

  private static Pattern NON_ENGLISH = Pattern.compile("[^a-zA-Z]+");

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
