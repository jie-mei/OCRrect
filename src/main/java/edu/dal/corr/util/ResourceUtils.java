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
 * @since 2016.08.23
 */
public class ResourceUtils
{
  public static Path TEST_INPUT_SEGMENT = getResource("test.in.seg.txt");
  public static List<Path> INPUT = getResourceInDir("input", "*.txt");
  public static List<Path> GT = getResourceInDir("gt", "*.txt");

  public static Path SPECIAL_LEXICON = getResource("lexicon/special.txt");
  public static Path LEXI_LEXICON    = getResource("lexicon/lexicon.txt");
  public static Path WIKI_LEXICON    = getResource("lexicon/wiki.txt");

  public static Path GT_ERROR = getResource("error.gt.txt");

  // Retrieved from http://www.gutenberg.org/ebooks/29765
  public static Path WEBSTER_DICTIONARY = getResource("webster-dictionary.txt");

  public static Path UNIGRAM = getPath(
//      "/raid6/user/jmei/Google_Web_1T_5-gram/1gm/vocab");
      "/home/default/data/Google_Web_1T_5-gram/1gm/vocab");
  public static List<Path> FIVEGRAM = getPathsInDir(
//      "/raid6/user/jmei/Google_Web_1T_5-gram/5gms",
      "/home/default/data/Google_Web_1T_5-gram/5gms",
      "5gm-[0-9][0-9][0-9][0-9]");

  /**
   * Get resource file from compiled path.
   * 
   * @param  pathname  The resource path from compiled path.
   * @return The resource file.
   */
  public static Path getResource(String pathname)
  {
    try {
      return Paths.get(ResourceUtils.class.getClassLoader()
          .getResource(pathname).getPath());
    } catch (NullPointerException e) {
      throw new RuntimeException(
          "Error: cannot find resource \"" + pathname + "\".", e);
    }
  }
  
  public static Path getPath(String pathname)
  {
    Path p = Paths.get(pathname);
    if (! Files.exists(p)) {
      throw new RuntimeException(
          "Error: cannot find resource \"" + pathname + "\".");
    }
    return p;
  }

  private static List<Path> listPaths(Path path, String glob)
  {
    List<Path> paths = new ArrayList<>();
    try (DirectoryStream<Path> ds = Files.newDirectoryStream(path, glob)) {
      for (Path p : ds) {
        paths.add(p);
      }
    } catch (IOException e) {
      throw new RuntimeException(
          "Error: cannot find resource from \"" + path + "\".", e);
    }
    paths.sort((a, b) -> a.getFileName().compareTo(b.getFileName())); 
    return paths;
  }

  public static List<Path> getPathsInDir(String dir, String glob)
  {
    return listPaths(Paths.get(dir), glob);
  }

  public static List<Path> getResourceInDir(String dir, String glob)
  {
    return listPaths(getResource(dir), glob);
  }
}
