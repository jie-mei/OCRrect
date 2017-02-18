package edu.dal.corr.suggest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.dal.corr.suggest.feature.FeatureType;
import gnu.trove.list.array.TFloatArrayList;
import gnu.trove.map.hash.TObjectIntHashMap;

/**
 * @since 2016.08.10
 */
class SuggestionBuilder
{
  private String name;
  private int position;
  private List<FeatureType> types;
  private Map<String, TFloatArrayList> scoreMap;

  SuggestionBuilder(String name, int position)
  {
    this.name = name;
    this.position = position;
    types = new ArrayList<>();
    scoreMap = new HashMap<>();
  }
  
  /**
   * Add a feature suggestion.
   * 
   * @param  fs  a feature suggestion.
   * @return this builder object.
   */
  SuggestionBuilder add(FeatureSuggestion fs)
  {
    if (types.contains(fs.type())) {
      throw new IllegalArgumentException("duplicate feature suggestion type.");
    }
    for (FeatureCandidate fc : fs.candidates()) {
      TFloatArrayList cScores = null;

      // Create a new candidate score list if not exist.
      if ((cScores = scoreMap.get(fc.text())) == null) {
        cScores = new TFloatArrayList();
        scoreMap.put(fc.text(), cScores);
      }
      // Fill missing values.
      if (cScores.size() < types.size()) {
        cScores.fill(cScores.size(), types.size(), 0);
      }
      cScores.add(fc.score());
    }
    types.add(fs.type());
    return this;
  }
  
  Suggestion build()
  {
    // Build candidates.
    List<Candidate> cList = new ArrayList<>();
    TObjectIntHashMap<FeatureType> typeMap = new TObjectIntHashMap<>();
    for (int i = 0; i < types.size(); i++) {
      typeMap.put(types.get(i), i);
    }
    scoreMap.forEach((k, v) -> {
      // Pad 0s to the tail of score arrays.
      try {
        v.fill(v.size(), types.size(), 0);
      } catch (IllegalArgumentException e) {
        throw new RuntimeException(e);
      }
      Candidate c = new Candidate(k, typeMap, v.toArray());
      cList.add(c);
    });
    Candidate[] candidates = cList.toArray(new Candidate[cList.size()]);
    
    return new Suggestion(name, position, types, candidates);
  }
}
