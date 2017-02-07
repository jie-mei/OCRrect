package edu.dal.corr.suggest;

import java.util.ArrayList;
import java.util.List;

import edu.dal.corr.suggest.feature.Feature;
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

  private final Feature feature;
  private final String[] candidates;
  private final float[] scores;

  FeatureSuggestion(Feature feature, String name, int position, String[]
  candidates, float[] scores)
  {
    super(name, position);
    this.feature = feature;
    this.candidates = candidates;
    this.scores = scores;
  }
  
  public Feature feature()
  {
    return feature;
  }

  public List<FeatureCandidate> candidates()
  {
    List<FeatureCandidate> cands = new ArrayList<>(scores.length);
    for (int i = 0; i < scores.length; i++) {
      cands.add(new FeatureCandidate(feature, candidates[i], scores[i]));
    }
    return cands;
  }
}
