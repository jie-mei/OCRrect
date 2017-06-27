package edu.dal.ocrrect.expr.detect;

import edu.dal.ocrrect.detect.DetectionFeature;
import edu.dal.ocrrect.io.FloatTSVFile;
import edu.dal.ocrrect.util.Word;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

public class DetectionFeatureTSVFile extends FloatTSVFile {
  private DetectionFeature feature;
  private List<Word> words;

  public DetectionFeatureTSVFile(Path path) {
    super(path);
  }

  public void write(Float[] elements) throws IOException {
    List<Float> list = Arrays.asList(elements);
    write(list);
  }
}
