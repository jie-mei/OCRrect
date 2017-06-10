package edu.dal.ocrrect.suggest;

import edu.dal.ocrrect.metric.NGram;
import edu.dal.ocrrect.util.Word;
import info.debatty.java.stringsimilarity.Damerau;
import info.debatty.java.stringsimilarity.Jaccard;
import info.debatty.java.stringsimilarity.JaroWinkler;
import info.debatty.java.stringsimilarity.Levenshtein;
import info.debatty.java.stringsimilarity.LongestCommonSubsequence;
import info.debatty.java.stringsimilarity.OptimalStringAlignment;
import info.debatty.java.stringsimilarity.QGram;
import java.io.Serializable;

public interface Scoreable extends Serializable {
  /**
   * Score the candidate string.
   *
   * @param word A word.
   * @param candidate A candidate for the given word.
   * @return A score for the specified candidate.
   */
  float score(Word word, String candidate);

  static Scoreable levenshteinDist() {
    return (w, c) -> (float)new Levenshtein().distance(w.text(), c);
  }

  static Scoreable damerauLevDist() {
    return (w, c) -> (float)new Damerau().distance(w.text(), c);
  }

  static Scoreable lscDist() {
    return (w, c) -> (float)new LongestCommonSubsequence().distance(w.text(), c);
  }

  static Scoreable jaroWinklerDist() {
    return (w, c) -> (float)new JaroWinkler().distance(w.text(), c);
  }

  static Scoreable jaccardDist(int size) {
    return (w, c) -> {
      double dist = new Jaccard(size).distance(w.text(), c);
      return Double.isNaN(dist) ? 0f : (float) dist;
    };
  }

  static Scoreable optStrAlignDist() {
    return (w, c) -> (float)new OptimalStringAlignment().distance(w.text(), c);
  }

  static Scoreable binNgramDist(int size) {
    return (w, c) -> (float)new NGram(size, NGram.BINARY_COST).distance(w.text(), c);
  }

  static Scoreable posNgramDist(int size) {
    return (w, c) -> (float)new NGram(size, NGram.POSITIONAL_COST).distance(w.text(), c);
  }

  static Scoreable cmphNgramDist(int size) {
    return (w, c) -> (float)new NGram(size, NGram.COMPREHENSIVE_COST).distance(w.text(), c);
  }

  static Scoreable qgramDist(int size) {
    return (w, c) -> (float)new QGram(size).distance(w.text(), c);
  }
}
