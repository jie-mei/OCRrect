package edu.dal.corr.eval;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import edu.dal.corr.util.IOUtils;
import edu.dal.corr.util.LogUtils;
import edu.dal.corr.util.ResourceUtils;
import gnu.trove.map.hash.TLongObjectHashMap;

/**
 * This class defines the static procedures for operating {@code
 * GroundTruthError} objects.
 *
 * @since 2017.03.02
 */
public class GroundTruthErrors
{
  private GroundTruthErrors() {}
  
  /**
   * Convert two integer value to a unique long value.
   * 
   * @return a long value.
   */
  private static long toLong(int val1, int val2) {
    return Integer.MAX_VALUE * val1 + val2;
  }

  /**
   * The regex pattern contains in the deprecated error records, which is
   * intended to be omitted in the reading procedure.
   */
  private static Pattern ERROR = Pattern.compile(".*ERROR.*");
  
  /**
   * ASCII charset.
   */
  private static CharsetEncoder ASCII_ENCODER =
      Charset.forName("US-ASCII").newEncoder();

  /**
   * Read a list of ground truth errors from file.
   *
   * @param  path  path to file containing the ground truth error records.
   * @return A list of groud truth errors.
   */
  public static List<GroundTruthError> read(Path path)
  {
    return LogUtils.logMethodTime(1, () ->
    {
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

  
  public static void main(String[] args)
  {
    List<GroundTruthError> errors = GroundTruthErrors.read(ResourceUtils.GT_ERROR);
    errors.forEach(e -> {
//      if (! ASCII_ENCODER.canEncode(e.gtText())) {
//        System.out.println(e.gtText());
        System.out.println(e);
//      }
    });
  }
}
