package edu.dal.corr.word;

/**
 * An object that implements the {@code Tokenizer} interface generates a series
 * of tokens from a text.
 * 
 * @since 2016.07.24
 */
public interface Tokenizer
{
  /**
   * Tokenize the content. This method initializes the tokenization task and
   * should be called before {@link #hasNextToken()} and {@link #nextToken()}.
   * 
   * @param  content A string.
   */
  void tokenize(String content);

  /**
   * Return {@code true} if there is more token available.
   * 
   * @return {@code true} if there is at least one more token after current
   *    position; {@code false} otherwise.
   */
  boolean hasNextToken();
  
  /**
   * Return the next token.
   * 
   * @return A token string.
   */
  Token nextToken();
}
