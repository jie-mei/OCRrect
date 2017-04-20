package edu.dal.corr.util;

import java.io.Serializable;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * A textual unit is container for a piece of text.
 *
 * @since 2017.04.20
 */
public abstract class TextualUnit implements Serializable {
  private static final long serialVersionUID = 3195392798284827598L;

  private String text;

  protected TextualUnit(final String text) {
    this.text = text;
  }

  /**
   * Get the entity name.
   *
   * @return  A name string.
   */
  public String text() {
    return text;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof TextualUnit) {
      return text.equals(((TextualUnit) obj).text);
    }
    return false;
  }

  protected HashCodeBuilder buildHash() {
    return new HashCodeBuilder().append(text);
  }

  @Override
  public int hashCode() {
    return buildHash().toHashCode();
  }

  @Override
  public String toString() {
    return text;
  }
}
