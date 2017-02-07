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
 * @since 2017.01.18
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
   * A mapping from a non-ASCII pattern (1-2 characters) to a list of its ASCII
   * replacement.
   */
  private static TLongObjectHashMap<String> ASCII_MAP =
      new TLongObjectHashMap<>();
  static {
    // Single-char pattern.
    ASCII_MAP.put(198, "AE");
    ASCII_MAP.put(228, "a");
    ASCII_MAP.put(232, "e");
    ASCII_MAP.put(233, "e");
    ASCII_MAP.put(246, "o");
    ASCII_MAP.put(252, "u");
    ASCII_MAP.put(230, "ae");
    ASCII_MAP.put(339, "ae");
    ASCII_MAP.put(8208, "-");
    ASCII_MAP.put(8217, "'");
    ASCII_MAP.put(8220, "\"");
    ASCII_MAP.put(8221, "\"");
    ASCII_MAP.put(64259, "ffi");
    ASCII_MAP.put(61345, "ae");
    // Double-char pattern.
    ASCII_MAP.put(toLong(195, 134), "AE");
  }
  
  /**
   * Generate all possible ASCII encoded representation for a given string.
   * 
   * @return an array of all possible ASCII encoded representation for a given
   *    string. If the given string is originally encoded in ASCII, then return
   *    an array, which only element is the input string.
   */
  private static String toAsciiRep(String str)
  {
    if (ASCII_ENCODER.canEncode(str)) {
      return str;
    }
    String output = "";
    for (int i = str.length() - 1; i >= 0; i--) {
      char curr = str.charAt(i);
      String asciiRep = null;
      if (! ASCII_ENCODER.canEncode(curr)) {
        // Check double-char pattern.
        if (i > 0) {
          char prev = str.charAt(i - 1);
          if (! ASCII_ENCODER.canEncode(prev)
              && (asciiRep = ASCII_MAP.get(toLong(prev, curr))) != null) {
            i--;
          }
        }
        // Check single-char pattern.
        if (asciiRep == null) {
          asciiRep = ASCII_MAP.get(curr);
        }
      } else {
        asciiRep = Character.toString(curr);
      }
      output = asciiRep + output;
    }
    return output;
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
          String[] splits = line.split("\t", 4);
          // String gtName = stripTail(toAsciiRep(splits[0]));
          String gtName = toAsciiRep(splits[0]);
          String errName = splits[1];
          int pos = Integer.parseInt(splits[2]);
          String info = splits[3];
          errors.add(new GroundTruthError(pos, errName, gtName, info));
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
