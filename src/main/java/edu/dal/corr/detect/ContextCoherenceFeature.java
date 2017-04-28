package edu.dal.corr.detect;

import edu.dal.corr.suggest.NgramBoundedReaderSearcher;
import edu.dal.corr.word.Context;
import edu.dal.corr.word.Word;
import gnu.trove.map.hash.TObjectFloatHashMap;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;

/**
 * @since 2017.04.27
 */
public class ContextCoherenceFeature extends DetectionFeature {
  private NgramBoundedReaderSearcher reader;
  private int ngramSize;

  public ContextCoherenceFeature(String name, NgramBoundedReaderSearcher reader, int ngramSize) {
    setName(name);
    this.reader = reader;
    this.ngramSize = ngramSize;
  }

  /**
   * @param ngram a word ngram in the corpus.
   * @param context a word ngram context.
   * @return a valid word substitution, or {@null} otherwise.
   */
  protected String substitueWord(String ngram, Context context) {
    String[] grams = ngram.split(" ");
    // Detect whether this ngram contains a word substitution.
    String sub = grams[context.index()];
    for (int i = 0; i < ngramSize; i++) {
      if (context.index() != i && !context.words()[i].equals(grams[i])) { // not a valid substitution
        sub = null;
        break;
      }
    }
    return sub;
  }

  protected int ngramSize() {
    return ngramSize;
  }

  @Override
  public float detect(Word word) {
    TObjectFloatHashMap<String> wordMap = new TObjectFloatHashMap<>();
    List<Context> contexts = word.getContexts(ngramSize);
    for (Context c: contexts) {
      try (BufferedReader br = reader.openBufferedRecordsWithFirstWord(c.words()[0])) {
        if (br != null) {
          for (String line = br.readLine(); line != null; line = br.readLine()) {
            String[] splits = line.split("\t");
            String sub = substitueWord(splits[0], c);
            if (sub != null) {
              // Records the sum of log n-gram frequencies of the possible word substitutions.
              float val = (float)Math.log(Long.parseLong(splits[1]));
              wordMap.adjustOrPutValue(sub, val, val);
            }
          }
        }
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
    if (wordMap.get(word.text()) == 0) {
      return 0;
    } else {
      float max = 0;
      for (float val: wordMap.values()) {
        if (val > max) { max = val; }
      }
      return wordMap.get(word.text()) / max;
    }
  }
}
