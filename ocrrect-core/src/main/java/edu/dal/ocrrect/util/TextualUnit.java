package edu.dal.ocrrect.util;

import org.apache.commons.lang.builder.HashCodeBuilder;

import java.io.Serializable;

/**
 * A textual unit is a container for a piece of text.
 *
 * @since 1.0
 */
public abstract class TextualUnit implements Serializable {

  private String text;

  protected TextualUnit(final String text) {
    this.text = text;
  }

  public String getText() {
    return text;
  }

  protected HashCodeBuilder buildHash() {
    return new HashCodeBuilder().append(text);
  }

  @Override
  public int hashCode() {
    return buildHash().toHashCode();
  }

  /**
   * Two textual units are equal when they refer to the same underlying text.
   *
   * @param  another  Another object.
   * @return {@code True} if the given object is a textual unit and refers to the same text
   *    sequence.
   */
  @Override
  public boolean equals(Object another) {
    return another instanceof TextualUnit && hashCode() == another.hashCode();
  }
}
