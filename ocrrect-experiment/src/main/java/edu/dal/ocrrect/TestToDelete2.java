package edu.dal.ocrrect;

import edu.dal.ocrrect.text.*;
import edu.dal.ocrrect.util.ResourceUtils;
import edu.dal.ocrrect.util.lexicon.Lexicon;
import edu.dal.ocrrect.util.lexicon.Lexicons;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestToDelete2 {

  private static TextSegmenter google;
  private static TextSegmenter ptb;

  public static void main(String[] args) throws IOException {
    Lexicon vocab = Lexicons.toLexicon(ResourceUtils.getResource(
      "dictionary/12dicts-6.0.2/International/2of4brif.txt"));

    google = new GoogleGramSegmenter(vocab);
    ptb = new PennTreebankSegmenter();

    print("cannot");
    print("Hello-world");
    print("Hello- world");
    print("Hello}- world");
    print("Familx- world");
  }

  private static void print(String text) {
    System.out.println("-------------- ptb --------------");
    ptb.segment(new Text(text)).forEach(System.out::println);
    System.out.println("------------- google ------------");
    google.segment(new Text(text)).forEach(System.out::println);
    System.out.println("---------------------------------");
    System.out.println();
  }
}
