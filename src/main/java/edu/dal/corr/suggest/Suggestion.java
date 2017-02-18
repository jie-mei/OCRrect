package edu.dal.corr.suggest;

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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.log4j.Logger;

import edu.dal.corr.metric.NGram;
import edu.dal.corr.suggest.feature.Feature;
import edu.dal.corr.suggest.feature.FeatureType;
import edu.dal.corr.util.IOUtils;
import edu.dal.corr.util.LocatedTextualUnit;
import edu.dal.corr.util.LogUtils;
import edu.dal.corr.util.PathUtils;
import edu.dal.corr.word.Word;
import gnu.trove.map.TObjectFloatMap;


/**
 * @since 2017.02.15
 */
public class Suggestion
  extends LocatedTextualUnit
  implements Serializable
{
  private static final long serialVersionUID = 4175847645213310315L;
  private static final Logger LOG = Logger.getLogger(Suggestion.class);

  private static final int NON_MAPPING = -1;

  private final List<FeatureType> types;
  private final Candidate[] candidates;

  Suggestion(String name,
             int position,
             List<FeatureType> types,
             Candidate[] candidates)
  {
    super(name, position);
    this.types = types;
    this.candidates = candidates;
  }
  
  /**
   * Get suggested correction candidates.
   *
   * @return all candidates suggested.
   */
  public Candidate[] candidates() { return candidates; }
  
  /**
   * Get feature scores for each candidates.
   * 
   * @param  types  a list of types that sort order of the output scores for
   *    each candidate.
   * @return a two dimensional array, which first dimensional represents the
   *    index of the candidates according to {@link #candidates()} and second
   *    dimension represents the score for each feature sorted by the give type
   *    list.
   */
  public float[][] score(List<FeatureType> types)
  {
    // Create a mapping for types to the new position in the output array.
    int[] map = new int[types.size()];
    Arrays.fill(map, NON_MAPPING);
    for (int i = 0; i < types.size(); i++) {
      for (int j = 0; j < types.size(); j++) {
        if (types.get(i) == types.get(j)) {
          map[i] = j;
          break;
        }
      }
    }
    float[][] scores = new float[candidates.length][types.size()];
    for (int i = 0; i < candidates.length; i++) {
      float[] candScore = candidates[i].score();
      for (int j = 0; j < types.size(); j++) {
        if (map[j] != NON_MAPPING) {
          scores[i][map[j]] = candScore[j];
        }
      }
    }
    return scores;
  }

  public List<FeatureType> types() {
    return types;
  }
  
  @Override
  protected HashCodeBuilder buildHash() {
    return super.buildHash()
        .append(candidates)
        .append(types);
  }

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
  public static List<Boolean> detect(List<Word> words, List<Feature> features)
  {
    return LogUtils.logMethodTime(2, () ->
    {
      // Detect for each word using all features.
      List<List<Boolean>> decisions = features.stream()
          .map(feat -> LogUtils.logTime(3, () -> feat.detect(words),
              feat.getClass().getName() + ".detect()" +
              (feat.type().name() == null ? "": " [" + feat.type().name() + "]")))
          .collect(Collectors.toList());

      // Make final decision for each word.
      return IntStream.range(0, words.size())
          .mapToObj(i -> decisions.stream()
              .map(list -> list.get(i))
              .collect(Collectors.toList()))
          .map(Suggestion::detect)
          .collect(Collectors.toList());
    });
  }

  static List<List<FeatureSuggestion>> searchAndScore(
      List<Word> words,
      List<Feature> features)
  {
    return LogUtils.logMethodTime(2, () ->
    {
      // Suggest for each word using all features.
      List<List<FeatureSuggestion>> fsListedByWordsByFeatures = features
          .stream()
          .map(feat -> {

            // Perform benchmark suggestion for all words.
            String logInfo = feat.getClass().getName() + ".suggest()"
                + (feat.type().name() == null ? "": " [" + feat.type().name() + "]");
            List<TObjectFloatMap<String>> mapList = LogUtils.logTime(3, () -> {
              if (LOG.isInfoEnabled()) { LOG.info(logInfo); }
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
          .mapToObj(i
            -> fsListedByWordsByFeatures.stream()
              .map(fsListedbyWordsOfFeature -> fsListedbyWordsOfFeature.get(i))
              .collect(Collectors.toList()))
          .collect(Collectors.toList());
    });
  }

  /**
   * Generate correction suggestions.
   * 
   * @param words  A list of words.
   * @param features  A list of features.
   * @return  A list of suggestions.
   */
  public static List<Suggestion> suggest(List<Word> words, List<Feature> features)
  {
    return LogUtils.logMethodTime(1, () ->
    {
      // Detection.
      List<Boolean> detects = detect(words, features);
      
      // Filtering undetected words.
      List<Word> detectedWords = IntStream.range(0, words.size())
          .mapToObj(i -> (detects.get(i) ? words.get(i) : null))
          .filter(w -> w != null)
          .collect(Collectors.toList());

      // Suggesting.
      return searchAndScore(detectedWords, features)
          .stream()
          .map(fsListForWord -> {
            FeatureSuggestion fs = fsListForWord.get(0);  // anyone in the list.
            SuggestionBuilder sb = new SuggestionBuilder(fs.text(), fs.position());
            fsListForWord.stream().forEach(s -> sb.add(s));
            return sb.build();
          })
          .collect(Collectors.toList());
    });
  }
  
  /**
   * Write suggestions to a text file.
   * 
   * A valid data file should formated follows:
   *
   * -  the file starts with a list of feature names, which are separated by a
   *    newline character. Features follow by an empty line.
   * -  each error starts with one line containing its name, following by its
   *    candidates.
   * -  each error candidate uses one line, containing the following fields:
   *    candidate name, a list of candidate values, and a label. Fields are
   *    separated by a tab character.
   * -  there is an empty line between each errors.
   * 
   * @param  suggestions  A list of suggestions.
   * @param  out  The folder of the output files.
   * 
   * @throws IOException  If I/O error occurs.
   */
  public static void writeText(List<Suggestion> suggestions, Path out)
    throws IOException
  {
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

        // Write suggestion name and detection type.
        bw.write(suggest.text() + "\n");

        // Write candidates.
        float[][] scores = suggest.score(types);
        for (int i = 0; i < suggest.candidates().length; i++) {
          StringBuilder candScores = new StringBuilder();
          for (float s : scores[i]) {
            candScores.append(s).append('\t');
          }
          candScores.deleteCharAt(candScores.length() - 1);
          Candidate candidate = suggest.candidates()[i];

          // TODO matching requires normalization?
          boolean match = match(suggest.text(), candidate.text());
          // boolean match = suggest.text().toLowerCase()
          //    .equals(candidate.text().toLowerCase());

          bw.write(String.format("%s\t%s\t%s\n",
              candidate.text(),
              candScores.toString(),
              match ? "1" : "0"));
        }
        bw.write("\n");
      }
    }
  }
  
  private static boolean match(String gtName, String str)
  {
    if (gtName.toLowerCase().equals(str.toLowerCase()) ||
        stripTail(gtName.toLowerCase()).equals(str.toLowerCase())) {
      return true;
    } else {
      return false;
    }
  }
  
  /*
   * Remove the tailing non-English characters.
   */
  private static String stripTail(String str)
  {
    for (int i = str.length() - 1; i > 0; i--) {
      char c = str.charAt(i);
      if (Character.isLetter(c)) {
        return str.substring(0, i + 1);
      }
    }
    return "";
  }
  
  /**
   * Write suggestions to one file.
   * 
   * @param  suggestions  a list of suggestions.
   * @param  out  the path to the output files.
   * @throws IOException  if I/O error occurs. 
   */
  public static void write(List<Suggestion> suggestions, Path out)
    throws IOException
  {
    Files.createDirectories(out.getParent());
    try (ObjectOutputStream oos = new ObjectOutputStream(
        Channels.newOutputStream(FileChannel.open(out,
            StandardOpenOption.CREATE, StandardOpenOption.WRITE)))) {
      oos.writeInt(suggestions.size());
      for (Suggestion s : suggestions) {
        oos.writeObject(s);
      }
    }
  }

  public static void write(List<Suggestion> suggestions,
                           Path folder, String prefix)
    throws IOException
  {
    int numLen = Integer.toString(suggestions.size()).length();
    Files.createDirectories(folder);
    IntStream.range(0, suggestions.size()).parallel().forEach(i -> {
      Path outPath = folder.resolve(String.format("%s.%0" + numLen + "d", prefix, i));
      try (ObjectOutputStream oos = new ObjectOutputStream(
          Channels.newOutputStream(FileChannel.open(
              outPath, StandardOpenOption.CREATE, StandardOpenOption.WRITE)))
      ){
        System.out.println(String.format("%s: %d candidates",
            outPath.getFileName(),
            suggestions.get(i).candidates.length));
        oos.writeObject(suggestions.get(i));
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    });
  }

  public static void write(Suggestion suggestion, Path path)
    throws IOException
  {
    try (
      ObjectOutputStream oos = new ObjectOutputStream(
          Channels.newOutputStream(FileChannel.open(
              path, StandardOpenOption.CREATE, StandardOpenOption.WRITE)))
    ){
      oos.writeObject(suggestion);
    }
  }
  
  /**
   * Read suggestions from path.
   * 
   * @param  path  the read path. It could be either a folder that contains a
   *    list of files able to be read by {@link #read(Path)}, or a file that
   *    contains a list of serialized suggestions.
   * @return a list of suggestions sorted by the position in text.
   * @throws IOException  if I/O error occurs 
   */
  public static List<Suggestion> readList(Path path)
    throws IOException
  {
    if (path == null) {
      throw new NullPointerException();
    } 
    if (! Files.exists(path)) {
      throw new FileNotFoundException();
    }

    // Read suggestions from path.
    List<Suggestion> suggestions = new ArrayList<>();
    if (! Files.isDirectory(path)) {
      try (ObjectInputStream ois = new ObjectInputStream(
          Channels.newInputStream(FileChannel.open(path)))) {
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
      for (Path p : paths)  {
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
   * @param  path  a path to the serialized object.
   * @return a suggestion.
   */
  public static Suggestion read(Path path)
  {
    try (ObjectInputStream ois = new ObjectInputStream(
        Channels.newInputStream(FileChannel.open(path)))) {
      return (Suggestion) ois.readObject();
    } catch (IOException | ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }
  
  /**
   * A comparator that sort candidates by score.
   * 
   * @param  errorWord  the error word that candidates are suggested for. It is
   *    the {@link Suggestion#text()} for the candidates belongs to.
   * @param  feature  the feature that two candidates are comparing.
   * @return an integer value used for sorting.
   * 
   * @see Comparator#compare(Object, Object)
   */
  static Comparator<Candidate> sortByScore(FeatureType type)
  {
    return (c1, c2) -> {
      double diff = c1.score(type) - c2.score(type);
      return diff == 0 ? 0 : diff > 0 ? -1 : 1;
    };
  }

  static Comparator<Candidate> sortByMetric(String errorWord)
  {
    return (c1, c2) -> {
      NGram metric = new NGram(2, NGram.COMPREHENSIVE_COST);
      double diff = metric.distance(c1.text(), errorWord)
                  - metric.distance(c2.text(), errorWord);
      return diff == 0 ? 0 : diff < 0 ? -1 : 1;
    };
  }
  
  /**
   * Create a new suggestion with only the top suggested candidates from each
   * feature type.
   * 
   * @param  suggest  a suggestion.
   * @param  top  the number of candidates with the top feature scores remains
   *    in the output suggestion.
   * @return a new suggestion with only the top suggested candidates from each
   *    feature type.
   *    
   * @see #sortByScore(String, Class)
   */
  public static Suggestion top(Suggestion suggest, int top)
  {
    String word = suggest.text();
    Candidate[] candidates = suggest.candidates();

    HashMap<String, Candidate> candMap = new HashMap<>();
    for (int i = 0; i < suggest.candidates().length; i++) {
      Candidate c = suggest.candidates()[i];
      candMap.put(c.text(), c);
    }
    Set<Candidate> selected = new HashSet<>();

    for (FeatureType type: suggest.types()) {
      Stream.of(candidates)
        .sorted(sortByMetric(word))
        .sorted(sortByScore(type))
        .limit(top)
        .forEach(c -> selected.add(c));
    }

    return new Suggestion(suggest.text(), suggest.position(),
        suggest.types(), selected.toArray(new Candidate[selected.size()]));
  }
  
  public static List<Suggestion> top(List<Suggestion> suggests, int top)
  {
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
   * @param  in  the input path.
   * @param  out the output path.
   * @param  top the number of candidates with the top feature scores remains
   *    in the output suggestion.
   */
  public static void rewriteTop(Path in, Path out, int top)
  {
    List<Path> paths = null;
    try {
      paths = PathUtils.listPaths(in, "*");
    } catch (IOException e) {
      new RuntimeException(e);
    }
    paths.parallelStream().forEach(p -> {
      String fname = p.getFileName().toString();
      Suggestion suggest = top(read(p), top);
      try {
        write(suggest, out.getParent().resolve(fname));
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    });
  }
}
