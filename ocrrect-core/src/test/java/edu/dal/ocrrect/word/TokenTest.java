package edu.dal.ocrrect.word;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import edu.dal.ocrrect.util.ResourceUtils;
import edu.dal.ocrrect.word.Token;

public class TokenTest
{
  private static Path TOKEN_PATH =
      ResourceUtils.getResource("test.hello-world.tokens.txt");
  private static Path TEXT_PATH =
      ResourceUtils.getResource("test.hello-world.text.txt");

  @Rule
  public TemporaryFolder temp = new TemporaryFolder();
  private List<Token> tokens;
  
  @Before
  public void before()
    throws IOException {
    tokens = Token.readTSV(TOKEN_PATH);
  }

  @Test
  public void testRead() {
    assertThat(tokens.get(0), is(new Token("Hello", 0)));
    assertThat(tokens.get(1), is(new Token(",", 5)));
    assertThat(tokens.get(2), is(new Token("world", 7)));
    assertThat(tokens.get(3), is(new Token("!", 12)));
  }

  @Test
  public void testWrite() throws IOException {
    Path tempPath = temp.newFile().toPath();
    Token.writeTSV(tokens, tempPath);
    assertThat(Token.readTSV(tempPath), is(tokens));
  }
}
