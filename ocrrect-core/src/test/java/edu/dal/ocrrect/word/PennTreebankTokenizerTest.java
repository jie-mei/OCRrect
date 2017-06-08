package edu.dal.ocrrect.word;

import org.junit.Before;

public class PennTreebankTokenizerTest
  extends TokenizerTest
{
  @Before
  @Override
  public void setUp()
    throws Exception
  {
    tokenizer = new PennTreebankTokenizer();
  }
}
