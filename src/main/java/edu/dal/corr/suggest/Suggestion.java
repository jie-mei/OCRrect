package edu.dal.corr.suggest;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang.builder.HashCodeBuilder;

import edu.dal.corr.util.LocatedTextualUnit;

/**
 * @since 2016.08.10
 */
public class Suggestion
  extends LocatedTextualUnit
  implements Serializable
{
  private static final long serialVersionUID = 4175847645213310315L;

  private static int NON_MAPPING = -1;

  private final List<Class<? extends Feature>> types;
  private final Candidate[] candidates;

  Suggestion(
      String name,
      int position,
      List<Class<? extends Feature>> types,
      Candidate[] candidates)
  {
    super(name, position);
    this.types = types;
    this.candidates = candidates;
  }
  
  public Candidate[] candidates() { return candidates; }
  
  public float[][] score(List<Class<? extends Feature>> types)
  {
    // Create a mapping for types to the new position in the output array.
    int[] map = new int[types.size()];
    Arrays.fill(map, NON_MAPPING);
    for (int i = 0; i < types.size(); i++) {
      for (int j = 0; j < types.size(); j++) {
        if (types.get(i) == types.get(j)) {
          map[i] = j;
          break;
        }
      }
    }
    float[][] scores = new float[candidates.length][types.size()];
    for (int i = 0; i < candidates.length; i++) {
      float[] candScore = candidates[i].score();
      for (int j = 0; j < types.size(); j++) {
        if (map[j] != NON_MAPPING) {
          scores[i][map[j]] = candScore[j];
        }
      }
    }
    return scores;
  }
  
  public float[][] scoreByFeature(List<Feature> features)
  {
    return score(features.stream()
        .map(f -> f.getClass())
        .collect(Collectors.toList()));
  }

  public List<Class<? extends Feature>> types() {
    return types;
  }
  
  @Override
  protected HashCodeBuilder buildHash() {
    return super.buildHash()
        .append(candidates)
        .append(types);
  }
}
