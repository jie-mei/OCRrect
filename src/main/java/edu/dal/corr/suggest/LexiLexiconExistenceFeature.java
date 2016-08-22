package edu.dal.corr.suggest;

import java.io.IOException;

import edu.dal.corr.util.ResourceUtils;

public class LexiLexiconExistenceFeature
  extends LexiconExistenceFeature
{
  public LexiLexiconExistenceFeature()
    throws IOException
  {
    super(ResourceUtils.LEXI_LEXICON);
  }
}
