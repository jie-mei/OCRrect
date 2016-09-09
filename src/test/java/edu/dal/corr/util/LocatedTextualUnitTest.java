package edu.dal.corr.util;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import org.junit.Test;

public abstract class LocatedTextualUnitTest
  extends TextualUnitTest
{
  private int position;
  
  public LocatedTextualUnitTest(
      String text,
      int position,
      LocatedTextualUnit instance)
  {
    super(text, instance);
    this.position = position;
  }
  
  public int position() {
    return position;
  }

  @Test
  public void testPosition() {
    assertThat(((LocatedTextualUnit)instance()).position(), is(position));
  }
}
