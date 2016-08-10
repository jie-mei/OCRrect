package edu.dal.corr.util;

import java.io.Serializable;

/**
 * A textual unit is container for a piece of text.
 * 
 * @since 2016.08.10
 */
public abstract class TextualUnit
  implements Serializable
{
  private static final long serialVersionUID = 3195392798284827598L;

  private String text;

  protected TextualUnit() {}

  protected TextualUnit(final String text)
  {
    this.text = text;
  }

  /**
   * Get the entity name.
   * 
   * @return  A name string.
   */
  public String text()
  {
    return text;
  }

  @Override
  public String toString()
  {
    return text;
  }
}
