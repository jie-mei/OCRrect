package edu.dal.corr.word;

import java.io.Serializable;

import org.apache.commons.lang.builder.HashCodeBuilder;

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

  public Context(int position, int idx, String... words)
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

  public String toNgram()
  {
    return String.join(" ", words);
  }

  @Override
  public boolean equals(Object obj)
  {
    if (obj instanceof Context && super.equals(obj)) {
      Context another = (Context) obj;
      if (words.length == another.words.length) {
        for (int i = 0; i < words.length; i++) {
          if (words[i] != another.words[i]) {
            return false;
          }
        }
      }
      return index == another.index;
    } else {
      return false;
    }
  }
  
  @Override
  public HashCodeBuilder buildHash()
  {
    return super.buildHash()
        .append(index)
        .append(words);
  }
  
  @Override
  public String toString()
  {
    return toNgram();
  }
}
