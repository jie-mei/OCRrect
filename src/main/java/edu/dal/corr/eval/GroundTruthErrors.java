package edu.dal.corr.eval;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import edu.dal.corr.util.IOUtils;
import edu.dal.corr.util.LogUtils;

public class GroundTruthErrors
{
  private static Pattern ERROR = Pattern.compile(".*ERROR.*");

  private GroundTruthErrors() {}
  
  public static List<GroundTruthError> read(Path path)
  {
    return LogUtils.logMethodTime(1, () ->
    {
      List<GroundTruthError> errors = new ArrayList<>();
      try (BufferedReader br = IOUtils.newBufferedReader(path)) {
        for (String line = br.readLine(); line != null; line = br.readLine()) {
          String[] splits = line.split("\t", 4);
          if (splits.length < 4) {
            System.out.println(line);
          }
          String gtName = splits[0];
          String errName = splits[1];
          int pos = Integer.parseInt(splits[2]);
          String info = splits[3];
          
          // Omit the error information.
          if (ERROR.matcher(line).matches()) {
            continue;
          }
          errors.add(new GroundTruthError(gtName, errName, pos, info));
        }
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
      return errors;
    });
  }
}
