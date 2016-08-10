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

  public static BufferedWriter newBufferedWriter(Path path, OpenOption... options)
      throws IOException
  {
    return Files.newBufferedWriter(path, DEFAULT_CHARSET, options);
  }

  public static String read(Path path)
    throws IOException
  {
    StringBuilder sb = new StringBuilder();
    try (
      BufferedReader br = newBufferedReader(path)
    ){
      for (String line; (line = br.readLine()) != null;) {
        sb.append(line).append('\n');
      }
    }
    return sb.toString();
  }

  public static THashSet<String> readList(Path path)
    throws IOException
  {
    THashSet<String> dic = new THashSet<String>();
    try (
      BufferedReader br = newBufferedReader(path)
    ){
      for (String line; (line = br.readLine()) != null;) {
        dic.add(line);
      }
    }
    return dic;
  }
}
