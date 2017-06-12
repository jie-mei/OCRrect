package edu.dal.ocrrect;

import edu.dal.ocrrect.text.GoogleGramSegmenter;
import edu.dal.ocrrect.text.Text;
import edu.dal.ocrrect.util.IOUtils;
import edu.dal.ocrrect.util.ResourceUtils;
import edu.dal.ocrrect.util.Token;

import java.io.IOException;
import java.util.List;

public class SegmentationExperiment {

  public static void main(String[] args) throws IOException {
    Text text = new Text(IOUtils.read(ResourceUtils.getResourceInDir("*.txt", "mibio-ocr/ocr")));
    text = new Text(text.text().substring(0, 2000));
    List<Token> tokens = text
//      .process(new LineConcatTextProcessor(new GoogleUnigramLexicon()))
      .segment(new GoogleGramSegmenter());
//      .segment(new PennTreebankSegmenter());
    System.out.print(tokens);
  }
}
