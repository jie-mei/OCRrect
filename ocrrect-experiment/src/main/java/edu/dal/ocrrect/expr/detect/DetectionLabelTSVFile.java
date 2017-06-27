package edu.dal.ocrrect.expr.detect;

import edu.dal.ocrrect.io.IntegerTSVFile;
import edu.dal.ocrrect.util.Word;

import java.nio.file.Path;
import java.util.List;

public class DetectionLabelTSVFile extends IntegerTSVFile {
  private List<Word> words;

  public DetectionLabelTSVFile(Path path) {
    super(path);
  }
}
