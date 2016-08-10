package edu.dal.corr;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;

import edu.dal.corr.suggest.Candidate;
import edu.dal.corr.suggest.Suggestion;
import edu.dal.corr.suggest.Suggestions;

public class TestingSuggestion
{
  public static void main(String[] args)
      throws IOException, ClassNotFoundException {
    List<Suggestion> suggestions = Suggestions.read(Paths.get("tmp/suggestion.out"));
    suggestions.stream()
        .map(Suggestion::text)
        .forEach(System.out::println);
    for (int i = 0; i < 10; i++) {
      Candidate c = suggestions.get(0).candidates()[i];
      System.out.println(c.text() + " " + Arrays.toString(c.score()));
    }
  }
}
