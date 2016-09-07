package edu.dal.corr.word;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang.builder.HashCodeBuilder;

import edu.dal.corr.util.LocatedTextualUnit;

/**
 * An abstract representation of word.
 * An abstract word contains the following components:
 * <ul>
 *  <li> The string representation of this word.
 *  <li> All context words.
 *  <li> The offset from the beginning of the original text to the the position
 *       of the first character in the error word.
 * </ul>
 *
 * @since 2016.09.07
 */
public class Word
  extends LocatedTextualUnit
  implements Serializable
{
  private static final long serialVersionUID = 1201174127991744048L;

  private String[] context;

  /**
   * Construct a word with the position and four neighboring words before and
   * three after.
   * 
   * @param  position  the offset from the beginning of the text.
   * @param  context   a series of eight words. Four words before the occurring
   *                   word and three after.
   */
  public Word(int position, String... context)
  {
    super(context[4], position);
    if (context.length != 8)
      throw new IllegalArgumentException("Incorrect context is given.");
    this.context = context;
  }
  
  /**
   * Construct a context, which pivot is set using the information of this
   * object.
   * 
   * @param  size   the size of the n-gram context.
   * @param  index  the index of the pivot word in context.
   */
  private Context newContext(int size, int index) {
    int rangeStr = 4 - index;
    return new Context(position(), index,
        Arrays.copyOfRange(context, rangeStr, rangeStr + size));
  }
  
  /**
   * Check if n-gram size is valid.
   * 
   * @param  size  the size of the n-gram context.
   * @throws IllegalArgumentException  if size is not in a valid range.
   */
  private void checkContextSize(int size)
  {
    if (size > 5 || size < 1) {
      throw new IllegalArgumentException(
          "invalid context size is given: " + size);
    }
  }
  
  /**
   * Get all the of n-gram contexts which pivot is this word.
   * 
   * @param  size  the size of the ngram.
   *
   * @return a list of n-gram contexts which pivot is this word.
   */
  public List<Context> getContexts(int size)
  {
    checkContextSize(size);
    // Note that we omit the n-gram context starting with pivot word for
    // efficiency reason in future computation.
    return IntStream.range(1, size - 1)
      .mapToObj(i -> newContext(size, i))
      .collect(Collectors.toList());
  }

  /**
   * Get a n-gram contexts which pivot is this word.
   * 
   * @param  size  the size of the ngram.
   * @param  index  the index of the pivot word in context.
   * @return a context.
   */
  public Context getContext(int size, int index)
  {
    checkContextSize(size);
    if (index <= 0 && index >= size) {
      throw new IllegalArgumentException("invalid pivot index: " + index);
    }
    return newContext(size, index);
  }
  
  /**
   * Construct a new word, where all context words are transformed by a mapper
   * function.
   * 
   * @param  mapper  a mapper function.
   * @return a new word, where .
   */
  public Word mapTo(Function<String, String> mapper)
  {
    String[] mapped = new String[8];
    for (int i = 0; i < context.length; i++) {
      mapped[i] = mapper.apply(context[i]);;
    }
    return new Word(position(), mapped);
  }
  
  /**
   * Get the string representation of the context words.
   *
   * @return the string representation of the context words.
   */
  String contextToString() {
    return " <\"" + String.join("\",\"", context) + "\">";
  }
  
  @Override
  public String toString() {
    return text() + contextToString();
  }
  
  @Override
  protected HashCodeBuilder buildHash()
  {
    return super.buildHash().append(context);
  }
}
