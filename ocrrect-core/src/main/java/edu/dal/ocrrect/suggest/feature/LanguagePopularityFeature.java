package edu.dal.ocrrect.suggest.feature;

import edu.dal.ocrrect.suggest.NormalizationOption;
import edu.dal.ocrrect.util.Unigram;
import edu.dal.ocrrect.word.Word;

import java.io.IOException;

/**
 * @since 2017.04.20
 */
public class LanguagePopularityFeature extends WordIsolatedFeature {
  private static final long serialVersionUID = -7219239074897074972L;

  private Unigram unigram;

  public LanguagePopularityFeature(String name, Unigram unigram) throws IOException {
    this.unigram = unigram;
  }

  public LanguagePopularityFeature(Unigram unigram) throws IOException {
    this(null, unigram);
  }

  @Override
  public boolean detect(Word word) {
    return unigram.contains(word.text());
  }

  @Override
  public float score(Word word, String candidate) {
    return unigram.freq(candidate);
  }

  @Override
  public NormalizationOption normalize() {
    return NormalizationOption.LOG_AND_RESCALE;
  }
}
