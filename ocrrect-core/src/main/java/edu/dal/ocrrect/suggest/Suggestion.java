package edu.dal.ocrrect.suggest;

import edu.dal.ocrrect.eval.GroundTruthError;
import edu.dal.ocrrect.feature.metric.NGram;
import edu.dal.ocrrect.suggest.feature.ContextSensitiveFeature;
import edu.dal.ocrrect.suggest.feature.DuplicateFeatureException;
import edu.dal.ocrrect.suggest.feature.Feature;
import edu.dal.ocrrect.suggest.feature.FeatureType;
import edu.dal.ocrrect.util.IOUtils;
import edu.dal.ocrrect.util.LocatedTextualUnit;
import edu.dal.ocrrect.util.LogUtils;
import edu.dal.ocrrect.util.PathUtils;
import edu.dal.ocrrect.util.Unigram;
import edu.dal.ocrrect.util.Word;
import gnu.trove.map.TObjectFloatMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * @since 2017.02.15
 */
public class Suggestion extends LocatedTextualUnit implements Serializable {
  private static final long serialVersionUID = 4175847645213310315L;

  public static final int ALL_CANDIDATES = 0;

  private final FeatureRegistry featureRegistry;
  private final Candidate[] candidates;

  Suggestion(String name, int position, FeatureRegistry featureRegistry, Candidate[] candidates) {
    super(name, position);
    for (Candidate c : candidates) {
      if (featureRegistry.types().size() != c.scores().length) {
        throw new IllegalArgumentException("unequal number of features and scores: "
            + featureRegistry.types().size() + ", " + c.scores().length);
      }
    }
    this.featureRegistry = featureRegistry;
    this.candidates = candidates;
  }

  /**
   * Get suggested correction candidates.
   *
   * @return all candidates suggested.
   */
  public Candidate[] candidates() {
    return candidates;
  }

  /**
   * Get feature scores for each candidates.
   *
   * @param types a list of types that sort order of the output scores for each candidate.
   * @return a two dimensional array, which first dimensional represents the index of the candidates
   *     according to {@link #candidates()} and second dimension represents the score for each
   *     feature sorted by the give type list.
   */
  public float[][] score(List<FeatureType> types) {
    float[][] scores = new float[candidates.length][types.size()];
    for (int i = 0; i < candidates.length; i++) {
      scores[i] = candidates[i].scores(types);
    }
    return scores;
  }

  public List<FeatureType> types() {
    return featureRegistry.types();
  }

  @Override
  protected HashCodeBuilder buildHash() {
    return super.buildHash().append(candidates).append(featureRegistry);
  }

  /**
   * Make detection result giving decision from all features.
   *
   * @param decisions A list of decision from all applied features.
   * @return {@code true} if any feature gives {@code true}; otherwise return
   *         {@code false}.
   */
  static final boolean detect(List<Boolean> decisions) {
    return decisions.stream().anyMatch(d -> d.booleanValue());
  }

  /**
   * Detect whether the given word requires further correction. The performance
   * of this method is optimized for detection with {@link BatchSearchMixin}
   * features.
   *
   * @param word A list of words.
   * @param features A list of features.
   * @return A list of candidates for each word ordered in the input word list.
   */
  private static List<Boolean> batchDetect(List<Word> words, List<Feature> features) {
    return LogUtils.logMethodTime(2, () -> {
      // Detect for each word using all features.
      List<List<Boolean>> decisions = features.stream()
          .map(feat ->
              LogUtils.logTime(3, () -> feat.detect(words),
                  feat.getClass().getName()
                      + ".detect()"
                      + (feat.type().name() == null ? "" : " [" + feat.type().name() + "]")))
          .collect(Collectors.toList());

      // Make final decision for each word.
      return IntStream
          .range(0, words.size())
          .mapToObj(i -> decisions
              .stream()
              .map(list -> list.get(i))
              .collect(Collectors.toList()))
          .map(Suggestion::detect).collect(Collectors.toList());
    });
  }

  private static List<Set<String>> batchSearch(List<Word> words, List<Feature> features) {
    return LogUtils.logMethodTime(2, () -> {
      List<Set<String>> candidateForWords =
          words.stream()
               .map(w -> new HashSet<String>())
               .collect(Collectors.toList());
      for (Feature feat: features) {
        List<Set<String>> candidateLists = feat.search(words);
        for (int i = 0; i < words.size(); i++) {
          candidateForWords.get(i).addAll(candidateLists.get(i));
        }
      }
      return candidateForWords;
    });
  }

  private static List<List<FeatureSuggestion>> batchScore(List<Word> words, List<Feature> features,
      List<Set<String>> candidateLists) {
    return LogUtils.logMethodTime(2, () -> {
      List<List<FeatureSuggestion>> fsByFeatsByWords = new ArrayList<>();
      for (Feature feat: features) {
        List<TObjectFloatMap<String>> scoreMaps = feat.score(words, candidateLists);
        fsByFeatsByWords.add(FeatureSuggestionBuilder.build(feat, words, scoreMaps));
      }
      return fsByFeatsByWords;
    });
  }

  @SuppressWarnings("unused")
  private static List<List<FeatureSuggestion>> batchSuggest(
      List<Word> words,
      List<Feature> features)
  {
    return batchScore(words, features, batchSearch(words, features));
  }

  private static List<List<FeatureSuggestion>> batchSuggestWithBatchType(
      List<Word> words,
      List<Feature> features,
      int top)
  {
    return LogUtils.logMethodTime(2, () -> {
      List<Set<String>> candidateTotalByWords =
          words.stream()
               .map(w -> new HashSet<String>())
               .collect(Collectors.toList());
      List<List<FeatureSuggestion>> fsByFeatsByWords = new ArrayList<>();

      // Search candidates from word-isolated features.
      for (Feature feat: features) {
        if (! (feat instanceof ContextSensitiveFeature)) {
          List<Set<String>> candidateLists = LogUtils.logTime(
              String.format("%s.%s.search()", feat.getClass().getPackage(), feat.type()),
              3,
              () -> feat.search(words));
          for (int i = 0; i < words.size(); i++) {
            candidateTotalByWords.get(i).addAll(candidateLists.get(i));
          }
        }
      }
      // Generate features suggestions from context-sensitive features.
      for (Feature feat: features) {
        if (feat instanceof ContextSensitiveFeature) {
          List<TObjectFloatMap<String>> scoreMaps =
              LogUtils.logTime(
                  String.format("%s.%s.suggest()", feat.getClass().getPackage(), feat.type()), 3,
                  () -> feat.suggest(words));
          List<FeatureSuggestion> fsList =
              IntStream.range(0, words.size())
                  .parallel()
                  .mapToObj(i -> {
                    TObjectFloatMap<String> scoreMap = scoreMaps.get(i);
                    // Find the top candidates sorted by according feature
                    // scores.
                    List<FeatureCandidate> topCands =
                        scoreMap.keySet()
                                .stream()
                                .map(s -> new FeatureCandidate(feat.type(), s, scoreMap.get(s)))
                                .sorted((a, b) -> {
                                  float diff = a.score() - b.score();
                                  return diff == 0 ? 0 : (diff > 0 ? -1 : 1);
                                })
                                .limit(top)
                                .collect(Collectors.toList());
                    // Build a new feature suggestion with scores from the top
                    // ranked and reverse levenshtein suggested candidates.
                    FeatureSuggestionBuilder fsb = new FeatureSuggestionBuilder(feat, words.get(i));
                    topCands.stream().forEach(fc -> fsb.add(fc));
                    candidateTotalByWords.get(i).forEach(c -> {
                      float score = 0;
                      if ((score = scoreMap.get(c)) > 0) {
                        fsb.add(c, score);
                      }
                    });
                    // Insert the top candidates into the candidate list.
                    candidateTotalByWords.get(i).addAll(
                        topCands.stream()
                            .map(fc -> fc.text())
                            .collect(Collectors.toList())
                        );
                    return fsb.build();
                  })
                  .collect(Collectors.toList());
          fsByFeatsByWords.add(fsList);
        }
      }
      // Score candidates and generate feature suggestions.
      for (Feature feat: features) {
        if (! (feat instanceof ContextSensitiveFeature)) {
          List<TObjectFloatMap<String>> scoreMaps =
              LogUtils.logTime(
                  String.format("%s.%s.score()", feat.getClass().getPackage(), feat.type()), 3,
                  () -> feat.score(words, candidateTotalByWords));
          List<FeatureSuggestion> fsList =
              FeatureSuggestionBuilder
                  .build(feat, words, scoreMaps)
                  .parallelStream()
                  .map(fs -> fs.top(top))
                  .collect(Collectors.toList());
          fsByFeatsByWords.add(fsList);
        }
      }
      return fsByFeatsByWords;
    });
  }

  /**
   * Generate correction suggestions.
   *
   * @param words a list of words.
   * @param features a list of features.
   * @param detect whether the error detection step is needed.
   * @return A list of suggestions.
   */
  public static List<Suggestion> suggest(List<Word> words, List<Feature> features, int top,
      boolean detect) {
    return LogUtils.logMethodTime(1, () -> {
      // Detection.
      List<Word> errWords = words;
      if (detect) {
        List<Boolean> detects = batchDetect(words, features);
        // Filtering undetected words.
        errWords = IntStream
            .range(0, words.size())
            .mapToObj(i -> (detects.get(i) ? words.get(i) : null))
            .filter(w -> w != null)
            .collect(Collectors.toList());
      }
      // Candidate suggesting.
      List<List<FeatureSuggestion>> fsByFeatsByWords =
          batchSuggestWithBatchType(errWords, features, top);
      List<SuggestionBuilder> sbList = errWords
          .stream()
          .map(w -> new SuggestionBuilder(w))
          .collect(Collectors.toList());
      fsByFeatsByWords.forEach(fsByWords -> {
        for (int i = 0; i < words.size(); i++) {
          try {
            sbList.get(i).add(fsByWords.get(i));
          } catch (DuplicateFeatureException e) {
            throw new RuntimeException(e);
          }
        }
      });
      return sbList
          .stream()
          .map(sb -> sb.build())
          .collect(Collectors.toList());
    });
  }

  public static List<Suggestion> suggest(List<Word> words, List<Feature> features, int top) {
    return suggest(words, features, top, false);
  }

  /**
   * Write suggestions to a text file.
   *
   * <p>A valid data file should formated follows:
   *
   * <p>- the file starts with a list of feature names, which are separated by a newline character.
   * Features follow by an empty line. - each error starts with one line containing its name,
   * following by its candidates. - each error candidate uses one line, containing the following
   * fields: candidate name, a list of candidate values, and a label. Fields are separated by a tab
   * character. - there is an empty line between each errors.
   *
   * @param suggestions A list of suggestions.
   * @param gtErrs A list of ground truth errors.
   * @param out The folder of the output files.
   * @throws IOException If I/O error occurs.
   */
  public static void writeText(List<Suggestion> suggestions, List<GroundTruthError> gtErrs,
      Path out) throws IOException {
    TIntObjectHashMap<GroundTruthError> errMap = new TIntObjectHashMap<>();
    for (GroundTruthError err : gtErrs) {
      errMap.put(err.position(), err);
    }

    Files.createDirectories(out.getParent());
    try (BufferedWriter bw = IOUtils.newBufferedWriter(out)) {
      List<FeatureType> types = suggestions.get(0).types();

      // Write features.
      for (FeatureType type : types) {
        bw.write(type.toString() + "\n");
      }
      bw.write("\n");

      // Write suggestions.
      for (Suggestion suggest : suggestions) {
        int pos = suggest.position();
        GroundTruthError err = errMap.get(pos);

        // Write suggestion name and position.
        bw.write(suggest.text() + "\t" + pos + "\n");

        // Write candidates.
        for (Candidate candidate: suggest.candidates()) {
          // TODO change to candidate.scores(types)
          StringBuilder candScores = new StringBuilder();
          float[] scores = candidate.scores();
          for (int i = 0; i < scores.length; i++) {
            candScores.append(scores[i]);
            if (i  < scores.length - 1) {
              candScores.append("\t");
            }
          }

          boolean match = false;
          if (err == null) {
            // Full match.
            match = suggest.text().equals(candidate.text());
            if (!match) {
              match = enPrefixMatch(suggest.text(), candidate.text());
            }
          } else {
            // TODO: check if bounded detection, the exact GT match is required.
            match = err.gtText().equals(candidate.text())
                 || err.gtTextAscii().equals(candidate.text());
            // TODO matching requires normalization?
            // boolean match = suggest.text().toLowerCase()
            // .equals(candidate.text().toLowerCase());
          }
          bw.write(String.format("%s\t%s\t%s\n", candidate.text(), candScores.toString(),
                match ? "1" : "0"));
        }
        bw.write("\n");
      }
    }
  }

  private static Pattern EN_PREFIX = Pattern.compile("^\\w+");

  private static boolean enPrefixMatch(String s1, String s2) {
    Matcher m1 = EN_PREFIX.matcher(s1);
    Matcher m2 = EN_PREFIX.matcher(s2);
    if (m1.find() && m2.find()) {
      return m1.group().equals(m2.group());
    } else {
      return false;
    }
  }

  /**
   * Write suggestions to one file.
   *
   * @param suggestions a list of suggestions.
   * @param out the path to the output files.
   * @throws IOException if I/O error occurs.
   */
  public static void write(List<Suggestion> suggestions, Path out) throws IOException {
    Files.createDirectories(out.getParent());
    try (ObjectOutputStream oos = new ObjectOutputStream(Channels.newOutputStream(
        FileChannel.open(out, StandardOpenOption.CREATE, StandardOpenOption.WRITE)))
    ){
      oos.writeInt(suggestions.size());
      for (Suggestion s : suggestions) {
        oos.writeObject(s);
      }
    }
  }

  public static void write(List<Suggestion> suggestions, Path folder, String prefix)
      throws IOException {
    int numLen = Integer.toString(suggestions.size()).length();
    Files.createDirectories(folder);
    IntStream
        .range(0, suggestions.size())
        .parallel()
        .forEach(i -> {
          Path outPath = folder.resolve(String.format("%s.%0" + numLen + "d", prefix, i));
          try (ObjectOutputStream oos = new ObjectOutputStream(Channels.newOutputStream(
                  FileChannel.open(outPath, StandardOpenOption.CREATE, StandardOpenOption.WRITE)))) {
            oos.writeObject(suggestions.get(i));
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        });
  }

  public static void write(Suggestion suggestion, Path path) throws IOException {
    try (ObjectOutputStream oos = new ObjectOutputStream(Channels.newOutputStream(
        FileChannel.open(path, StandardOpenOption.CREATE, StandardOpenOption.WRITE)))) {
      oos.writeObject(suggestion);
    }
  }

  /**
   * Write suggestions to a TSV file. A label will be assigned to each candidate if a labeler is
   * given.
   *
   * @param suggests a list of suggestions.
   * @param labeler a candidate correctness labeler.
   * @param path the output path.
   * @throws IOException if I/O error occurs.
   */
  public static void writeTSV(List<Suggestion> suggests, Labeler labeler, Path path)
      throws IOException {
    try (BufferedWriter bw = Files.newBufferedWriter(path, StandardOpenOption.CREATE)) {
      // Write the header line.
      ArrayList<String> headers = new ArrayList<>(Arrays.asList("position", "name", "candidate"));
      suggests.get(0).types()
              .stream()
              .map(FeatureType::toString)
              .forEachOrdered(headers::add);
      if (labeler != null) {
        headers.add("label");
      }
      bw.write(String.join("\t", headers) + "\n");
      // Write candidate suggestion records. Each candidate is recorded as one line in the output
      // file. The entire suggestion is written to the output file once.
      for (Suggestion suggest: suggests) {
        StringBuilder sb = new StringBuilder();
        for (Candidate c: suggest.candidates) {
          sb.append(suggest.position()).append('\t')
            .append(suggest.text()).append('\t')
            .append(c.text()).append('\t');
          for (int i = 0; i < c.scores().length; i++) {
            sb.append(c.scores()[i]).append('\t');
          }
          if (labeler == null) {
            sb.deleteCharAt(sb.length() - 1).append('\n');
          } else {
            sb.append(labeler.isCorrect(suggest.text(), c.text()) ? '1' : '0').append('\n');
          }
        }
        bw.write(sb.toString());
      }
    }
  }

  /**
   * Write suggestions to a TSV file.
   *
   * @param suggests a list of suggestions.
   * @param path the output path.
   * @throws IOException if I/O error occurs.
   */
  public static void writeTSV(List<Suggestion> suggests, Path path) throws IOException {
    writeTSV(suggests, null, path);
  }

  /**
   * Read suggestions from path.
   *
   * @param path the read path. It could be either a folder that contains a list
   *          of files able to be read by {@link #read(Path)}, or a file that
   *          contains a list of serialized suggestions.
   * @return a list of suggestions sorted by the position in text.
   * @throws IOException if I/O error occurs
   */
  public static List<Suggestion> readList(Path path) throws IOException {
    if (path == null) {
      throw new NullPointerException();
    }
    if (!Files.exists(path)) {
      throw new FileNotFoundException();
    }
    // Read suggestions from path.
    List<Suggestion> suggestions = new ArrayList<>();
    if (!Files.isDirectory(path)) {
      try (ObjectInputStream ois = new ObjectInputStream(Channels.newInputStream(FileChannel
          .open(path)))) {
        int size = ois.readInt();
        for (int i = 0; i < size; i++) {
          suggestions.add((Suggestion) ois.readObject());
        }
      } catch (IOException | ClassNotFoundException e) {
        throw new RuntimeException(e);
      }
    } else {
      List<Path> paths;
      paths = PathUtils.listPaths(path, "*");
      for (Path p : paths) {
        suggestions.add(read(p));
      }
    }
    // Sort suggestions by position.
    suggestions.sort((a, b) -> a.position() - b.position());
    // Log read suggestions.
    LogUtils.logToFile("suggest.read", false, (logger) -> {
      suggestions.stream().forEachOrdered(sug -> {
        logger.debug(String.format("%6d %-25s (%d candidates)",
              sug.position(), sug.text(), sug.candidates().length));
      });
    });
    return suggestions;
  }

  /**
   * Read a serialized suggestion object from path.
   *
   * @param path a path to the serialized object.
   * @return a suggestion.
   */
  public static Suggestion read(Path path) {
    try (ObjectInputStream ois = new ObjectInputStream(Channels.newInputStream(FileChannel
        .open(path)))) {
      return (Suggestion) ois.readObject();
    } catch (IOException | ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * A comparator that sort candidates by score.
   *
   * @param errorWord the error word that candidates are suggested for. It is
   *          the {@link Suggestion#text()} for the candidates belongs to.
   * @param feature the feature that two candidates are comparing.
   * @return an integer value used for sorting.
   *
   * @see Comparator#compare(Object, Object)
   */
  static Comparator<Candidate> sortByScore(FeatureType type) {
    return (c1, c2) -> {
      double diff = c1.score(type) - c2.score(type);
      return diff == 0 ? 0 : diff > 0 ? -1 : 1;
    };
  }

  static Comparator<Candidate> sortByMetric(String errorWord) {
    return (c1, c2) -> {
      NGram metric = new NGram(2, NGram.COMPREHENSIVE_COST);
      double diff = metric.distance(c1.text(), errorWord) - metric.distance(c2.text(), errorWord);
      return diff == 0 ? 0 : diff < 0 ? -1 : 1;
    };
  }

  static Comparator<Candidate> sortByFreq(String errorWord) {
    return (c1, c2) -> {
      double diff = Unigram.getInstance().freq(c1.text())
                  - Unigram.getInstance().freq(c2.text());
      return diff == 0 ? 0 : diff > 0 ? -1 : 1;
    };
  }

  /**
   * Create a new suggestion with only the top suggested candidates from each
   * feature type.
   *
   * @param suggest a suggestion.
   * @param top the number of candidates with the top feature scores remains in
   *          the output suggestion.
   * @return a new suggestion with only the top suggested candidates from each
   *         feature type.
   *
   * @see #sortByScore(String, Class)
   */
  public static Suggestion top(Suggestion suggest, int top) {
    String word = suggest.text();
    Set<Candidate> selected = new HashSet<>();
    List<Candidate> candidates = Stream.of(suggest.candidates())
          .sorted(sortByFreq(word))
          .sorted(sortByMetric(word))
          .collect(Collectors.toList());
    for (FeatureType type : suggest.types()) {
      ArrayList<Candidate> cloned = new ArrayList<>(candidates.size());
      for (Candidate c: candidates) {
        cloned.add(c);
      }
      cloned
          .stream()
          .sorted(sortByScore(type))
          .limit(top)
          .forEach(c -> selected.add(c));
    }
    return new Suggestion(suggest.text(), suggest.position(), suggest.featureRegistry,
        selected.toArray(new Candidate[selected.size()]));
  }

  public static List<Suggestion> top(List<Suggestion> suggests, int top) {
    return suggests
        .parallelStream()
        .map(s -> top(s, top))
        .collect(Collectors.toList());
  }

  /**
   * Regenerate a suggestion from a serialized suggestion object with the top
   * candidates using {@link #top(Suggestion, int)} and write the new suggestion
   * to another file.
   *
   * @param in the input path.
   * @param out the output path.
   * @param top the number of candidates with the top feature scores remains in
   *          the output suggestion.
   * @throws IOException if I/O error occurs.
   */
  public static void rewriteTop(Path in, Path out, int top) throws IOException {
    Files.createDirectories(out);
    List<Path> paths = null;
    paths = PathUtils.listPaths(in, "*");
    paths.parallelStream().forEach(p -> {
      String fname = p.getFileName().toString();
      Suggestion suggest = top(read(p), top);
      try {
        write(suggest, out.resolve(fname));
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    });
  }
}
