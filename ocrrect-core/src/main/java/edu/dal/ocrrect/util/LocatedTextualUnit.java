package edu.dal.ocrrect.util;

import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * A located textual unit provides the position of the textual entity. The position refers to the
 * offset to the beginning of a text string.
 *
 * @since 1.0
 */
public abstract class LocatedTextualUnit extends TextualUnit {

  public static final int UNASSIGNED_POSITION = -1;

  private int position;

  /**
   * Validate a given position value. A valid position should be a non-negative integer value.
   *
   * @param  position  A position value.
   * @return {@True} if the given position is valid.
   */
  private boolean validatePosition(int position) {
    return position >= 0;
  }

  /**
   * Construct a located textual unit with the given name and position.
   *
   * @param  name  A text string.
   * @param  position  A non-negative integer indicates the unit in text.
   */
  protected LocatedTextualUnit(final String name, final int position) {
    super(name);
    if (! validatePosition(position)) {
      throw new IllegalArgumentException("Construct located textual unit with a invalid position: "
          + position);
    }
    this.position = position;
  }

  protected LocatedTextualUnit(final String name) {
    super(name);
    this.position = UNASSIGNED_POSITION;
  }

  public int getPosition() {
    if (position == UNASSIGNED_POSITION) {
      throw new PositionUnassignedException();
    }
    return position;
  }

  public void setPosition(int position) {
    if (! validatePosition(position)) {
      throw new IllegalArgumentException("Set position with negative value: " + position);
    }
    this.position = position;
  }

  @Override
  protected HashCodeBuilder buildHash() {
    return super.buildHash().append(position);
  }

  @Override
  public boolean equals(Object another) {
    return another instanceof LocatedTextualUnit && hashCode() == another.hashCode();
  }
}
