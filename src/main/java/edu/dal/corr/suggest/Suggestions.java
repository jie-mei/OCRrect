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
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import edu.dal.corr.util.LogUtils;
import edu.dal.corr.word.Word;

/**
 * @since 2016.08.10
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
   * Write suggestions to file.
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
        for (int i = 0; i < suggestions.size(); i++) {
          Path outPath = out.resolve(String.format("%s.%0" + numLen + "d", name, i));

          try (ObjectOutputStream oos = new ObjectOutputStream(
              Channels.newOutputStream(FileChannel.open(outPath,
                  StandardOpenOption.CREATE, StandardOpenOption.WRITE)))) {
            oos.writeObject(suggestions.get(i));
          }
        }
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    });
  }
  
  
  public static List<Suggestion> readList(Path in)
  {
    return LogUtils.logMethodTime(1, () ->
    {
      List<Suggestion> suggestions = new ArrayList<>();
      try (ObjectInputStream ois = new ObjectInputStream(
          Channels.newInputStream(FileChannel.open(in)))) {
        int size = ois.readInt();
        for (int i = 0; i < size; i++) {
          suggestions.add((Suggestion) ois.readObject());
        }
      } catch (IOException | ClassNotFoundException e) {
        throw new RuntimeException(e);
      }
      return suggestions;
    });
  }
}
