package edu.dal.corr.suggest;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import edu.dal.corr.util.LocatedTextualUnit;

public class Suggestion
  extends LocatedTextualUnit
  implements Serializable
{
  private static final long serialVersionUID = 4175847645213310315L;

  private static int NON_MAPPING = -1;

  private final List<Class<? extends Feature>> types;
  private final Candidate[] candidates;

  Suggestion(String name, int position, List<Class<? extends Feature>> types, Candidate[] candidates)
  {
    super(name, position);
    this.types = types;
    this.candidates = candidates;
  }
  
  public Candidate[] candidates() { return candidates; }
  
  public float[][] score(List<Feature> features)
  {
    // Create a mapping for types to the new position in the output array.
    int[] map = new int[features.size()];
    Arrays.fill(map, NON_MAPPING);
    for (int i = 0; i < types.size(); i++) {
      for (int j = 0; j < features.size(); j++) {
        if (types.get(i) == features.get(j).getClass()) {
          map[i] = j;
          break;
        }
      }
    }
    float[][] scores = new float[candidates.length][features.size()];
    for (int i = 0; i < candidates.length; i++) {
      float[] candScore = candidates[i].score();
      for (int j = 0; j < features.size(); j++) {
        if (map[j] != NON_MAPPING) {
          scores[i][map[j]] = candScore[j];
        }
      }
    }
    return scores;
  }

  public List<Class<? extends Feature>> types()
  {
    return types;
  }
}
