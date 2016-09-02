package edu.dal.corr.util;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * The utility class for declaring resource path.
 *
 * @since 2016.09.01
 */
public class ResourceUtils
{
  /*
   * Input error texts.
   */
  public static Path TEST_INPUT_SEGMENT = getResource("test.in.seg.txt");
  public static List<Path> INPUT = getResourceInDir("input", "*.txt");
  public static List<Path> GT = getResourceInDir("gt", "*.txt");

  /*
   * Lexicons.
   */
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
  public static Path UNIGRAM = null;
  static {
    for (String pname : new String[]{
      "/raid6/user/jmei/Google_Web_1T_5-gram/1gm/vocab",
      "/home/default/data/Google_Web_1T_5-gram/1gm/vocab"
    }) {
      try {
        UNIGRAM = getPath(pname);
        break;
      } catch(ResourceNotFoundException err) {
      }
    }
    if (UNIGRAM == null) {
      throw new RuntimeException("Error: cannot find resource\"unigram\".");
    }
  }
  public static List<Path> FIVEGRAM = null;
  static {
    for (String pname : new String[]{
      "/raid6/user/jmei/Google_Web_1T_5-gram/5gms",
      "/home/default/data/Google_Web_1T_5-gram/5gms"
    }) {
      try {
        FIVEGRAM = getPathsInDir(pname, "5gm-[0-9][0-9][0-9][0-9]");
        break;
      } catch(ResourceNotFoundException err) {
      }
    }
    if (FIVEGRAM == null) {
      throw new RuntimeException("Error: cannot find resource\"fivegram\".");
    }
  }

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
      throw new ResourceNotFoundException(
          "Error: cannot find resource \"" + pathname + "\".", e);
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
      throw new ResourceNotFoundException(
          "Error: cannot find resource \"" + pathname + "\".");
    }
    return p;
  }

  /**
   * Retrieve all subpaths in the given pathname and filter by a wildcard.
   * 
   * @param  path  the resource folder in the file system.
   * @param  glob  a wildcard pattern for filtering the retrieved subpaths.
   * @return a list of subpaths.
   */
  private static List<Path> listPaths(Path path, String glob)
  {
    List<Path> paths = new ArrayList<>();
    try (DirectoryStream<Path> ds = Files.newDirectoryStream(path, glob)) {
      for (Path p : ds) {
        paths.add(p);
      }
    } catch (IOException e) {
      throw new ResourceNotFoundException(
          "Error: cannot find resource from \"" + path + "\".", e);
    }
    paths.sort((a, b) -> a.getFileName().compareTo(b.getFileName())); 
    return paths;
  }

  /**
   * Retrieve all subpaths in the given pathname and filter by a wildcard.
   * 
   * @param  dir   a folder pathname in the compiled path.
   * @param  glob  a wildcard pattern for filtering the retrieved subpaths.
   * @return
   */
  public static List<Path> getPathsInDir(String dir, String glob) {
    return listPaths(Paths.get(dir), glob);
  }

  /**
   * Retrieve all subpaths in the given pathname and filter by a wildcard.
   * 
   * @param  dir   a folder pathname in the file system.
   * @param  glob  a wildcard pattern for filtering the retrieved subpaths.
   * @return
   */
  public static List<Path> getResourceInDir(String dir, String glob) {
    return listPaths(getResource(dir), glob);
  }
}
