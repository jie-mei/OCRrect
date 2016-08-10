package edu.dal.corr.word;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import edu.dal.corr.util.LogUtils;

/**
 * This class consists exclusively of static methods that related to {@link
 * Word}.
 * 
 * @since 2016.07.30
 * 
 * @see Word
 */
class WordTokenizers
{
  private static final Logger LOG = Logger.getLogger(WordTokenizers.class);
  private static final Pattern SPLITTED = Pattern.compile("[^a-zA-Z():;,.\\-'\"]");
  
  private WordTokenizers() {}

  /**
   * Tokenize the text using a {@link Tokenizer}.
   * 
   * @param  concatenated       A text string.
   * @param  tokenizer  A tokenizer.
   * @return A list of words.
   */
  static List<Word> tokenize(String text, Tokenizer tokenizer)
  {
    return LogUtils.logMethodTime(1, () ->
    {
      String concatenated = concatLines(text);

      // Store eight latest reading consecutive tokens and positions.
      Token empty = new Token("", -1);
      Token[] context = new Token[8];
      Arrays.fill(context, empty);
      int widx = 0;
      List<WordBuilder> bldrs = new ArrayList<>();
      
      tokenizer.tokenize(concatenated);
      
      Token prev = null;
      for (Token curr = null; tokenizer.hasNextToken(); prev = curr) {
        curr = tokenizer.nextToken();

        // Combine the two connected words if one of the word consists of
        // non-English letters and common punctuations (i.e. ,.'").
        if (prev != null
            && prev.position() + prev.text().length() == curr.position()
            && (SPLITTED.matcher(prev.text()).matches()
                || SPLITTED.matcher(curr.text()).matches())
        ){
          LOG.trace(String.format("%s + %s -> %s",
              prev.text(), curr.text(), prev.text() + curr.text()));

          curr = new Token(prev.text() + curr.text(), prev.position());
          
          // Override the previous written token.
          bldrs.get(bldrs.size() - 1).set(7, curr);
          context[(widx - 1) % 8] = curr;

          continue;
        }
       
        // Add token to context.
        int idx = widx % 8;
        context[idx] = curr;
        
        if (widx >= 3) {
          // Store the full-context token.
          idx = (widx - 3) % 8;
          bldrs.add(new WordBuilder(
              context[(widx + 1) % 8],
              context[(widx + 2) % 8],
              context[(widx + 3) % 8],
              context[(widx + 4) % 8],
              context[idx],
              context[(widx - 2) % 8],
              context[(widx - 1) % 8],
              context[widx % 8]));
        }
        widx++;
      }
      // Store the tailing tokens.
      for (int i = 0; i < 3; widx++, i++) {
        int idx = (widx + 5) % 8;
        if (context[idx].position() >= 0) {
          bldrs.add(new WordBuilder(
              context[(widx + 1) % 8],
              context[(widx + 2) % 8],
              context[(widx + 3) % 8],
              context[(widx + 4) % 8],
              context[idx],
              (i > 1 ? empty : context[(widx - 2) % 8]),
              (i > 0 ? empty : context[(widx - 1) % 8]),
              empty
          ));
        }
      }
      List<Word> words = bldrs.stream()
          .map(w -> w.build())
          .collect(Collectors.toList());

      return words;
    });
  }
  
  private static Pattern BROKEN_WORD = Pattern.compile("\\S+(-\\s*)$");
  private static Pattern TAILING_SPACE = Pattern.compile("(\\s*)$");
  
  /**
   * Concatenate content lines and fix the broken word created by line wrap.
   * 
   * @param  content A string.
   * @return The input content without internal line-break and broken word
   *    fixed.
   */
  private static String concatLines(String content)
  {
    StringBuilder sb = new StringBuilder();
    try (BufferedReader br = new BufferedReader(new StringReader(content))) {
      String curr = null;
      for (String next = null ; (next = br.readLine()) != null; curr = next) {
        if (curr == null) continue;
        
        // Check the line tail. If there is word broken by line wrap, remove the
        // internal hyphen and trailing white-spaces.
        Matcher brokenWord = BROKEN_WORD.matcher(curr);
        if (brokenWord.find()) {
          curr = curr.substring(0, curr.length() - brokenWord.group(1).length());
        } else {
          // Trim the tailing white-space characters.
          Matcher tailSpace = TAILING_SPACE.matcher(curr);
          if (tailSpace.find()) {
            curr = curr.substring(0, tailSpace.start()) + " ";
          }
        }
        sb.append(curr);
      }
      // Concatenate the last line.
      sb.append(curr);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return sb.toString();
  }
}
