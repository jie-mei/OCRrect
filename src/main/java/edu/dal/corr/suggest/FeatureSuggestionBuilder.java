package edu.dal.corr.suggest;

import java.util.ArrayList;
import java.util.List;

import edu.dal.corr.word.Word;
import gnu.trove.function.TFloatFunction;
import gnu.trove.list.array.TFloatArrayList;

/**
 * @since 2016.08.10
 */
class FeatureSuggestionBuilder
{
  private Class<? extends Feature> type;
  private String name;
  private int position;
  private List<String> candidates;
  private TFloatArrayList scores;
 
  FeatureSuggestionBuilder(Class<? extends Feature> type, Word word)
  {
    this.type = type;
    name = word.text();
    position = word.position();
    candidates = new ArrayList<>();
    scores = new TFloatArrayList();
  }
  
  FeatureSuggestionBuilder add(String name, float score)
  {
    candidates.add(name);
    scores.add(score);
    return this;
  }
  
  FeatureSuggestionBuilder add(FeatureCandidate fc)
  {
    if (! fc.type().equals(type)) {
      throw new RuntimeException(String.join(" ", 
          "Invalid candidate type given:", fc.type().getName(),
          "expect:", type.getName()));
    }
    return add(fc.text(), fc.score());
  }
  
  FeatureSuggestionBuilder normalize(Normalization opt)
  {
    float factor = 0;
    switch (opt) {
      case NONE:  factor = 1;
      case MAX:   factor = scores.max();
      case MIN:   factor = scores.min();
      case TOTAL: factor = scores.sum();
    }
    final float denorm = factor;
    scores.transformValues(new TFloatFunction() {
      @Override
      public float execute(float score) {
        return score / denorm;
      }
    });
    return this;
  }
  
  FeatureSuggestion build()
  {
    return new FeatureSuggestion(type, name, position,
        candidates.toArray(new String[candidates.size()]),
        scores.toArray());
  }
}
