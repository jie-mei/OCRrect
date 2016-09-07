package edu.dal.corr.eval;

import org.apache.commons.lang.builder.HashCodeBuilder;

import edu.dal.corr.util.LocatedTextualUnit;

/**
 * An abstract representation of a ground truth error.
 * An abstract ground truth error contains the following components:
 * <ul>
 *  <li> The string representation of the error word.
 *  <li> The string representation of the correct word according to the error
 *       word.
 *  <li> The offset from the beginning of the original text to the the position
 *       of the first character in the error word.
 *  <li> Addition note of this errors.
 * </ul>
 *
 * @since 2016.09.07
 */
public class GroundTruthError
  extends LocatedTextualUnit
{
  private static final long serialVersionUID = 480517897613831371L;

  private String errText;
  private String info;
  
  /**
   * @param  groundTruthText  the string representation of the correction.
   * @param  errorText        the string representation of the error word.
   * @param  position         the offset from the beginning of the original text
   *                          to the the position of the first character in the
   *                          error word.
   * @param  information      addition notes of this error.
   */
  GroundTruthError(String groundTruthText,
                   String errorText,
                   int position,
                   String information)
  {
    super(groundTruthText, position);
    errText = errorText;
    info = information;
  }
  
  /**
   * Get the string representation of the error word.
   *
   * @return the string representation of the error word.
   */
  public String errorText() {
    return errText;
  }

  /**
   * Get the addition note of the errors.
   *
   * @return the addition note of the errors.
   */
  public String info() {
    return info;
  }
  
  @Override
  public String toString()
  {
    return String.format("<%s>",
        String.join(", ", text(), errText, Integer.toString(position()), info));
  }
  
  @Override
  protected HashCodeBuilder buildHash()
  {
    return super.buildHash()
        .append(errText)
        .append(info);
  }
}
