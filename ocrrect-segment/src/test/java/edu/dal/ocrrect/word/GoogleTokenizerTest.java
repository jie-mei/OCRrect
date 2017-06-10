package edu.dal.ocrrect.word;

import org.junit.Before;

public class GoogleTokenizerTest
  extends TokenizerTest
{
  @Before
  @Override
  public void setUp()
    throws Exception
  {
    tokenizer = new GoogleTokenizer();
  }
}
