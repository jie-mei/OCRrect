package edu.dal.ocrrect.suggest;

import edu.dal.ocrrect.suggest.feature.DuplicateFeatureException;
import edu.dal.ocrrect.word.Word;
import gnu.trove.list.array.TFloatArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @since 2017.04.20
 */
class SuggestionBuilder {
  private String name;
  private int position;
  private FeatureRegistry featureRegistry;
  private Map<String, TFloatArrayList> scoreMap;

  SuggestionBuilder(String name, int position) {
    this.name = name;
    this.position = position;
    featureRegistry = new FeatureRegistry();
    scoreMap = new HashMap<>();
  }

  SuggestionBuilder(Word word) {
    this(word.text(), word.position());
  }

  /**
   * Add a feature suggestion.
   *
   * @param fs a feature suggestion.
   * @return this builder object.
   * @throws DuplicateFeatureException 
   */
  SuggestionBuilder add(FeatureSuggestion fs) throws DuplicateFeatureException {
    featureRegistry.register(fs.type());
    for (FeatureCandidate fc : fs.candidates()) {
      TFloatArrayList cScores = null;
      // Create a new candidate score list if not exist.
      if ((cScores = scoreMap.get(fc.text())) == null) {
        cScores = new TFloatArrayList();
        scoreMap.put(fc.text(), cScores);
      }
      // Fill missing values.
      if (cScores.size() < featureRegistry.types().size()) {
        cScores.fill(cScores.size(), featureRegistry.types().size() - 1, 0);
      }
      cScores.add(fc.score());
    }
    return this;
  }

  Suggestion build() {
    // Build candidates.
    Candidate[] candidates = new Candidate[scoreMap.size()];
    int index = 0;
    for (Entry<String, TFloatArrayList> entry: scoreMap.entrySet()) {
      String name = entry.getKey();
      TFloatArrayList scores = entry.getValue();
      // Pad 0s to the tail of score arrays.
      try {
        scores.fill(scores.size(), featureRegistry.types().size(), 0);
      } catch (IllegalArgumentException e) {
        throw new RuntimeException(e);
      }
      candidates[index] = new Candidate(name, featureRegistry, scores.toArray());
      index++;
    }
    return new Suggestion(name, position, featureRegistry, candidates);
  }
}
