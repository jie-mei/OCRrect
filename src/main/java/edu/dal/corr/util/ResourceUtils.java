package edu.dal.corr.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * The utility class for declaring resource path.
 *
 * @since 2017.01.18
 */
public class ResourceUtils
{
  private ResourceUtils() {}

  /*
   * Input error texts.
   */
  public static Path TEST_INPUT_SEGMENT = getResource("test.in.seg.txt");
  //public static List<Path> INPUT = getResourceInDir("*.txt", "input");
  //public static List<Path> GT = getResourceInDir("*.txt", "gt");
  public static List<Path> INPUT = getPathsInDir("*.txt", "data/ocr");
  public static List<Path> GT = getPathsInDir("*.txt", "data/gt");

  /*
   * Lexicons.
   */
  public static Path VOCAB = getResource("search_vocab.txt");
  public static Path SPECIAL_LEXICON = getResource("lexicon/special.txt");
  public static Path LEXI_LEXICON    = getResource("lexicon/lexicon.txt");
  public static Path WIKI_LEXICON    = getResource("lexicon/wiki.txt");

  /*
   * Ground truth error information.
   */
  public static Path GT_ERROR = getResource("error.gt.txt");

  /*
   * Dictionaries.
   */
  // Retrieved from http://www.gutenberg.org/ebooks/29765
  public static Path WEBSTER_DICTIONARY = getResource(
      "dictionary/webster/webster-dictionary.txt");

  /*
   * Ngram corpus.
   */
  public static Path UNIGRAM = tryAndGetPath(
      "/raid6/user/jmei/Google_Web_1T_5-gram/1gm/vocab",
      "/home/default/data/Google_Web_1T_5-gram/1gm/vocab");

  public static List<Path> BIGRAM = tryAndGetPathsInDir(
      "2gm-[0-9][0-9][0-9][0-9]",
      "/raid6/user/jmei/Google_Web_1T_5-gram/2gms",
      "/home/default/data/Google_Web_1T_5-gram/2gms");

  public static List<Path> TRIGRAM = tryAndGetPathsInDir(
      "3gm-[0-9][0-9][0-9][0-9]",
      "/raid6/user/jmei/Google_Web_1T_5-gram/3gms",
      "/home/default/data/Google_Web_1T_5-gram/3gms");

  public static List<Path> FOURGRAM = tryAndGetPathsInDir(
      "4gm-[0-9][0-9][0-9][0-9]",
      "/raid6/user/jmei/Google_Web_1T_5-gram/4gms",
      "/home/default/data/Google_Web_1T_5-gram/4gms");

  public static List<Path> FIVEGRAM = tryAndGetPathsInDir(
      "5gm-[0-9][0-9][0-9][0-9]",
      "/raid6/user/jmei/Google_Web_1T_5-gram/5gms",
      "/home/default/data/Google_Web_1T_5-gram/5gms");

  /**
   * Get resource file from compiled path.
   * 
   * @param  pathname  the resource path from the compiled path.
   * @return the resource file.
   */
  public static Path getResource(String pathname)
  {
    try {
      return Paths.get(ResourceUtils.class.getClassLoader()
        .getResource(pathname).getPath());
    } catch (NullPointerException e) {
      throw new ResourceNotFoundException(pathname);
    }
  }
  
  /**
   * Get resource file from system path.
   * 
   * @param  pathname  the resource path in the file system.
   * @return the resource file.
   */
  public static Path getPath(String pathname)
  {
    Path p = Paths.get(pathname);
    if (! Files.exists(p)) {
      throw new ResourceNotFoundException(pathname);
    }
    return p;
  }
  
  /**
   * Try to retrieve path from one of the given pathnames using {@link
   * #getPathsInDir(String, String)}. The given directories are
   * tried in order.
   * 
   * @param  pathnames  a list of pathname.
   * @return a path from the first valid pathname in the list.
   * @throws ResourceNotFoundException  if none of the given pathname is valid.
   */
  public static Path tryAndGetPath(String... pathnames)
  {
    for (String pathname : pathnames) {
      try {
        return getPath(pathname);
      } catch(ResourceNotFoundException e) {
      }
    }
    throw new ResourceNotFoundException();
  }

  /**
   * Retrieve all subpaths in the given pathname that have been filtered by a
   * wildcard.
   * 
   * @param  glob  a wildcard pattern for filtering the retrieved subpaths.
   * @param  dir   a folder pathname in the file system.
   * @return all pathnames in the given pathname  have been filtered by a
   *    wildcard.
   */
  public static List<Path> getPathsInDir(String glob, String dir) {
    try {
      return PathUtils.listPaths(Paths.get(dir), glob);
    } catch (IOException e) {
      throw new ResourceNotFoundException(e);
    }
  }
  
  /**
   * Try to retrieve paths from one of the given directories using {@link
   * #getPathsInDir(String, String)}. The given directories are tried in order.
   * 
   * @param  glob  a wildcard pattern for filtering the retrieved subpaths.
   * @param  dir   a list of folder pathname in the file system.
   * @return a list of subpaths from the first valid directory in the list.
   * @throws ResourceNotFoundException  if none of the given directories is
   *                                    valid.
   */
  public static List<Path> tryAndGetPathsInDir(String glob, String... dirs)
  {
    for (String dir : dirs) {
      try {
        return getPathsInDir(glob, dir);
      } catch(ResourceNotFoundException e) {
      }
    }
    throw new ResourceNotFoundException();
  }

  /**
   * Retrieve all subpaths in the given pathname and filter by a wildcard.
   * 
   * @param  glob  a wildcard pattern for filtering the retrieved subpaths.
   * @param  dir   a folder pathname in the file system.
   * @return a list of pathname.
   */
  public static List<Path> getResourceInDir(String glob, String dir) {
    try {
      return PathUtils.listPaths(getResource(dir), glob);
    } catch (IOException e) {
      throw new ResourceNotFoundException(e);
    }
  }
}
