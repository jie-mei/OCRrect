package edu.dal.ocrrect.word;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import edu.dal.ocrrect.util.LocatedTextualUnit;

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
    tokens.sort(Comparator.comparing(Token::position));

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
}
