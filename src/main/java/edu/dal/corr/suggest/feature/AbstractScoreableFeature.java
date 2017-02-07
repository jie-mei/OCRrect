package edu.dal.corr.suggest.feature;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import edu.dal.corr.suggest.banchmark.BenchmarkScoreMixin;
import edu.dal.corr.suggest.banchmark.BenchmarkSearchMixin;
import edu.dal.corr.suggest.banchmark.IsolatedWordBenchmarkSearchMixin;
import edu.dal.corr.util.Unigram;
import edu.dal.corr.word.Word;
import gnu.trove.map.TObjectFloatMap;
import gnu.trove.map.hash.TObjectFloatHashMap;

/**
 * @since 2016.08.11
 */
abstract class AbstractScoreableFeature
  extends Feature
  implements BenchmarkSearchMixin, BenchmarkScoreMixin,
    IsolatedWordBenchmarkSearchMixin
{
  private static final long serialVersionUID = -5936708004358762300L;

  /**
   * The threshold of edit distance used for candidate searching. Default value
   * is 3.
   */
  public static final int DISTANCE_THRESHOLD = 3;

  private Searchable revLevDistance;

  /**
   * @param  unigram  a unigram that limit the candidate search space.
   */
  AbstractScoreableFeature(Unigram unigram) {
    this.revLevDistance =
        new ReverseLevenshteinDistanceSearcher(unigram, DISTANCE_THRESHOLD);
  }

  AbstractScoreableFeature() {
    this(Unigram.getInstance());
  }

  /**
   * Search candidates that has {@link
   * edu.dal.corr.metric.EditDistance#levDist(String, String)} {@value
   * #DISTANCE_THRESHOLD} with the given word.
   */
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
