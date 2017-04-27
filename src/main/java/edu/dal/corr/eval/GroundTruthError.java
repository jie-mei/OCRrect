package edu.dal.corr.eval;

import edu.dal.corr.util.IOUtils;
import edu.dal.corr.util.LocatedTextualUnit;
import edu.dal.corr.util.LogUtils;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
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
 * @since 2017.04.24
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

  /**
   * The regex pattern contains in the deprecated error records, which is
   * intended to be omitted in the reading procedure.
   */
  private static Pattern ERROR = Pattern.compile(".*ERROR.*");

  /**
   * Read a list of ground truth errors from file.
   *
   * @param  path  path to file containing the ground truth error records.
   * @return A list of ground truth errors.
   */
  public static List<GroundTruthError> read(Path path) {
    return LogUtils.logMethodTime(1, () -> {
      List<GroundTruthError> errors = new ArrayList<>();

      try (BufferedReader br = IOUtils.newBufferedReader(path)) {
        for (String line = br.readLine(); line != null; line = br.readLine()) {
          if (ERROR.matcher(line).matches()) {
            // Omit the error record.
            continue;
          }
          String[] splits = line.split("\t", 5);
          int pos = Integer.parseInt(splits[0]);
          String errName = splits[1];
          String gtName = splits[2];
          String gtNameAscii = splits[3];
          String info = splits[4];
          errors.add(new GroundTruthError(pos, errName, gtName, gtNameAscii, info));
        }
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
      return errors;
    });
  }
}
