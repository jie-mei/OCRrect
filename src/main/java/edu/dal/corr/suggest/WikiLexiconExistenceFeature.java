package edu.dal.corr.suggest;

import java.io.IOException;

import edu.dal.corr.util.ResourceUtils;

public class WikiLexiconExistenceFeature
  extends LexiconExistenceFeature
{
  public WikiLexiconExistenceFeature()
    throws IOException
  {
    super(ResourceUtils.WIKI_LEXICON);
  }
}
