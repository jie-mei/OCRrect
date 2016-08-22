package edu.dal.corr.suggest;

import java.io.IOException;

import edu.dal.corr.util.ResourceUtils;

public class SpecialLexiconExistenceFeature
  extends LexiconExistenceFeature
{
  public SpecialLexiconExistenceFeature()
    throws IOException
  {
    super(ResourceUtils.SPECIAL_LEXICON);
  }
}
