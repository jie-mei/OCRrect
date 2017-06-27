package edu.dal.ocrrect.io;

import edu.dal.ocrrect.util.Word;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * IO utilities for words.
 */
public class WordTSVFile extends TSVFile<Word> {

  public WordTSVFile(Path path) {
    super(path);
  }

  @Override
  public List<Word> read() throws IOException {
    try (Stream<String> lines = Files.lines(this.path())) {
      return lines
        .map(l -> {
          String[] splits = l.split("\t");
          int pos = Integer.parseInt(splits[splits.length - 1]);
          String[] ctxt = Arrays.copyOfRange(splits, 0, splits.length - 1);
          return new Word(pos, ctxt);
        })
        .collect(Collectors.toList());
    }
  }

  @Override
  public void write(List<Word> elements, OpenOption... options) throws IOException {
    // Concatenate the CSV records and write the entire string at once.
    String tsvStr = elements
        .stream()
        .map(w -> String.join("\t", w.context()) + "\t" + w.position())
        .collect(Collectors.joining("\n"));
    try (BufferedWriter bw = Files.newBufferedWriter(this.path(), options)) {
      bw.write(tsvStr);
    }
  }
}
