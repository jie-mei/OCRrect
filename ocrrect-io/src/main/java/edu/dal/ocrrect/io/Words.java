package edu.dal.ocrrect.io;

import edu.dal.ocrrect.word.Word;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * IO utilities for words.
 */
public class Words {
  /**
   * Read a list of words from a formatted CSV file.
   *
   * @param path the input path.
   * @return a list of words.
   * @throws IOException if I/O error occurs.
   */
  public static List<Word> readTSV(Path path) throws IOException {
    try (Stream<String> lines = Files.lines(path)) {
      return lines
        .map(l -> {
          String[] splits = l.split("\t");
          int pos = Integer.parseInt(splits[0]);
          String[] ctxt = Arrays.copyOfRange(splits, 1, splits.length);
          return new Word(pos, ctxt);
        })
        .collect(Collectors.toList());
    }
  }

  /**
   * Write a list of words into a CSV file.
   *
   * @param words a list of words.
   * @param path the output path.
   * @throws IOException if I/O error occurs.
   */
  public static void writeTSV(List<Word> words, Path path) throws IOException {
    // Concatenate the CSV records and write the entire string at once.
    String tsvStr = words
      .stream()
      .map(w -> w.position() + "\t" + String.join("\t", w.context()))
      .collect(Collectors.joining("\n"));
    try (BufferedWriter bw = Files.newBufferedWriter(path, StandardOpenOption.CREATE)) {
      bw.write(tsvStr);
    }
  }
}
