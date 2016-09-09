package edu.dal.corr.util;

import java.io.Serializable;

import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * A located textual unit provides the position of the textual entity. The
 * position refers to the offset to the beginning of a text string.
 * 
 * @since 2016.07.26
 */
public abstract class LocatedTextualUnit
  extends TextualUnit
  implements Serializable
{
  private static final long serialVersionUID = -780573715784587087L;

  private int position;

  protected LocatedTextualUnit(final String name, final int position)
  {
    super(name);
    this.position = position;
  }

  /**
   * Get the position of the entity.
   * 
   * @return The offset to the beginning of a text string.
   */
  public int position()
  {
    return position;
  }
  
  @Override
  protected HashCodeBuilder buildHash()
  {
    return super.buildHash().append(position);
  }

  @Override
  public boolean equals(Object obj)
  {
    if (obj instanceof LocatedTextualUnit
        && super.equals(obj)) {
      return position == ((LocatedTextualUnit) obj).position;
    }
    return false;
  }
}
