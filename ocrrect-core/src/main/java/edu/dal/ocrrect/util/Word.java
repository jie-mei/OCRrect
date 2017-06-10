package edu.dal.ocrrect.util;

import org.apache.commons.lang.builder.HashCodeBuilder;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
/**
 * An abstract representation of word. An abstract word contains the following components:
 *
 * <ul>
 *   <li>The string representation of this word.
 *   <li>All context words.
 *   <li>The offset from the beginning of the original text to the the position of the first
 *       character in the error word.
 * </ul>
 *
 * @since 2017.04.21
 */
public class Word extends LocatedTextualUnit implements Serializable {
  private static final long serialVersionUID = 1201174127991744048L;

  private String[] context;

  /**
   * Construct a word with the position and four neighboring words before and three after.
   *
   * @param position the offset from the beginning of the text.
   * @param context a series of eight words. Four words before the occurring word and three after.
   */
  public Word(int position, String... context) {
    super(checkAndGetPivot(context), position);
    this.context = context;
  }

  private static String checkAndGetPivot(String[] context) {
    if (context.length != 8) {
      System.out.print(context.length);
      throw new IllegalArgumentException("Incorrect context is given.");
    }
    return context[4];
  }

  public String[] context() {
    return context;
  }

  /**
   * Construct a context, which pivot is set using the information of this object.
   *
   * @param size the size of the n-gram context.
   * @param index the index of the pivot word in context.
   */
  private Context newContext(int size, int index) {
    int rangeStr = 4 - index;
    return new Context(index, position(),
        Arrays.copyOfRange(context, rangeStr, rangeStr + size));
  }

  /**
   * Check if n-gram size is valid.
   *
   * @param size the size of the n-gram context.
   * @throws IllegalArgumentException if size is not in a valid range.
   */
  private void checkContextSize(int size) {
    if (size > 5 || size < 1) {
      throw new IllegalArgumentException(
          "invalid context size is given: " + size);
    }
  }

  /**
   * Get all the of n-gram contexts which pivot is this word.
   *
   * @param size the size of the ngram.
   * @return a list of n-gram contexts which pivot is this word.
   */
  public List<Context> getContexts(int size) {
    checkContextSize(size);
    // Note that we omit the n-gram context starting with pivot word for efficiency reason in future
    // computation.
    return IntStream.range(1, size)
      .mapToObj(i -> newContext(size, i))
      .collect(Collectors.toList());
  }

  /**
   * Get a n-gram contexts which pivot is this word.
   *
   * @param size the size of the ngram.
   * @param index the index of the pivot word in context.
   * @return a context.
   */
  public Context getContext(int size, int index) {
    checkContextSize(size);
    if (index <= 0 && index >= size) {
      throw new IllegalArgumentException("invalid pivot index: " + index);
    }
    return newContext(size, index);
  }

  /**
   * Get the string representation of the context words.
   *
   * @return the string representation of the context words.
   */
  private String contextToString() {
    return " <\"" + String.join("\",\"", context) + "\">";
  }

  public String toString(int formatLen) {
    return String.format("%" + formatLen + "s %s", text(), contextToString());
  }

  @Override
  public String toString() {
    return text() + contextToString();
  }

  @Override
  protected HashCodeBuilder buildHash() {
    return super.buildHash().append(context);
  }
}
