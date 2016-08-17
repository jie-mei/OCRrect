package edu.dal.corr.suggest;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.log4j.Logger;

import edu.dal.corr.suggest.banchmark.BenchmarkSearchMixin;
import edu.dal.corr.util.LogUtils;
import edu.dal.corr.word.Word;
import gnu.trove.map.TObjectFloatMap;

/**
 * @since 2016.08.10
 */
public class Features
{
  private static final Logger LOG = Logger.getLogger(Features.class);
  
  private Features() {}
  
  public static Logger getLogger() { return LOG; }

  /**
   * Make detection result giving decision from all features.
   *
   * @param  decisions  A list of decision from all applied features.
   * @return {@code true} if any feature gives {@code true}; otherwise return
   *    {@code false}.
   */
  static final boolean detect(List<Boolean> decisions)
  {
    return decisions.stream().anyMatch(d -> d.booleanValue());
  }

  /**
   * Detect whether the given word requires further correction. The performance
   * of this method is optimized for detection with {@link BenchmarkSearchMixin}
   * features.
   * 
   * @param  word      A list of words.
   * @param  features  A list of features.
   * @return A list of candidates for each word ordered in the input word list.
   */
  static List<Boolean> detect(List<Word> words, List<Feature> features)
  {
    return LogUtils.logMethodTime(2, () ->
    {
      // Detect for each word using all features.
      List<List<Boolean>> decisions = features.stream()
          .map(feat -> {
            String logInfo = feat.getClass().getName() + ".detect()";
            LOG.info(logInfo);
            return LogUtils.logTime(3, () -> feat.detect(words), logInfo);
          })
          .collect(Collectors.toList());

      // Make final decision for each word.
      return IntStream.range(0, words.size())
          .mapToObj(i -> decisions.stream()
              .map(list -> list.get(i))
              .collect(Collectors.toList()))
          .map(Features::detect)
          .collect(Collectors.toList());
    });
  }

  static List<List<FeatureSuggestion>> suggest(List<Word> words, List<Feature> features)
  {
    return LogUtils.logMethodTime(2, () ->
    {
      // Suggest for each word using all features.
      List<List<FeatureSuggestion>> fsListedByWordsByFeatures = features
          .stream()
          .map(feat -> {

            // Perform benchmark suggestion for all words.
            String logInfo = feat.getClass().getName() + ".suggest()";
            List<TObjectFloatMap<String>> mapList = LogUtils.logTime(3, () -> {
              if (LOG.isInfoEnabled()) {
                LOG.info(logInfo);
              }
              return feat.suggest(words);
            }, logInfo);

            // Convert suggested candidate maps to feature suggestions.
            List<FeatureSuggestion> fsList = IntStream.range(0, words.size())
                .mapToObj(i -> {
                  return new FeatureSuggestionBuilder(feat, words.get(i))
                      .add(mapList.get(i))
                      .build();
                })
                .collect(Collectors.toList());

            return fsList;
          })
          .collect(Collectors.toList());

      // List results by word. Each list item is a nested list, which item is
      // the candidates form a feature.
      return IntStream.range(0, words.size())
          .mapToObj(i -> fsListedByWordsByFeatures.stream()
              .map(fsListedbyWordsOfFeature -> fsListedbyWordsOfFeature.get(i))
              .collect(Collectors.toList()))
          .collect(Collectors.toList());
    });
  }
  
//  /**
//   * Merge searching result giving result from all features.
//   *
//   * @param  decisions  A list of searching result from all applied features.
//   * @return A list of candidate strings which is the union of candidates
//   *    suggested by different features.
//   */
//  private static final List<String> merge(List<List<String>> candidates)
//  {
//    return candidates.stream()
//        .flatMap(List::stream)
//        .distinct()
//        .collect(Collectors.toList());
//  }
//  
//  private static TObjectFloatMap<String> getTopCandidates(
//      TObjectFloatMap<String> candidateMap, int number)
//  {
//    List<Pair> pairs = new ArrayList<>();
//    candidateMap.keySet().forEach(c -> pairs.add(new Pair(c, candidateMap.get(c))));
//    Collections.sort(pairs);
//    TObjectFloatMap<String> topMap = new TObjectFloatHashMap<>();
//    pairs.stream().limit(number).forEach(p -> topMap.put(p.name, p.val));
//    return topMap;
//  }
//  
//  private static class Pair
//    implements Comparable<Pair>
//  {
//    private final String name;
//    private final float val;
//    Pair(String name, float val)
//    {
//      this.name = name;
//      this.val = val;
//    }
//    @Override
//    public int compareTo(Pair another) {
//      float diff = val - another.val;
//      return diff == 0 ? 0 : (diff < 0 ? 1 : -1);
//    }
//  }
}
