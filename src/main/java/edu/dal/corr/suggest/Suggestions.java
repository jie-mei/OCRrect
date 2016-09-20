package edu.dal.corr.suggest;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import edu.dal.corr.eval.GroundTruthError;
import edu.dal.corr.util.IOUtils;
import edu.dal.corr.util.LogUtils;
import edu.dal.corr.util.PathUtils;
import edu.dal.corr.word.Word;
import gnu.trove.map.hash.TIntObjectHashMap;

/**
 * @since 2016.09.19
 */
public class Suggestions
{
  private Suggestions() {}

  public static List<Suggestion> suggest(List<Word> words, List<Feature> features)
  {
    return LogUtils.logMethodTime(1, () ->
    {
      // Detection.
      List<Boolean> detects = Features.detect(words, features);
      
      // Filtering undetected words.
      List<Word> detectedWords = IntStream.range(0, words.size())
          .mapToObj(i -> (detects.get(i) ? words.get(i) : null))
          .filter(w -> w != null)
          .collect(Collectors.toList());

      // Suggesting.
      return Features.suggest(detectedWords, features)
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
   *    separated by a tab characters.
   * -  there is an empty line between each errors.
   * 
   * @param  suggestions  A list of suggestions.
   * @param  out  The folder of the output files.
   */
  public static void writeText(
      List<Suggestion> suggestions,
      List<GroundTruthError> gtErrors,
      Path out)
  {
    LogUtils.logMethodTime(1, () ->
    {
      try {
        Files.createDirectories(out.getParent());
        try (BufferedWriter bw = IOUtils.newBufferedWriter(out)) {
          TIntObjectHashMap<GroundTruthError> errMap =
              new TIntObjectHashMap<>();
          for (GroundTruthError err : gtErrors) {
            errMap.put(err.position(), err);
          }
          List<Class<? extends Feature>> types = suggestions.get(0).types();

          // Write features.
          for (Class<? extends Feature> type : types) {
            bw.write(type.getName() + "\n");
          }
          bw.write("\n");

          // Write suggestions.
          for (Suggestion suggest : suggestions) {

            // Write suggestion name.
            bw.write(suggest.text() + "\n");

            // Write candidates.
            float[][] scores = suggest.score(types);
            for (int i = 0; i < suggest.candidates().length; i++) {
              StringBuilder candScores = new StringBuilder();
              for (float s : scores[i]) {
                candScores.append(s).append('\t');
              }
              candScores.deleteCharAt(candScores.length() - 1);

              GroundTruthError err = errMap.get(suggest.position());
              Candidate candidate = suggest.candidates()[i];
              boolean match = (err == null
                  ? false
                  : match(err.gtText(), candidate.text()));

              bw.write(String.format("%s\t%s\t%s\n",
                  candidate.text(),
                  candScores.toString(),
                  match ? "1" : "0"));
            }
            bw.write("\n");
          }
        }
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    });
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
  
  private static String stripTail(String str)
  {
    // Remove the tailing non-English characters.
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
   * @param  suggestions  A list of suggestions.
   * @param  out  The folder of the output files.
   */
  public static void write(List<Suggestion> suggestions, Path out)
  {
    LogUtils.logMethodTime(1, () ->
    {
      try {
        Files.createDirectories(out.getParent());
        try (ObjectOutputStream oos = new ObjectOutputStream(
            Channels.newOutputStream(FileChannel.open(out,
                StandardOpenOption.CREATE, StandardOpenOption.WRITE)))) {
          oos.writeInt(suggestions.size());
          for (Suggestion s : suggestions) {
            oos.writeObject(s);
          }
        }
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    });
  }

  public static void write(List<Suggestion> suggestions, Path out, String name)
  {
    LogUtils.logMethodTime(1, () ->
    {
      int numLen = Integer.toString(suggestions.size()).length();
      try {
        Files.createDirectories(out);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
      IntStream.range(0, suggestions.size()).parallel().forEach(i -> {
        Path outPath = out.resolve(String.format("%s.%0" + numLen + "d", name, i));

        try (ObjectOutputStream oos = new ObjectOutputStream(
            Channels.newOutputStream(FileChannel.open(outPath,
                StandardOpenOption.CREATE, StandardOpenOption.WRITE)))) {
          oos.writeObject(suggestions.get(i));
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      });
    });
  }

  public static void write(Suggestion suggestion, Path out, String name)
  {
    try {
      Files.createDirectories(out);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    try (ObjectOutputStream oos = new ObjectOutputStream(
        Channels.newOutputStream(FileChannel.open(
            out.resolve(name),
            StandardOpenOption.CREATE, StandardOpenOption.WRITE)))) {
      oos.writeObject(suggestion);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
  
  public static List<Suggestion> readList(Path in)
  {
    return LogUtils.logMethodTime(1, () ->
    {
      if (in == null || ! Files.exists(in)) {
        return null;
      }

      List<Suggestion> suggestions = new ArrayList<>();
      if (! Files.isDirectory(in)) {
        try (ObjectInputStream ois = new ObjectInputStream(
            Channels.newInputStream(FileChannel.open(in)))) {
          int size = ois.readInt();
          for (int i = 0; i < size; i++) {
            suggestions.add((Suggestion) ois.readObject());
          }
        } catch (IOException | ClassNotFoundException e) {
          throw new RuntimeException(e);
        }
      } else {
        List<Path> paths;
        try {
          paths = PathUtils.listPaths(in, "*");
          for (Path p : paths)  {
            suggestions.add(read(p));
          }
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }

      LogUtils.logToFile("suggest.read", false, (logger) -> {
        suggestions.stream()
          .sorted((a, b) -> a.position() - b.position())
          .forEachOrdered(sug -> {
            logger.debug(String.format("%6d %-25s (%d candidates)",
                sug.position(), sug.text(), sug.candidates().length));
          });
      });
      return suggestions;
    });
  }
  
  public static Suggestion read(Path in)
  {
    try (ObjectInputStream ois = new ObjectInputStream(
        Channels.newInputStream(FileChannel.open(in)))) {
      return (Suggestion) ois.readObject();
    } catch (IOException | ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }
  
  /**
   * Create a new suggestion with only the top suggested candidates from each
   * feature type.
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

    for (Class<? extends Feature> type : suggest.types()) {
      Stream.of(candidates)
        .sorted(sortByScore(word, type))
        .limit(top)
        .forEach(c -> selected.add(c));
    }

    return new Suggestion(suggest.text(), suggest.position(), suggest.types(),
        selected.toArray(new Candidate[selected.size()]));
  }
  
  static Comparator<Candidate> sortByScore(
      String errorWord,
      Class<? extends Feature> type)
  {
    return (c1, c2) -> {
      double diff = c1.score(type) - c2.score(type);
      if (diff == 0) {
        diff = StringSimilarityScorer.lcs(c1.text(), errorWord)
             - StringSimilarityScorer.lcs(c2.text(), errorWord);
      }
      return diff == 0 ? 0 : diff > 0 ? -1 : 1;
    };
  }
  
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
      write(suggest, out, fname);
    });
  }
}
