package edu.dal.corr.suggest;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import edu.dal.corr.suggest.feature.Feature;
import edu.dal.corr.word.Word;
import gnu.trove.list.array.TFloatArrayList;
import gnu.trove.map.TObjectFloatMap;

/**
 * @since 2017.03.19
 */
class FeatureSuggestionBuilder
{
  private Feature feature;
  private String name;
  private int position;
  private List<String> candidates;
  private TFloatArrayList scores;
 
  FeatureSuggestionBuilder(Feature feature, Word word)
  {
    this.feature = feature;
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
    if (! fc.type().equals(feature)) {
      throw new RuntimeException(String.join(" ", 
          "Invalid candidate type given:", fc.type().toString(),
          "expect:", feature.toString()));
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

    float normDenom = 0;
    float normReduce = 0;
    switch (feature.normalize()) {
      case NONE:
        return;
      case DIVIDE_MAX:
        normDenom = scores.max(); break;
      case TO_PROB:
        normDenom = scores.sum(); break;
      case LOG_AND_RESCALE:
        scores.transformValues(s -> (float)Math.log10(s + 1));  // add-one smooth
        // continue
      case RESCALE:
      case RESCALE_AND_NEGATE:
        normReduce = scores.min();
        normDenom = scores.max() - scores.min(); break;
    }
    final float reduce = normReduce;
    final float denorm = (normDenom == 0 ? 1 : normDenom);  // avoid 0 division
    scores.transformValues(s -> (s - reduce) / denorm);
    if (feature.normalize() == NormalizationOption.RESCALE_AND_NEGATE) {
      scores.transformValues(s -> -s);
    }
  }
  
  FeatureSuggestion build()
  {
    normalize();
    return new FeatureSuggestion(feature, name, position,
        candidates.toArray(new String[candidates.size()]),
        scores.toArray());
  }
  
  static FeatureSuggestion build(Feature feature, Word word,
      TObjectFloatMap<String> scoreMap) {
    return new FeatureSuggestionBuilder(feature, word)
        .add(scoreMap)
        .build();
  }
  
  static List<FeatureSuggestion> build(Feature feature, List<Word> words,
      List<TObjectFloatMap<String>> scoreMaps) {
    return IntStream
        .range(0, words.size())
        .mapToObj(i -> build(feature, words.get(i), scoreMaps.get(i)))
        .collect(Collectors.toList());
  }
}
