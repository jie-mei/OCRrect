package edu.dal.corr.util;

import gnu.trove.set.hash.THashSet;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.List;

/**
 * @since 2016.08.10
 */
public class IOUtils
{
  private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
  
  /**
   * Create a new {@link java.io.BufferedReader} for path.
   * 
   * @param  path  A file path.
   * @return A new {@link java.io.BufferedReader} for the given path.
   * @throws IOException  If I/O error occurs.
   */
  public static BufferedReader newBufferedReader(Path path)
      throws IOException
  {
    return Files.newBufferedReader(path, DEFAULT_CHARSET);
  }

  /**
   * Create a new {@link java.io.BufferedReader} for path.
   * 
   * @param  path     A file path.
   * @param  options  Open options.
   * @return A new {@link java.io.BufferedReader} for the given path.
   * @throws IOException  If I/O error occurs.
   */
  public static BufferedWriter newBufferedWriter(Path path, OpenOption... options)
      throws IOException
  {
    return Files.newBufferedWriter(path, DEFAULT_CHARSET, options);
  }

  public static String read(Path path)
    throws IOException
  {
    /*
    StringBuilder sb = new StringBuilder();
    try (BufferedReader br = newBufferedReader(path)){
      for (String line; (line = br.readLine()) != null;) {
        sb.append(line).append('\n');
      }
    }
    return sb.toString();
    */
    return new String(Files.readAllBytes(path));
  }

  public static String read(List<Path> paths)
    throws IOException
  {
    StringBuilder sb = new StringBuilder();
    for (Path p : paths) {
      sb.append(read(p));
    }
    return sb.toString();
  }

  public static THashSet<String> readList(Path path)
    throws IOException
  {
    THashSet<String> set = new THashSet<String>();
    try (
      BufferedReader br = newBufferedReader(path)
    ){
      for (String line; (line = br.readLine()) != null;) {
        set.add(line);
      }
    }
    return set;
  }

  public static THashSet<String> readList(List<Path> path)
    throws IOException
  {
    THashSet<String> set = new THashSet<String>();
    for (Path p : path) {
      try (
        BufferedReader br = newBufferedReader(p)
      ){
        for (String line; (line = br.readLine()) != null;) {
          set.add(line.trim());
        }
      }
    }
    return set;
  }
}
