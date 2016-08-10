package edu.dal.corr.word;

import java.io.StringReader;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;

public class PennTreebankTokenizer
  implements Tokenizer
{
  private PTBTokenizer<CoreLabel> ptbt;
  
  protected void ensureTokenizer()
  {
    if (ptbt == null) {
      throw new RuntimeException("Tokenization task uninitializated.");
    }
  }

  @Override
  public void tokenize(String content)
  {
    ptbt = new PTBTokenizer<>(new StringReader(content),
        new CoreLabelTokenFactory(),
        "ptb3Escaping=false,normalizeOtherBrackets=false");
  }

  @Override
  public boolean hasNextToken()
  {
    ensureTokenizer();
    return ptbt.hasNext();
  }

  @Override
  public Token nextToken()
  {
    ensureTokenizer();
    CoreLabel token = ptbt.next();
    return new Token(token.toString(), token.beginPosition());
  }
}
