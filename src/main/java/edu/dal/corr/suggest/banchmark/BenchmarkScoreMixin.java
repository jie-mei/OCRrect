package edu.dal.corr.suggest.banchmark;

import java.util.List;
import java.util.stream.Collectors;

import edu.dal.corr.suggest.feature.Scoreable;
import edu.dal.corr.word.Word;

/**
 * @since 2016.08.10
 */
public interface BenchmarkScoreMixin
  extends Scoreable
{
  default List<Float> score(Word word, List<String> candidates)
  {
    return candidates.stream()
        .map(c -> score(word, c))
        .collect(Collectors.toList());
  }
}
