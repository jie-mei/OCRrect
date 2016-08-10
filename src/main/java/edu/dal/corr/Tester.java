package edu.dal.corr;

import java.util.Arrays;
import java.util.List;

import edu.dal.corr.suggest.Suggestion;
import edu.dal.corr.word.GoogleTokenizer;
import edu.dal.corr.word.Word;
import edu.dal.corr.word.WordFilter;
import edu.dal.corr.suggest.Feature;

public class Tester
{
  private static class Feature1
    implements Feature {

    // Treat "h" as an error.
    public boolean detect(Word word) {
      return word.text().equals("h");
    }

    // Correct error to "x" with 100% confidence.
    public List<String> search(Word word) {
      return Arrays.asList("x");
    }
    public float score(Word word, String candidate) {
      return 1f;
    }
  }

  private static class Feature2
    implements Feature {

    // Treat "h" and "a" as an error.
    public boolean detect(Word word) {
      String text = word.text();
      return text.equals("h") || text.equals("a");
    }

    // Correct error to "y" with 50% confidence.
    public List<String> search(Word word) {
      return Arrays.asList("y");
    }
    public float score(Word word, String candidate) {
      return 0.5f;
    }
  }

  private static class Feature3
    implements Feature {

    // Treat "h" and "a" as an error.
    public boolean detect(Word word) {
      String text = word.text();
      return text.equals("h") || text.equals("c");
    }

    // Correct error to "y" with 50% confidence.
    public List<String> search(Word word) {
      return Arrays.asList("z");
    }
    public float score(Word word, String candidate) {
      return 0.3f;
    }
  }

  public static void main(String[] args)
  {
    List<Feature> corrs = Arrays.asList(
        new Feature1(),
        new Feature2(),
        new Feature3()
        );
    String text = "a b c d e f g h i j k l m n";
    
    WordFilter filter = new WordFilter() {
      @Override
      public boolean filter(Word word)
      {
        return true;
      }
    };

    List<Suggestion> errors = new DocumentCorrector().correct(
        new GoogleTokenizer(),
        filter,
        corrs,
        text
        );
  }
}
