package edu.dal.ocrrect.word;

import java.util.LinkedList;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @since 2017.04.20
 */
public class GoogleTokenizer extends PennTreebankTokenizer {
  private final static Pattern SPLIT_PATTERN = Pattern.compile("([^-]+|-)");

  /**
   * The tokens has been extracted from text but not yet given via {@link nextToken()}.
   */
  private Queue<Token> cache;

  public GoogleTokenizer() {
  	super();
    cache = new LinkedList<>();
  }

  @Override
  public boolean hasNextToken() {
    if (cache.size() > 0) {
      return true;
    } else {
      boolean hasToken = super.hasNextToken();
      return hasToken;
    }
  }

  @Override
  public Token nextToken() {
    Token next = null;
    if ((next = cache.poll()) == null) {
      Token token = super.nextToken();
      Matcher m = SPLIT_PATTERN.matcher(token.text());
      while(m.find()) {
        cache.add(new Token(m.group(), token.position() + m.start()));
      }
      next = cache.poll();
    }
    return next;
  }
}
