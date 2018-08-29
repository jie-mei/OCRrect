package edu.dal.ocrrect.expr.segment;

import edu.dal.ocrrect.io.WordTSVFile;
import edu.dal.ocrrect.text.PennTreebankSegmenter;
import edu.dal.ocrrect.Text;
import edu.dal.ocrrect.text.WhiteSpaceSegmenter;
import edu.dal.ocrrect.util.IOUtils;
import edu.dal.ocrrect.util.ResourceUtils;
import edu.dal.ocrrect.util.Word;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class BaselineSegmentationGeneration {
  public static void main(String[] args) throws IOException {
    Text text = new Text(IOUtils
      .read(ResourceUtils.getResourceInDir("*.txt", "mibio-ocr/ocr")));

    Path folder = Paths.get("tmp").resolve("detect").resolve("data");
    Files.createDirectories(folder);
    {
      List<Word> ptbWords = text.segment(new PennTreebankSegmenter()).toWords();
      new WordTSVFile(folder.resolve("words.ptb.tsv")).write(ptbWords);
    }
    {
      List<Word> wsWords = text.segment(new WhiteSpaceSegmenter()).toWords();
      new WordTSVFile(folder.resolve("words.ws.tsv")).write(wsWords);
    }
  }
}
