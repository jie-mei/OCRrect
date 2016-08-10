package edu.dal.corr.suggest;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import edu.dal.corr.util.LogUtils;
import edu.dal.corr.util.Timer;
import edu.dal.corr.word.Word;

class Features
{
  static final Logger LOG = Logger.getLogger(Features.class);

  /**
   * Make detection result giving decision from all features.
   *
   * @param  decisions  A list of decision from all applied features.
   * @return {@code true} if any feature gives {@code true}; otherwise return
   *    {@code false}.
   */
  private static final boolean detect(List<Boolean> decisions)
  {
    return decisions.stream().anyMatch(d -> d.booleanValue());
  }

  /**
   * Detect whether the given word requires further correction. The performance
   * of this method is optimized for detection with {@link BenchmarkBehavior}
   * features.
   * 
   * @param  word      A list of words.
   * @param  features  A list of features.
   * @return A list of candidates for each word ordered in the input word list.
   */
  static List<Boolean> detect(List<Word> words, List<Feature> features)
  {
    Timer t = new Timer();

    List<List<Boolean>> decisions = features.stream()
        .map(feat -> {
          if (feat instanceof BenchmarkBehavior) {
            return ((BenchmarkBehavior) feat).detect(words);
          } else {
            return words.stream()
                .map(feat::detect)
                .collect(Collectors.toList());
          }
        })
        .collect(Collectors.toList());
    List<Boolean> result = IntStream.range(0, words.size())
        .mapToObj(i -> decisions.stream()
            .map(list -> list.get(i))
            .collect(Collectors.toList()))
        .map(Features::detect)
        .collect(Collectors.toList());
    
    LogUtils.logMethodTime(t, 2);
    return result;
  }
  
  /**
   * Make searching result giving result from all features.
   *
   * @param  decisions  A list of searching result from all applied features.
   * @return A list of candidate strings which is the union of candidates
   *    suggested by different features.
   */
  private static final List<String> search(List<List<String>> candidates)
  {
    return candidates.stream()
        .flatMap(List::stream)
        .distinct()
        .collect(Collectors.toList());
  }

  /**
   * Search word candidates for a list of words. The performance of this method
   * is optimized for searching with {@link ContextInsensitive} features.
   * 
   * @param  word      A list of words.
   * @param  features  A list of features.
   * @return A list of candidates for each word ordered in the input word list.
   */
  static List<List<String>> search(List<Word> words, List<Feature> features)
  {
    Timer t = new Timer();

    List<List<List<String>>> decisions = features.stream()
        .map(feat -> {
          LogUtils.debug("FEATURE: " + feat.getClass().getSimpleName());
          FileAppender fa = null;
          try {
            fa = new FileAppender(new PatternLayout("%m%n"),
                String.format("log/feature.search.%s.log", feat.getClass().getSimpleName()),
                false);
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
          LOG.addAppender(fa);

          List<List<String>> candidates = null;
          if (feat instanceof BenchmarkBehavior) {
            candidates = ((BenchmarkBehavior) feat).search(words);
          } else {
            candidates = words.stream()
                .map(feat::search)
                .collect(Collectors.toList());
          }
          LOG.removeAppender(fa);
          return candidates;
        })
        .collect(Collectors.toList());
    List<List<String>> result = IntStream.range(0, words.size())
        .mapToObj(i -> decisions.stream()
            .map(list -> list.get(i))
            .collect(Collectors.toList()))
        .map(Features::search)
        .collect(Collectors.toList());
    
    LogUtils.logMethodTime(t, 2);
    return result;
  }
  
  /**
   * Generate {@link FeatureSuggestion} for a word. This method defines the
   * general feature suggestion procedure.
   * 
   * @param  word
   * @param  candidates
   * @param  feature
   * @return
   */
  static FeatureSuggestion score(Word word, List<String> candidates, Feature feature)
  {
    FeatureSuggestionBuilder fsb = new FeatureSuggestionBuilder(feature.getClass(), word);
    candidates.forEach(c -> fsb.add(c, feature.score(word, c)));
    if (feature instanceof Normalized) {
      fsb.normalize(((Normalized) feature).normalize());
    }
    return fsb.build();
  }
  
  static List<List<FeatureSuggestion>> score(List<Word> detected,
      List<List<String>> candidates, List<Feature> features)
  {
    Timer t = new Timer();

    List<List<FeatureSuggestion>> suggestions = IntStream.range(0, detected.size())
        .mapToObj(i -> features.stream()
            .map(f -> score(detected.get(i), candidates.get(i), f))
            .collect(Collectors.toList()))
        .collect(Collectors.toList());

    LogUtils.logMethodTime(t, 2);
    return suggestions;
  }

  /**
   * Generate a list of {@link FeatureSuggestion} for each detected word ordered
   * in the given word list.
   * 
   * @param  words     A list of words.
   * @param  features  A list of features.
   * @return A list of {@code FeatureSuggestion} for each error word ordered in
   *    the given list.
   */
  static List<List<FeatureSuggestion>> suggest(List<Word> words,
      List<Feature> features)
  {
    // Detection.
    List<Boolean> detects = detect(words, features);
    
    // Filtering undetected words.
    List<Word> detectedWords = IntStream.range(0, words.size())
        .mapToObj(i -> (detects.get(i) ? words.get(i) : null))
        .filter(w -> w != null)
        .collect(Collectors.toList());

    // Searching.
    List<List<String>> candidates = search(detectedWords, features);
    
    // Scoring.
    List<List<FeatureSuggestion>> suggestions =
        score(detectedWords, candidates, features);

    return suggestions;
  }
}
