package edu.dal.corr.eval;

import edu.dal.corr.util.LocatedTextualUnit;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * An abstract representation of a ground truth error. An abstract ground truth error contains the
 * following components:
 *
 * <ul>
 *   <li>The string representation of the error word.
 *   <li>The string representation of the correct word according to the error word.
 *   <li>The offset from the beginning of the original text to the the position of the first
 *       character in the error word.
 *   <li>Addition note of this errors.
 * </ul>
 *
 * @since 2017.04.20
 */
public class GroundTruthError extends LocatedTextualUnit {
  private static final long serialVersionUID = 480517897613831371L;

  private String gtText;
  private String gtTextAscii;
  private String info;

  /**
   * @param groundTruthText the string representation of the correction.
   * @param errorText the string representation of the error word.
   * @param position the offset from the beginning of the original text to the the position of the
   *     first character in the error word.
   * @param information addition notes of this error.
   */
  GroundTruthError(
      int position,
      String errorText,
      String groundTruthText,
      String groundTruthAscii,
      String information) {
    super(errorText, position);
    gtText = groundTruthText;
    gtTextAscii = groundTruthAscii;
    info = information;
  }

  /**
   * Get the string representation of the error word.
   *
   * @return the string representation of the error word.
   */
  public String errorText() {
    return text();
  }

  /**
   * Get the string representation of the error word.
   *
   * @return the string representation of the error word.
   */
  public String gtText() {
    return gtText;
  }

  public String gtTextAscii() {
    return gtTextAscii.length() == 0 ? gtText : gtTextAscii;
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
  public String toString() {
    return String.format("<%s>", String.join(", ",
        Integer.toString(position()), text(), gtText, info));
  }

  @Override
  protected HashCodeBuilder buildHash() {
    return super.buildHash()
        .append(gtText)
        .append(info);
  }
}
