package edu.dal.corr.util;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * The util class for declaring resource path.
 *
 * @since 2016.07.24
 */
public class ResourceUtils
{
  public static Path INPUT_FOLDER = getResource("ocr");
  public static Path TEST_INPUT_SEGMENT = getResource("test.in.seg.txt");
  public static Path SPECIAL_LIST = getResource("specialList.txt");
  public static Path LEXICON_LIST = getResource("lexiconList.txt");

  // Retrieved from http://www.gutenberg.org/ebooks/29765
  public static Path WEBSTER_DICTIONARY = getResource("webster-dictionary.txt");

  // public static Path WIKI_LIST = getResource("wiki.txt");
  public static Path UNIGRAM = Paths.get("/home/default/data/Google_Web_1T_5-gram/1gm/vocab");
  public static List<Path> FIVEGRAM = getPathsInDir(
      "/home/default/data/Google_Web_1T_5-gram/5gms", "5gm-[0-9][0-9][0-9][0-9]");

  /**
   * Get resource file from compiled path.
   * 
   * @param  pathname  The resource path from compiled path.
   * @return The resource file.
   */
  public static Path getResource(String pathname)
  {
    try {
      return Paths.get(ResourceUtils.class.getClassLoader().getResource(pathname).getPath());
    } catch (NullPointerException e) {
      throw new RuntimeException("Error: cannot find resource from \"" + pathname + "\".", e);
    }
  }

  public static List<Path> getPathsInDir(String dir, String glob)
  {
    List<Path> paths = new ArrayList<>();
    try (DirectoryStream<Path> ds = Files.newDirectoryStream(
        Paths.get(dir), glob)
    ){
      for (Path p : ds) {
        paths.add(p);
      }
    } catch (IOException e) {
      throw new RuntimeException(
          "Error: cannot find resource from \"" + dir + "\".", e);
    }
    paths.sort((a, b) -> a.getFileName().compareTo(b.getFileName())); 
    return paths;
  }

  public static List<Path> getResourceInDir(String dir, String glob)
  {
    List<Path> paths = new ArrayList<>();
    try (DirectoryStream<Path> ds = Files.newDirectoryStream(
        getResource(dir), glob)
    ){
      for (Path p : ds) {
        paths.add(p);
      }
    } catch (IOException e) {
      throw new RuntimeException(
          "Error: cannot find resource from \"" + dir + "\".", e);
    }
    paths.sort((a, b) -> a.getFileName().compareTo(b.getFileName())); 
    return paths;
  }
  
  public static List<Path> getResourceList(String dir, String glob)
  {
    Path folder = getResource(dir);
    try (DirectoryStream<Path> ds = Files.newDirectoryStream(folder, "*.txt")) {
      
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }
}
