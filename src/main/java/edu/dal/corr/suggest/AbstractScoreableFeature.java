package edu.dal.corr.suggest;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import edu.dal.corr.suggest.banchmark.BenchmarkScoreMixin;
import edu.dal.corr.suggest.banchmark.BenchmarkSearchMixin;
import edu.dal.corr.suggest.banchmark.IsolatedWordBenchmarkSearchMixin;
import edu.dal.corr.word.Word;
import gnu.trove.map.TObjectFloatMap;
import gnu.trove.map.hash.TObjectFloatHashMap;

/**
 * @since 2016.08.11
 */
abstract class AbstractScoreableFeature
  extends AbstractFeature
  implements BenchmarkSearchMixin, BenchmarkScoreMixin,
    IsolatedWordBenchmarkSearchMixin
{
  private static final int DISTANCE_THRESHOLD = 3;
  private Searchable revLevDistance;
  
  AbstractScoreableFeature()
  {
    this.revLevDistance =
        new ReverseLevenshteinDistanceSearcher(DISTANCE_THRESHOLD);
  }

  @Override
  public List<String> search(Word word) {
    return revLevDistance.search(word);
  }

  @Override
  public TObjectFloatMap<String> suggest(Word word)
  {
    TObjectFloatMap<String> map = new TObjectFloatHashMap<>();
    search(word).stream().forEach(candidate -> {
      map.put(candidate, score(word, candidate));
    });
    return map;
  }

  @Override
  public List<TObjectFloatMap<String>> suggest(List<Word> words)
  {
    List<List<String>> candidatesList = search(words);
    return IntStream.range(0, words.size())
        .mapToObj(i -> {
          TObjectFloatMap<String> map = new TObjectFloatHashMap<>();
          List<String> candidates = candidatesList.get(i);
          List<Float> scores = score(words.get(i), candidates);
          IntStream.range(0, candidates.size()).forEach(idx -> {
            map.put(candidates.get(idx), scores.get(idx));
          });
          return map;
        })
        .collect(Collectors.toList());
  }
}
