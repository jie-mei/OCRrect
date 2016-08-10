package edu.dal.corr.word;

import java.io.Serializable;

import edu.dal.corr.util.LocatedTextualUnit;

/**
 * A five-gram context of a text entity.
 *
 * @since 2016.07.26
 */
public class Context
  extends LocatedTextualUnit
  implements Serializable
{
  private static final long serialVersionUID = 4170733392972229406L;

  private int index;
  private String[] words;

  Context(int position, int idx, String... words)
  {
    super(words[idx], position);
    this.index = idx;
    this.words = words;
  }
  
  public String[] words() { return words; }

  /**
   * Get the index of the word in the context.
   * 
   * @return An integer represents the word index.
   */
  public int index() { return index; }
}
