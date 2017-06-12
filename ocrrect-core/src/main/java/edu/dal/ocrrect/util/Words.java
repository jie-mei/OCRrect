package edu.dal.ocrrect.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Words {

  /**
   * Generate words from tokens.
   *
   * @param tokens a list of tokens
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
