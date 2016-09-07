package edu.dal.corr.suggest;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import edu.dal.corr.util.LogUtils;
import edu.dal.corr.util.PathUtils;
import edu.dal.corr.word.Word;

/**
 * @since 2016.08.23
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
        .sorted((c1, c2) -> {
          double diff = c1.score(type) - c2.score(type);
          if (diff == 0) {
            diff = StringSimilarityScorer.lcs(c1.text(), word)
                 - StringSimilarityScorer.lcs(c2.text(), word);
          }
          return diff == 0 ? 0 : diff > 0 ? -1 : 1;
        })
        .limit(top)
        .forEach(c -> selected.add(c));
    }

    return new Suggestion(suggest.text(), suggest.position(), suggest.types(),
        selected.toArray(new Candidate[selected.size()]));
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
