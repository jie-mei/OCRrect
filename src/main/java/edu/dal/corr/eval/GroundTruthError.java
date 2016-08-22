package edu.dal.corr.eval;

import edu.dal.corr.util.LocatedTextualUnit;

public class GroundTruthError
  extends LocatedTextualUnit
{
  private static final long serialVersionUID = 480517897613831371L;

  private String errText;
  private String info;
  
  protected GroundTruthError(String groundTruthText, String errorText,
      int position, String information)
  {
    super(groundTruthText, position);
    errText = errorText;
    info = information;
  }
  
  public String errorText() { return errText; }

  public String info() { return info; }
  
  public String toString()
  {
    return String.format("<%s>",
        String.join(", ", text(), errText, Integer.toString(position()), info));
  }
}
