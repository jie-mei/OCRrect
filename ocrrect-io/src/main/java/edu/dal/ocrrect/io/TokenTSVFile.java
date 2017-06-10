package edu.dal.ocrrect.io;

import edu.dal.ocrrect.util.IOUtils;
import edu.dal.ocrrect.util.Token;

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
public class TokenTSVFile extends TSVFile<Token> {

  public TokenTSVFile(Path path) {
    super(path);
  }

  /**
   * Read elements from the TSV file.
   *
   * <p>Each line in the file records a token in the list, sorted by position. The token string and
   * its according position should be tab separated.
   *
   * @return a list of tokens.
   * @throws IOException if I/O error occurs.
   */
  @Override
  public List<Token> read() throws IOException {
    List<Token> tokens = new ArrayList<>();
    try (BufferedReader br = Files.newBufferedReader(this.path())) {
      for (String line; (line = br.readLine()) != null;) {
        String[] fields = line.split("\\t");
        tokens.add(new Token(fields[0], Integer.parseInt(fields[1])));
      }
    }
    return tokens;
  }

  @Override
  public void write(List<Token> elements) throws IOException {
    try (BufferedWriter bw = IOUtils.newBufferedWriter(this.path(), StandardOpenOption.CREATE)){
      elements.sort(Comparator.comparing(Token::position));
      for (Token t: elements) {
        bw.write(t.text() + "\t" + t.position() + "\n");
      }
    }
  }
}
