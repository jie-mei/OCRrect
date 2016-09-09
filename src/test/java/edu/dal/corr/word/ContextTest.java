package edu.dal.corr.word;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import edu.dal.corr.util.LocatedTextualUnitTest;
import edu.dal.corr.util.Random;

@RunWith(Parameterized.class)
public class ContextTest
  extends LocatedTextualUnitTest
{
  private static final int NUM_TEST_CASES = 1000;

  @Parameters
  public static Collection<Object[]> setUpParameters()
  {
    Random rand = new Random();
    int size = rand.nextInt(5) + 1;
    return IntStream.range(0, NUM_TEST_CASES)
      .mapToObj(i -> {
        String [] contextWords = rand.nextStringArray(size);
        int index = rand.nextInt(size);
        int position = rand.nextInt();
        return new Object[]{contextWords, index, position};
      })
      .collect(Collectors.toList());
  }

  private String[] contextWords;
  private int index;
  
  public ContextTest(String[] contextWords, int index, int position)
  {
    super(contextWords[index],
          position,
          new Context(index, position, contextWords));
    this.contextWords = contextWords;
    this.index = index;
  }

  @Override
  public Object newInstance() {
    return new Context(index, position(), contextWords);
  }
  
  @Override
  public Context instance() {
    return (Context) super.instance();
  }

  @Test
  public void testWords()
  {
    String[] words = instance().words();
    for (int i = 0; i < words.length; i++) {
      assertThat(words[i], is(contextWords[i]));
    }
  }

  @Test
  public void testIndex() {
    assertThat(instance().index(), is(index));
  }

  @Test
  public void testToNgram() {
    assertThat(instance().toNgram(), is(String.join(" ", contextWords)));
  }

  @Test
  @Override
  public void testToString() {
    assertThat(instance().toString(), is(String.join(" ", contextWords)));
  }

  @Test
  public void testEquals()
  {
    // Test the equal case.
    assertThat(instance(), is(new Context(index, position(), contextWords)));

    // Test the unequal cases with one of the parameter changes.
    Random random = new Random();
    if (contextWords.length > 1) {
      int newIdx = index;
      while ((newIdx = random.nextInt(contextWords.length)) == index);
      assertThat(instance(),
          not(new Context(newIdx, position(), contextWords)));
    }
    assertThat(instance(),
        not(new Context(index, random.nextIntNot(position()), contextWords)));
    for (int i = 0; i < contextWords.length; i++) {
      String[] words = contextWords.clone();
      words[i] = random.nextStringNot(words[i]);
      assertThat(instance(), not(new Context(index, position(), words)));
    }
  }
}
