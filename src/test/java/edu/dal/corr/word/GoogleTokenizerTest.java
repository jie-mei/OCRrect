package edu.dal.corr.word;

import org.junit.Before;

import edu.dal.corr.word.GoogleTokenizer;

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
