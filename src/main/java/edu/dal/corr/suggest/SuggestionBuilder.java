package edu.dal.corr.suggest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import gnu.trove.list.array.TFloatArrayList;
import gnu.trove.map.hash.TObjectIntHashMap;

class SuggestionBuilder {
  private String name;
  private int position;
  private List<Class<? extends Feature>> types;
  private Map<String, TFloatArrayList> scoreMap;

  SuggestionBuilder(String name, int position)
  {
    this.name = name;
    this.position = position;
    types = new ArrayList<>();
    scoreMap = new HashMap<>();
  }
  
  SuggestionBuilder add(FeatureSuggestion correction)
  {
    types.add(correction.type());
    for (FeatureCandidate fc : correction.candidates()) {
      TFloatArrayList cScores = null;
      if ((cScores = scoreMap.get(fc.text())) == null) {
        // Create a new candidate score list and filled 0 as previous inserted
        // features scores.
        cScores = new TFloatArrayList();
        cScores.fill(0, types.size() - 1, 0);
        scoreMap.put(fc.text(), cScores);
      }
      if (cScores.size() > 3) {
        System.out.println(fc.text());
        System.out.println(cScores);
      }
      cScores.add(fc.score());
    }
    return this;
  }
  
  Suggestion build()
  {
    // Build candidates.
    List<Candidate> cList = new ArrayList<>();
    TObjectIntHashMap<Class<? extends Feature>> typeMap = new TObjectIntHashMap<>();
    scoreMap.forEach((k, v) -> {
      // Pad 0s to the tail of score arrays.
      try {
      v.fill(v.size(), types.size(), 0);
      } catch (IllegalArgumentException e) {
        System.out.println(v);
        types.forEach(t -> System.out.println(t.getName()));
        throw new RuntimeException(e);
      }
      Candidate c = new Candidate(k, typeMap, v.toArray());
      cList.add(c);
    });
    Candidate[] candidates = cList.toArray(new Candidate[cList.size()]);
    
//    // Create array of types only known at runtime.
//    @SuppressWarnings("unchecked")
//    Class<? extends Feature>[] typeArray = (Class<? extends Feature>[]) new Object[types.size()];
//    types.toArray(typeArray);
    
    return new Suggestion(name, position, types, candidates);
  }
}
