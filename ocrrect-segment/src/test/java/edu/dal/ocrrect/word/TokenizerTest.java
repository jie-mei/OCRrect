package edu.dal.ocrrect.word;

import static org.junit.Assert.*;

import java.util.regex.Pattern;

import edu.dal.ocrrect.util.Token;
import org.junit.Before;
import org.junit.Test;

public abstract class TokenizerTest
{
  private Pattern WHITESPACES_PATTERN = Pattern.compile("\\s*");
  private static String CONTENT = "a b-c d";

  protected WordTokenizer tokenizer;

  @Before
  public abstract void setUp() throws Exception;

  @Test
  public void testTokenize() {
    tokenizer.tokenize(CONTENT);
    int offset = 0;
    while (CONTENT.substring(offset).trim().length() > 0) {
      assertEquals(true, tokenizer.hasNextToken());
      Token token = tokenizer.nextToken();

      // Check if there is non-space character between tokens.
      String interval = CONTENT.substring(offset, token.position());
      assertEquals(true, WHITESPACES_PATTERN.matcher(interval).matches());

      // Check if the token name matches the prefix of the string.
      assertEquals(token.text(),
          CONTENT.substring(token.position(),
                            token.position() + token.text().length()));

      offset = token.position() + token.text().length();
    }
    assertEquals(false, tokenizer.hasNextToken());
  }
}
