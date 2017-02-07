package edu.dal.corr.word;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

import edu.dal.corr.util.IOUtils;
import edu.dal.corr.util.LocatedTextualUnit;

/**
* The token class encapsulate the string representation and the position of a
* token.
*
* @since 2016.08.10
*/
public class Token
  extends LocatedTextualUnit
  implements Serializable
{
  private static final long serialVersionUID = -8116466570752778955L;

  public static Token EMPTY = new Token("", -1);

  Token(String name, int position)
  {
    super(name, position);
  }
  
  /**
   * Read tokens from a formatted text file.
   * <p>
   * Each line in the file records a token in the list, sorted by position. The
   * token string and its according position should be tab separated.
   * 
   * @param  path  a path.
   * @return a list of tokens.
   * @throws IOException  if I/O error occurs.
   */
  public static List<Token> read(Path path)
    throws IOException
  {
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
   * @param  tokens  a token list.
   * @param  path  a path.
   * @throws IOException 
   */
  public static void write(List<Token> tokens, Path path)
    throws IOException
  {
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
