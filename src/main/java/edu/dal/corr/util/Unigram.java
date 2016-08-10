package edu.dal.corr.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Set;

import gnu.trove.map.hash.TObjectLongHashMap;

/**
 * Unigram object for supporting frequency lookup. This class is implemented in
 * a thread-safe lazy-loading singleton manner.
 *
 * @since 2016.07.26
 */
public class Unigram
{
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
    map = new TObjectLongHashMap<String>();
    try (
      BufferedReader br = IOUtils.newBufferedReader(ResourceUtils.UNIGRAM)
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
