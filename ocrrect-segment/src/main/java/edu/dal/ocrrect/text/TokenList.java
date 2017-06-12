package edu.dal.ocrrect.text;

import edu.dal.ocrrect.util.Token;

import java.util.List;

public class TokenList {
  private List<Token> tokens;

  public TokenList(List<Token> tokens) {
    this.tokens = tokens;
  }

  public TokenList process(Processor<Token> processor) {

  }
}
