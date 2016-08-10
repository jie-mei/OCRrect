package edu.dal.corr.word;

import org.junit.Before;

import edu.dal.corr.word.PennTreebankTokenizer;

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
