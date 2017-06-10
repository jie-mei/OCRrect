package edu.dal.ocrrect.util;

/**
 * The token class encapsulate the string representation and the position of a token.
 *
 * @since 2017.04.20
 */
public class Token extends LocatedTextualUnit {
  public static Token EMPTY = new Token("", -1);

  public Token(String name, int position) {
    super(name, position);
  }
}
