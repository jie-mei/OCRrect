package edu.dal.corr.word;

import java.io.Serializable;

import edu.dal.corr.util.LocatedTextualUnit;

/**
* The token class encapsulate the string representation and the position of a
* token.
*/
public class Token
  extends LocatedTextualUnit
  implements Serializable
{
  private static final long serialVersionUID = -1616371049278442444L;

  Token(String name, int position)
  {
    super(name, position);
  }
}
