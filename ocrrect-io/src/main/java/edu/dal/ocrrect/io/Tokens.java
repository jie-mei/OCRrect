package edu.dal.ocrrect.io;

import edu.dal.ocrrect.util.IOUtils;
import edu.dal.ocrrect.word.Token;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 */
public class Tokens {
  /**
   * Read tokens from a formatted text file.
   *
   * <p>Each line in the file records a token in the list, sorted by position. The token string and
   * its according position should be tab separated.
   *
   * @param path a path.
   * @return a list of tokens.
   * @throws IOException if I/O error occurs.
   */
  public static List<Token> readTSV(Path path) throws IOException {
    List<Token> tokens = new ArrayList<>();
    try (BufferedReader br = Files.newBufferedReader(path)) {
      for (String line; (line = br.readLine()) != null;) {
        String[] fields = line.split("\t");
        tokens.add(new Token(fields[0], Integer.parseInt(fields[1])));
      }
    }
    return tokens;
  }

  /**
   * Write tokens to a formatted text file.
   *
   * @param tokens a token list.
   * @param path a path.
   * @throws IOException
   */
  public static void writeTSV(List<Token> tokens, Path path) throws IOException {
    try (BufferedWriter bw = IOUtils.newBufferedWriter(path, StandardOpenOption.CREATE)){
      tokens.sort(Comparator.comparing(Token::position));
      for (Token t: tokens) {
        bw.write(t.text() + "\t" + t.position() + "\n");
      }
    }
  }
}