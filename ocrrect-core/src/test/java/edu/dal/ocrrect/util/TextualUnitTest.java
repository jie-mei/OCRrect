package edu.dal.ocrrect.util;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import org.junit.Test;

import edu.dal.ocrrect.util.TextualUnit;

public abstract class TextualUnitTest
{
  private String text;
  private TextualUnit instance;

  public TextualUnitTest(String text, TextualUnit instance)
  {
    this.text = text;
    this.instance = instance;
  }

  protected String text() {
    return text;
  }

  protected TextualUnit instance() {
    return instance;
  }

  @Test
  public void testText() {
    assertThat(instance().text(), is(text));
  }

  @Test
  public void testToString() {
    assertThat(instance().toString(), is(text));
  }

  @Test
  public void testBuildHash() {
    assertThat(instance().hashCode(), is(instance().buildHash().toHashCode()));
  }

  public abstract Object newInstance();

  @Test
  public void testHashCode() {
    assertThat(instance().hashCode(), is(newInstance().hashCode()));
  }
}
