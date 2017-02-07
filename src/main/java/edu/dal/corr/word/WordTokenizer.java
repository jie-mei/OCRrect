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

import edu.dal.corr.util.Dictionary;
import edu.dal.corr.util.GoogleUnigramHuristicThresholdDictionary;

/**
 * An object that implements the {@code Tokenizer} interface generates a series
 * of tokens from a text.
 * 
 * @since 2016.07.24
 */
public interface WordTokenizer
{
  static final Logger LOG = Logger.getLogger(WordTokenizer.class);

  /**
   * Tokenize the content. This method initializes the tokenization task and
   * should be called before {@link #hasNextToken()} and {@link #nextToken()}.
   * 
   * @param  content A string.
   */
  void tokenize(String content);

  /**
   * Return {@code true} if there is more token available.
   * 
   * @return {@code true} if there is at least one more token after current
   *    position; {@code false} otherwise.
   */
  boolean hasNextToken();
  
  /**
   * Return the next token.
   * 
   * @return A token string.
   */
  Token nextToken();
  
  static List<Word> tokenize(String text, WordTokenizer tokenizer) {
    return WordTokenizerImpl.tokenize(text, tokenizer);
  }
}

class WordTokenizerImpl
{
	private static String repeat(String str, int times)
	{
	  if (times < 0) {
	    throw new RuntimeException();
	  } else if (times == 0) {
	    return "";
	  }
	  return new String(new char[times]).replace("\0", str);
	}

  private static final Pattern BROKEN_WORD = Pattern.compile("([a-zA-Z]+)(-\\s*)$");
  private static final Pattern FIRST_WORD = Pattern.compile("^([a-zA-Z]*)(\\S*)(.*)$");

  /**
   * Concatenate content lines and fix the broken word created by line wrap.
   * If fixing a word changes the text length, extra white space characters are
   * padded following the modified words to keep the position of other words
   * unchanged.
   * 
   * @param  content A string.
   * @return The input content without internal line-break and broken word
   *    fixed.
   */
  private static String fixLineBrokenWords(String str)
  {
    StringBuilder sb = new StringBuilder();
    try (BufferedReader br = new BufferedReader(new StringReader(str))) {
      Dictionary dict = GoogleUnigramHuristicThresholdDictionary.getInstance();

      String curr = null;
      String bkPart1 = null;
      int pad = 0;
      for (curr = br.readLine(); curr != null; curr = br.readLine()) {
        
        // Pad is greater than zero, if the previous line is truncated.
        if (pad > 0) {
          // Pad white spaces to before the first white space characters on this
          // line.
          Matcher m = FIRST_WORD.matcher(curr);
          m.find();
          String bkPart2 = m.group(1);
          String bkPart3 = m.group(2);
          String remain = m.group(3);
          
          if (bkPart2.length() == 0) {
            // If current line start with a white space, then restore the
            // character sequence on the previous line.
            sb.append('-').append(repeat(" ", pad));

          } else {
            // Check if the combining word is a valid word in the dictionary.
            // Otherwise keep the character sequence in the original text.
            String fixedWord1 = bkPart1 + bkPart2;
            String fixedWord2 = bkPart1 + "-" + bkPart2;
            // LogUtils.info(">>> " + fixedWord1 + ", " + fixedWord2);

            if (dict.contains(fixedWord2.toLowerCase())) {
              curr = "-" + bkPart2 + bkPart3 + repeat(" ", pad) + remain;
              // LogUtils.info("F2: " + fixedWord2 + bkPart3 + " = " + bkPart1 + "," + bkPart2);

            } else if (dict.contains(fixedWord1.toLowerCase())) {
              curr = bkPart2 + bkPart3 + repeat(" ", pad + 1) + remain;
              // LogUtils.info("F1: " + fixedWord1 + bkPart3 + " = " + bkPart1 + "," + bkPart2);

            } else if (dict.contains(bkPart1.toLowerCase())
                && dict.contains(bkPart2.toLowerCase())) {
              curr = "-" + bkPart2 + bkPart3 + repeat(" ", pad) + remain;
              // LogUtils.info("F2: " + fixedWord2 + bkPart3 + " = " + bkPart1 + "," + bkPart2);

            } else {
              curr = "-" + repeat(" ", pad) + curr;
              // LogUtils.info("F3: " + fixedWord2 + bkPart3 + " = " + bkPart1 + "," + repeat(" ", pad) + "," + bkPart2);
            }
          }
          pad = 0;
        }
        
        // Check the line tail. If there is word broken by line wrap, remove the
        // internal hyphen and trailing white-spaces.
        Matcher brokenWord = BROKEN_WORD.matcher(curr);
        if (brokenWord.find()) {
          bkPart1 = brokenWord.group(1);
          pad = brokenWord.group(2).length();
          curr = curr.substring(0, curr.length() - pad);
          sb.append(curr);

        } else {
          // Insert a white-space characters to replace the newline character in
          // the input text.
          sb.append(curr).append(' ');
        }
      }
      // Concatenate the last line.
      sb.append(repeat(" ", pad));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return sb.toString();
  }

  private static final Pattern SPLITTED = Pattern.compile("[^a-zA-Z():;,.\\-'\"!?]");

  /**
   * Tokenize the text using a {@link WordTokenizer}.
   * 
   * @param  concatenated       A text string.
   * @param  tokenizer  A tokenizer.
   * @return A list of words.
   */
  static List<Word> tokenize(String text, WordTokenizer tokenizer)
  {
    String concatenated = fixLineBrokenWords(text);

    // Store eight latest reading consecutive tokens and positions.
    Token[] context = new Token[8];
    Arrays.fill(context, Token.EMPTY);
    int widx = 0;
    List<WordBuilder> bldrs = new ArrayList<>();
    
    tokenizer.tokenize(concatenated);
    
    Token prev = null;
    for (Token curr = null; tokenizer.hasNextToken(); prev = curr) {
      curr = tokenizer.nextToken();

      // Combine the connected words if one of the word consists of non-English
      // letters and common punctuations (i.e. ,.'").
      if (prev != null
          && prev.position() + prev.text().length() == curr.position()
          && (SPLITTED.matcher(prev.text()).matches()
              || SPLITTED.matcher(curr.text()).matches())
      ){
        WordTokenizer.LOG.trace(String.format("%s + %s -> %s",
            prev.text(), curr.text(), prev.text() + curr.text()));

        curr = new Token(prev.text() + curr.text(), prev.position());
        
        // Override the previous written token.
        // TODO less than 3 tokens in total may throw error here!
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
            (i > 1 ? Token.EMPTY: context[(widx - 2) % 8]),
            (i > 0 ? Token.EMPTY : context[(widx - 1) % 8]),
            Token.EMPTY
        ));
      }
    }
    List<Word> words = bldrs.stream()
        .map(w -> w.build())
        .collect(Collectors.toList());

    return words;
  }
}