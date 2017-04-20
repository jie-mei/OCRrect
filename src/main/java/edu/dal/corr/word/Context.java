package edu.dal.corr.word;

import edu.dal.corr.util.LocatedTextualUnit;
import java.io.Serializable;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * An abstract representation of n-gram context.
 *
 * <p>A n-gram is a sequence of consecutive words appearing in a text. There is one pivot word in
 * this abstract n-gram representation, the rest words are the context word of this pivot words.
 * This object allows to generate more relaxed n-grams representation, where some context words can
 * be omitted. The pivot word cannot be omitted in this process.
 *
 * @since 2017.04.20
 */
public class Context extends LocatedTextualUnit implements Serializable {
  private static final long serialVersionUID = 4170733392972229406L;

  public static int MAX_CONTEXT_SIZE = 5;

  private int index;
  private String[] words;

  /**
   * Construct a n-gram context with a list of word strings as well as the index and of the pivot
   * word.
   *
   * @param idx the index of the pivot word among word strings.
   * @param position the position of the pivot word.
   * @param words an array of word strings.
   */
  public Context(int idx, int position, String... words) {
    super(words[idx], position);
    this.index = idx;
    this.words = words;
  }

  /**
   * Get words in this n-gram context.
   *
   * @return words in this n-gram context.
   */
  public String[] words() {
    return words;
  }

  /**
   * Get the index of the word in the context.
   *
   * @return An integer represents the word index.
   */
  public int index() {
    return index;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Context && super.equals(obj)) {
      Context another = (Context) obj;
      if (words.length == another.words.length) {
        for (int i = 0; i < words.length; i++) {
          if (! words[i].equals(another.words[i])) {
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
  protected HashCodeBuilder buildHash() {
    return super.buildHash()
        .append(index)
        .append(words);
  }

  /**
   * Concatenate words into a n-gram.
   * <p>
   * Every two words are separated by an whitespace character.
   *
   * @return a n-gram string.
   */
  @Override
  public String toString() {
     return String.join(" ", words);
  }
}
