package edu.dal.corr.suggest.feature;

import java.util.Set;

import edu.dal.corr.suggest.Searchable;
import edu.dal.corr.suggest.batch.WordIsolatedBatchDetectMixin;
import edu.dal.corr.suggest.batch.WordIsolatedBatchScoreMixin;
import edu.dal.corr.suggest.batch.WordIsolatedBatchSearchMixin;
import edu.dal.corr.util.Unigram;
import edu.dal.corr.word.Word;
import gnu.trove.map.TObjectFloatMap;
import gnu.trove.map.hash.TObjectFloatHashMap;

/**
 * @since 2016.08.11
 */
abstract class WordIsolatedFeature
    extends Feature
    implements WordIsolatedBatchDetectMixin, WordIsolatedBatchSearchMixin,
               WordIsolatedBatchScoreMixin {

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
  WordIsolatedFeature(Unigram unigram) {
    this.revLevDistance =
        new ReverseLevenshteinDistanceSearcher(unigram, DISTANCE_THRESHOLD);
  }

  WordIsolatedFeature() {
    this(Unigram.getInstance());
  }

  /**
   * Search candidates that has {@link
   * edu.dal.corr.metric.EditDistance#levDist(String, String)} {@value
   * #DISTANCE_THRESHOLD} with the given word.
   */
  @Override
  public Set<String> search(Word word) {
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
}
