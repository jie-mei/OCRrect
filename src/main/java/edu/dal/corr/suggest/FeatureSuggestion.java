package edu.dal.corr.suggest;

import java.util.ArrayList;
import java.util.List;

import edu.dal.corr.suggest.feature.Feature;
import edu.dal.corr.suggest.feature.FeatureType;
import edu.dal.corr.util.LocatedTextualUnit;
import edu.dal.corr.word.Word;

/**
 * A correction for a {@link Word} provided by a {@link Feature}.
 * 
 * @since 2016.07.27
 */
public class FeatureSuggestion
  extends LocatedTextualUnit
{
  private static final long serialVersionUID = 4325848663691958974L;

  private final FeatureType type;
  private final String[] candidates;
  private final float[] scores;

  FeatureSuggestion(FeatureType type, String name, int position, String[]
      candidates, float[] scores) {
    super(name, position);
    this.type = type;
    this.candidates = candidates;
    this.scores = scores;
  }

  FeatureSuggestion(Feature feature, String name, int position, String[]
      candidates, float[] scores) {
    this(feature.type(), name, position, candidates, scores);
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
  
  
  Object[] topCandidatesAndScores(int num) {
    int top = num < candidates.length ? num : candidates.length;
    String[] topCand = new String[top];
    float[] topScore = new float[top];
    List<FeatureCandidate> candidates = candidates();
    if (top != num) {
      candidates.sort((a, b) -> {
        float diff = b.score() - a.score();
        return diff == 0 ? 0 : (diff > 0 ? 1 : -1);
      });
    }
    for (int i = 0; i < top; i++) {
      FeatureCandidate fc = candidates.get(i);
      topCand[i] = fc.text();
      topScore[i] = fc.score();
    }
    return new Object[]{topCand, topScore};
  }
  
  /**
   * Get a truncated feature suggestion with only top suggestions.
   * @return A feature suggestion with only top suggestions from the original object.
   */
  public FeatureSuggestion top(int num) {
    Object[] topData = topCandidatesAndScores(num);
    return new FeatureSuggestion(type, text(), position(), (String[])topData[0], (float[])topData[1]);
  }
}
