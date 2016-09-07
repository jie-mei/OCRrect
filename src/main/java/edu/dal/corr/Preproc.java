package edu.dal.corr;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import edu.dal.corr.suggest.NgramBoundedReaderSearcher;
import edu.dal.corr.suggest.NgramBoundedReaderSearchers;
import edu.dal.corr.util.PathUtils;
import edu.dal.corr.util.ResourceUtils;

/**
 * @since 2016.09.07
 */
public class Preproc
{
  private static Path BIGRAM_SEARCHER_FILE   = PathUtils.getTempPath("2gm.search");
  private static Path TRIGRAM_SEARCHER_FILE  = PathUtils.getTempPath("3gm.search");
  private static Path FOURGRAM_SEARCHER_FILE = PathUtils.getTempPath("4gm.search");
  private static Path FIVEGRAM_SEARCHER_FILE = PathUtils.getTempPath("5gm.search");
  
  /**
   * Generate n-gram searchers using n-gram resources (n > 1).
   * 
   * @param  ngramData  path to a list of n-gram data files.
   * @param  preproc    path to the pre-processed searcher object file.
   * @throws IOException  if I/O error occurs
   * @throws FileNotFoundException  if file not found.
   */
  public static void genNgramSearcher(List<Path> ngramData, Path preproc)
      throws FileNotFoundException, IOException
  {
    NgramBoundedReaderSearcher searcher = new NgramBoundedReaderSearcher(ngramData);
    NgramBoundedReaderSearchers.write(searcher, preproc);
  }
  
  public static void main(String[] args)
    throws Exception
  {
    Files.createDirectories(PathUtils.TEMP_DIR);
    genNgramSearcher(ResourceUtils.BIGRAM,   BIGRAM_SEARCHER_FILE);
    genNgramSearcher(ResourceUtils.TRIGRAM,  TRIGRAM_SEARCHER_FILE);
    genNgramSearcher(ResourceUtils.FOURGRAM, FOURGRAM_SEARCHER_FILE);
    genNgramSearcher(ResourceUtils.FIVEGRAM, FIVEGRAM_SEARCHER_FILE);
  }
}
