package edu.dal.corr.word;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import org.apache.commons.lang.builder.HashCodeBuilder;

import edu.dal.corr.util.LocatedTextualUnit;

/**
 * An occurrence of a word in text.
 *
 * @since 2016.07.26
 */
public class Word
  extends LocatedTextualUnit
  implements Serializable
{
  private static final long serialVersionUID = 1201174127991744048L;

  private String[] context;

  /**
   * Construct the word occurrence object with the position and four neighboring
   * words before and three after.
   * 
   * @param  position  The offset from the beginning of the text.
   * @param  context   A series of eight words. Four words before the occurring
   *    word and three after.
   */
  public Word(int position, String... context)
  {
    super(context[4], position);
    if (context.length != 8)
      throw new IllegalArgumentException("Incorrect context is given.");
    this.context = context;
  }
  
  Word(String word)
  {
    super(word, -1);
  }
  
  public List<Context> getContexts()
  {
    return Arrays.asList(
      new Context(position(), 4, Arrays.copyOfRange(context, 0, 5)),
      new Context(position(), 3, Arrays.copyOfRange(context, 1, 6)),
      new Context(position(), 2, Arrays.copyOfRange(context, 2, 7)),
      new Context(position(), 1, Arrays.copyOfRange(context, 3, 8)));
  }

  public Context getContext(int index)
  {
    switch(index) {
      case 1:  new Context(position(), 1, Arrays.copyOfRange(context, 3, 8));
      case 2:  new Context(position(), 2, Arrays.copyOfRange(context, 2, 7));
      case 3:  new Context(position(), 3, Arrays.copyOfRange(context, 1, 6));
      case 4:  new Context(position(), 4, Arrays.copyOfRange(context, 0, 5));
      default: throw new IllegalArgumentException(
                   "Incorrect context index: " + index);
    }
  }
  
  public Word mapTo(Function<String, String> mapper)
  {
    String[] mapped = new String[8];
    for (int i = 0; i < context.length; i++) {
      mapped[i] = mapper.apply(context[i]);;
    }
    return new Word(position(), mapped);
  }
  
  String info()
  {
    return " <\"" + String.join("\",\"", context) + "\">";
  }
  
  @Override
  protected HashCodeBuilder buildHash()
  {
    return super.buildHash().append(context);
  }
}
