package edu.dal.corr.word;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.dal.corr.util.IOUtils;
import edu.dal.corr.util.ResourceUtils;

public class WordTest
{
  private static Path TOKEN_PATH =
      ResourceUtils.getResource("test.hello-world.tokens.txt");
  private static Path TEXT_PATH =
      ResourceUtils.getResource("test.hello-world.text.txt");
  
  private List<Token> tokens;
  private String[] grams;
  
  @BeforeClass
  public static void setUpBeforeClass() {
    Logger.getRootLogger().setLevel(Level.OFF);
  }
  
  @Before
  public void setUp()
    throws IOException
  {
    tokens = Token.readTSV(TOKEN_PATH);
    grams = new String[]{
        "", "", "", "", "Hello", ",", "world", "!", "", "", ""};
  }

  @Test(expected=IllegalArgumentException.class)
  public void testConstructIllegalArgumentException()
  {
    new Word(0, "Hello", ",", "world", "!");
  }

  @Test
  public void testGetContexts()
  {
    List<Word> words = Token.toWords(tokens);
    Word w = words.get(0);
    assertThat(w.getContexts(2), is(Arrays.asList(
        new Context(1, w.position(), Arrays.copyOfRange(grams, 3, 5))
    )));
    assertThat(w.getContexts(3), is(Arrays.asList(
        new Context(1, w.position(), Arrays.copyOfRange(grams, 3, 6)),
        new Context(2, w.position(), Arrays.copyOfRange(grams, 2, 5))
    )));
    assertThat(w.getContexts(4), is(Arrays.asList(
        new Context(1, w.position(), Arrays.copyOfRange(grams, 3, 7)),
        new Context(2, w.position(), Arrays.copyOfRange(grams, 2, 6)),
        new Context(3, w.position(), Arrays.copyOfRange(grams, 1, 5))
    )));
  }

  @Test
  public void testEquals()
    throws IOException
  {
    for (int i = 0; i < 4; i++) {
      String[] ngrams = Arrays.copyOfRange(grams, i, i + 8);
      Word w1 = new Word(tokens.get(i).position(), ngrams);
      Word w2 = new Word(tokens.get(i).position(), ngrams);
      assertThat(w1.hashCode(), is(w2.hashCode()));
      assertThat(w1, is(w2));
    }
  }

  @Test
  public void testGetWithTokens()
    throws IOException
  {
    List<Word> words = Token.toWords(tokens);
    for (int i = 0; i < 4; i++) {
      assertThat(words.get(i), is(new Word(
              tokens.get(i).position(), Arrays.copyOfRange(grams, i, i + 8))));
    }
  }

  @Test
  public void testGetWithText()
    throws IOException
  {
    String text = IOUtils.read(TEXT_PATH);
    List<Word> words = Word.tokenize(text, new GoogleTokenizer());
    for (int i = 0; i < 4; i++) {
      assertThat(words.get(i),
          is(new Word(tokens.get(i).position(),
              Arrays.copyOfRange(grams, i, i + 8))));
    }
  }
}
