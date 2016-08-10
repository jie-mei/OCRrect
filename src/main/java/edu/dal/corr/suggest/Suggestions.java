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

import edu.dal.corr.util.LogUtils;
import edu.dal.corr.word.Word;

public class Suggestions
{
  private Suggestions() {}

  public static List<Suggestion> suggest(List<Word> words, List<Feature> features)
  {
    return LogUtils.logMethodTime(1, () ->
    {
      return Features.suggest(words, features).stream()
          .map(fsList -> {
            SuggestionBuilder sb = new SuggestionBuilder(fsList.get(0).text(), fsList.get(0).position());
            fsList.stream().forEach(fs -> sb.add(fs));
            return sb.build();
          })
          .collect(Collectors.toList());
    });
  }
  
  public static void write(List<Suggestion> suggestions, Path out)
  {
    LogUtils.logMethodTime(1, () ->
    {
      try {
        Files.createDirectories(out.getParent());
        try (ObjectOutputStream oos = new ObjectOutputStream(
            Channels.newOutputStream(FileChannel.open(out,
                StandardOpenOption.WRITE)))) {
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
  
  public static List<Suggestion> read(Path in)
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
