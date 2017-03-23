package edu.dal.corr.word;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.log4j.Logger;

import edu.dal.corr.util.LocatedTextualUnit;
import edu.dal.corr.word.filter.WordFilter;

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
  private static final Logger LOG = Logger.getLogger(Word.class);

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
    super(checkAndGetPivot(context), position);
    this.context = context;
  }
  
  private static String checkAndGetPivot(String[] context)
  {
    if (context.length != 8)
      throw new IllegalArgumentException("Incorrect context is given.");
    return context[4];
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
    return new Context(index, position(),
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
    return IntStream.range(1, size)
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
   * Construct a new word with a new pivot word string.
   * <p>
   * The pivot word string is provided by the given mapper function. Position
   * and all context words and remain unchanged in the generated word.
   * 
   * @param  mapper  a mapper function that changes the string representation of
   *    the pivot word.
   * @return a new word object with a different text name.
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
  protected HashCodeBuilder buildHash()
  {
    return super.buildHash().append(context);
  }

  /*
   * Generate words from tokens.
   */
  private static List<Word> getImpl(List<Word> words, WordFilter... filters)
  {
    WordFilter.filter(words, filters);

    if (LOG.isTraceEnabled()) {
      int lenMax = words.stream()
          .mapToInt(w -> w.text().length())
          .max()
          .getAsInt();
      words.stream()
          .map(w -> w.toString(lenMax))
          .forEach(LOG::trace);
    }
    return words;
  }
  
  /**
   * Generate words from tokens.
   * 
   * @param  tokens  a list of tokens, sorted by their positions in text.
   * @param  filters  an array of word filters.
   * @return a list of words.
   */
  public static List<Word> get(List<Token> tokens, WordFilter... filters)
  {
    List<Token> expend = new ArrayList<>();

    for (int i = 0; i < 4; i++) expend.add(Token.EMPTY);
    expend.addAll(tokens);
    for (int i = 0; i < 3; i++) expend.add(Token.EMPTY);

    List<Word> words = new ArrayList<>();
    for (int i = 0; i < tokens.size(); i++) {
      words.add(new Word(expend.get(i + 4).position(),
          expend.get(i).text(),
          expend.get(i + 1).text(),
          expend.get(i + 2).text(),
          expend.get(i + 3).text(),
          expend.get(i + 4).text(),
          expend.get(i + 5).text(),
          expend.get(i + 6).text(),
          expend.get(i + 7).text()
          ));
    }
    return getImpl(words, filters);
  }

  /**
   * Generate words from text.
   * 
   * @param  content  a text string.
   * @param  tokenizer  a tokenizer.
   * @param  filters  an array of word filters.
   * @return a list of words.
   * @throws IOException 
   */
  public static List<Word> get(String content, WordTokenizer tokenizer, WordFilter... filters) throws IOException
  {
    List<Word> words = WordTokenizer.tokenize(content, tokenizer);
    return getImpl(words, filters);
  }
  
  /**
   * Generate words from text.
   * 
   * @param  content  a text string.
   * @param  tokenizer  a tokenizer.
   * @return a list of words.
   * @throws IOException 
   */
  public static List<Word> get(String content, WordTokenizer tokenizer) throws IOException {
    return get(content, tokenizer, new WordFilter[0]);
  }
}
