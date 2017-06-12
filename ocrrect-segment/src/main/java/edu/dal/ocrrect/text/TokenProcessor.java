package edu.dal.ocrrect.text;

import edu.dal.ocrrect.util.Token;

import java.util.List;

public interface TokenProcessor {
  List<Token> process(List<Token> tokens);
}
