package edu.dal.corr.word;

import edu.dal.corr.util.IOUtils;
import edu.dal.corr.util.LocatedTextualUnit;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

/**
 * The token class encapsulate the string representation and the position of a token.
 *
 * @since 2017.04.20
 */
public class Token extends LocatedTextualUnit implements Serializable {
  private static final long serialVersionUID = -8116466570752778955L;

  static Token EMPTY = new Token("", -1);

  public Token(String name, int position) {
    super(name, position);
  }

  /**
   * Generate words from tokens.
   *
   * @param tokens a list of tokens
   * @param filters an array of word filters.
   * @return a list of words.
   */
  public static List<Word> toWords(List<Token> tokens) {
    tokens.sort((t1, t2) -> t1.position() - t2.position());

    List<Token> expend = new ArrayList<>();
    for (int i = 0; i < 4; i++) expend.add(Token.EMPTY);
    expend.addAll(tokens);
    for (int i = 0; i < 3; i++) expend.add(Token.EMPTY);

    List<Word> words = new ArrayList<>();
    for (int i = 0; i < tokens.size(); i++) {
      words.add(new Word(expend.get(i + 4).position(),
          expend.get(i).text(),
          expend.get(i + 1).text(),
          expend.get(i + 2).text(),
          expend.get(i + 3).text(),
          expend.get(i + 4).text(),
          expend.get(i + 5).text(),
          expend.get(i + 6).text(),
          expend.get(i + 7).text()
          ));
    }
    return words;
  }

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
    try (BufferedReader br = IOUtils.newBufferedReader(path)) {
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
    try (
      BufferedWriter bw = IOUtils.newBufferedWriter(path,
          StandardOpenOption.CREATE)
    ){
      tokens.sort((a, b) -> a.position() - b.position());
      for (Token t: tokens) {
        bw.write(t.text() + "\t" + t.position() + "\n");
      }
    }
  }
}
