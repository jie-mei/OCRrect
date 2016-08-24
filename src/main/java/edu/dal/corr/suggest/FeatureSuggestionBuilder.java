package edu.dal.corr.suggest;

import java.util.ArrayList;
import java.util.List;

import edu.dal.corr.word.Word;
import gnu.trove.function.TFloatFunction;
import gnu.trove.list.array.TFloatArrayList;
import gnu.trove.map.TObjectFloatMap;

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
  private NormalizationOption opt;

  FeatureSuggestionBuilder(Class<? extends Feature> type, String name,
      int position, NormalizationOption opt)
  {
    this.type = type;
    this.name = name;
    this.position = position;
    candidates = new ArrayList<>();
    scores = new TFloatArrayList();
    this.opt = opt;
  }

  FeatureSuggestionBuilder(Class<? extends Feature> type, Word word,
      NormalizationOption opt)
  {
    this(type, word.text(), word.position(), opt);
  }
 
  FeatureSuggestionBuilder(Feature feature, Word word)
  {
    this(feature.getClass(), word, feature.normalize());
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
  
  FeatureSuggestionBuilder add(TObjectFloatMap<String> map)
  {
    map.keySet().forEach(k -> add(k, map.get(k)));
    return this;
  }
  
  private void normalize()
  {
    if (scores.size() == 0) return;

    float factor = 0;
    switch (opt) {
      case NONE:  return;
      case MAX:   factor = scores.max(); break;
      case MIN:   factor = scores.min(); break;
      case TOTAL: factor = scores.sum(); break;
    }
    final float denorm = factor;
    scores.transformValues(new TFloatFunction() {
      @Override
      public float execute(float score) {
        return score / denorm;
      }
    });
  }
  
  FeatureSuggestion build()
  {
    normalize();
    return new FeatureSuggestion(type, name, position,
        candidates.toArray(new String[candidates.size()]),
        scores.toArray());
  }
}
