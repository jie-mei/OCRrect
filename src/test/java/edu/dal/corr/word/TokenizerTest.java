package edu.dal.corr.word;

import static org.junit.Assert.*;

import java.util.regex.Pattern;

import org.junit.Before;
import org.junit.Test;

import edu.dal.corr.word.Token;
import edu.dal.corr.word.Tokenizer;

public abstract class TokenizerTest
{
  Pattern WHITESPACES_PATTERN = Pattern.compile("\\s*");
  private static String content = "a b-c d";
  
  protected Tokenizer tokenizer;

  @Before
  public abstract void setUp() throws Exception;

  @Test(expected=RuntimeException.class)
  public void testHasNextTokenError() {
    tokenizer.hasNextToken();
  }

  @Test(expected=RuntimeException.class)
  public void testNextTokenError() {
    tokenizer.nextToken();
  }

  @Test
  public void testTokenize() {
    tokenizer.tokenize(content);
    int offset = 0;
    while (content.substring(offset).trim().length() > 0) {
      assertEquals(true, tokenizer.hasNextToken());
      Token token = tokenizer.nextToken();

      /* Check if there is non-space character between tokens. */
      String interval = content.substring(offset, token.position());
      assertEquals(true, WHITESPACES_PATTERN.matcher(interval).matches());

      /* Check if the token name matches the prefix of the string. */
      assertEquals(token.text(),
          content.substring(token.position(),
                            token.position() + token.text().length()));
      
      offset = token.position() + token.text().length();
    }
    assertEquals(false, tokenizer.hasNextToken());
  }
}
