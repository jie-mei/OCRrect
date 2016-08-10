package edu.dal.corr.suggest;

import static org.junit.Assert.*;

import org.junit.Test;

import edu.dal.corr.util.ResourceUtils;

public class NgramBoundedReaderSearcherTest
{
  @Test
  public void testConstruct() {
    NgramBoundedReaderSearcher nbrs = new NgramBoundedReaderSearcher(ResourceUtils.FIVEGRAM);
  }
}
