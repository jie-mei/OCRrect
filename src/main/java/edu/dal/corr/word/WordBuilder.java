package edu.dal.corr.word;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * @since 2016.08.10
 */
class WordBuilder
{
  private Token[] context;

  public WordBuilder(Token... context)
  {
    if (context.length != 8)
      throw new IllegalArgumentException("Incorrect context is given.");
    this.context = context;
  }
  
  public WordBuilder set(int index, Token word)
  {
    context[index] = word;
    return this;
  }
  
  public Word build()
  {
    return new Word(context[4].position(),
        Arrays.asList(context)
            .stream()
            .map(c -> c.text())
            .collect(Collectors.toList())
            .toArray(new String[context.length]));
  }
}
