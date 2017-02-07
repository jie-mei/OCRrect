package edu.dal.corr.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Path;
import java.util.Set;

import gnu.trove.map.hash.TObjectLongHashMap;

/**
 * Unigram object for supporting frequency lookup. This class is implemented in
 * a thread-safe lazy-loading singleton manner.
 *
 * @since 2016.07.26
 */
public class Unigram
  implements Serializable
{
  private static final long serialVersionUID = -1614121007280050705L;

  private static Unigram instance = null;

  public static synchronized Unigram getInstance()
  {
    if (instance == null) {
      instance = new Unigram();
    }
    return instance;
  }

  private TObjectLongHashMap<String> map;

  private Unigram()
  {
    this(ResourceUtils.UNIGRAM);
  }
  
  /**
   * Construct a unigram with file.
   * 
   * @param unigram  a path to the unigram file.
   */
  public Unigram(Path unigram)
  {
    map = new TObjectLongHashMap<String>();
    try (
      BufferedReader br = IOUtils.newBufferedReader(unigram)
    ){
      for (String line; (line = br.readLine()) != null;) {
        String[] fields = line.split("\t");
        String gram = fields[0];
        long freq = Long.parseLong(fields[1]);
        map.put(gram, freq);
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
  
  /**
   * Register an instance as the default for {@link #getInstance()}.
   * 
   * @param  unigram  an unigram instance.
   */
  public static void register(Unigram unigram)
  {
    instance = unigram;
  }
  
  /**
   * Get word frequency in the unigram corpus.
   *
   * @param  word  A word string.
   * @return The word frequency in the unigram corpus.
   */
  public Long freq(String word)
  {
    return map.get(word);
  }
  
  /**
   * Check whether word exists in the unigram corpus.
   * 
   * @param  word A word string.
   * @return {@code True} if the given word exists in the unigram corpus.
   */
  public boolean contains(String word)
  {
    return map.containsKey(word);
  }
  
  public Set<String> keys()
  {
    return map.keySet();
  }
}
