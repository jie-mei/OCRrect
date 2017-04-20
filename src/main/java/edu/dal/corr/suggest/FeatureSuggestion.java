package edu.dal.corr.suggest;

import edu.dal.corr.suggest.feature.Feature;
import edu.dal.corr.suggest.feature.FeatureType;
import edu.dal.corr.util.LocatedTextualUnit;
import edu.dal.corr.util.Unigram;
import edu.dal.corr.word.Word;
import java.util.ArrayList;
import java.util.List;

/**
 * A correction for a {@link Word} provided by a {@link Feature}.
 *
 * @since 2017.04.20
 */
public class FeatureSuggestion extends LocatedTextualUnit {
  private static final long serialVersionUID = 4325848663691958974L;

  private final FeatureType type;
  private final Word word;
  private final String[] candidates;
  private final float[] scores;

  FeatureSuggestion(FeatureType type, Word word, String[] candidates, float[] scores) {
    super(word.text(), word.position());
    this.word = word;
    this.type = type;
    this.candidates = candidates;
    this.scores = scores;
  }

  FeatureSuggestion(Feature feature, Word word, String[] candidates, float[] scores) {
    this(feature.type(), word, candidates, scores);
  }

  public FeatureType type() {
    return type;
  }

  public String[] candidateNames() {
    return candidates;
  }

  public float[] scores() {
    return scores;
  }

  public List<FeatureCandidate> candidates() {
    List<FeatureCandidate> cands = new ArrayList<>(scores.length);
    for (int i = 0; i < scores.length; i++) {
      cands.add(new FeatureCandidate(type, candidates[i], scores[i]));
    }
    return cands;
  }

  /**
   * Get a truncated feature suggestion with only top suggestions.
   *
   * @return A feature suggestion with only top suggestions from the original object.
   */
  public FeatureSuggestion top(int num) {
    int top = num < candidates.length ? num : candidates.length;
    String[] topCand = new String[top];
    float[] topScore = new float[top]; // deep clone
    List<FeatureCandidate> candidates = candidates();
    if (top != candidates.size()) {
      candidates.sort((a, b) -> {
        float diff = a.score() - b.score();
        if (diff != 0) {
          return diff > 0 ? -1 : 1;
        } else {
          diff = Unigram.getInstance().freq(a.text())
               - Unigram.getInstance().freq(b.text()); // prefer common word
          return diff == 0 ? 0 : (diff > 0 ? -1 : 1);
        }
      });
    }
    for (int i = 0; i < top; i++) {
      FeatureCandidate fc = candidates.get(i);
      topCand[i] = fc.text();
      topScore[i] = fc.score();
    }
    return new FeatureSuggestion(type, word, topCand, topScore);
  }
}
