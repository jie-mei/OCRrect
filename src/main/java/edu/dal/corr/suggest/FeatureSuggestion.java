package edu.dal.corr.suggest;

import java.util.ArrayList;
import java.util.List;

import edu.dal.corr.util.LocatedTextualUnit;
import edu.dal.corr.word.Word;

/**
 * A correction for a {@link Word} provided by a {@link Feature}.
 * 
 * @since 2016.07.27
 */
class FeatureSuggestion
  extends LocatedTextualUnit
{
  private static final long serialVersionUID = 4325848663691958974L;

  private final Class<? extends Feature> type;
  private final String[] candidates;
  private final float[] scores;

  FeatureSuggestion(Class<? extends Feature> type, String name,
      int position, String[] candidates, float[] scores)
  {
    super(name, position);
    this.type = type;
    this.candidates = candidates;
    this.scores = scores;
  }
  
  Class<? extends Feature> type()
  {
    return type;
  }

  public List<FeatureCandidate> candidates()
  {
    List<FeatureCandidate> cands = new ArrayList<>(scores.length);
    for (int i = 0; i < scores.length; i++) {
      cands.add(new FeatureCandidate(type, candidates[i], scores[i]));
    }
    return cands;
  }
}
