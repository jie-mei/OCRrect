package edu.dal.corr.suggest.feature;

import java.io.IOException;
import java.util.Set;

import edu.dal.corr.suggest.batch.WordIsolatedBatchDetectMixin;
import edu.dal.corr.suggest.batch.WordIsolatedBatchScoreMixin;
import edu.dal.corr.suggest.batch.WordIsolatedBatchSearchMixin;
import edu.dal.corr.util.IOUtils;
import edu.dal.corr.util.ResourceUtils;
import edu.dal.corr.word.Word;
import gnu.trove.set.hash.THashSet;

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

  private ReverseLevenshteinDistanceSearcher revLevDistance;
  private THashSet<String> vocab;

  /**
   * @param  unigram  a unigram that limit the candidate search space.
   */
  WordIsolatedFeature(THashSet<String> vocab) {
    this.vocab = vocab;
    this.revLevDistance = ReverseLevenshteinDistanceSearcher.getInstance(vocab);
  }

  WordIsolatedFeature() throws IOException {
    this(IOUtils.readList(ResourceUtils.VOCAB));
  }
  
  protected THashSet<String> getVocab() {
    return vocab;
  }

  /**
   * Search candidates that has {@link
   * edu.dal.corr.metric.EditDistance#levDist(String, String)} {@value
   * #DISTANCE_THRESHOLD} with the given word.
   */
  @Override
  public Set<String> search(Word word) {
    return revLevDistance.search(word.text(), DISTANCE_THRESHOLD);
  }
}
