package edu.dal.corr.detect;

import edu.dal.corr.word.Word;
import java.util.Arrays;
import java.util.List;

/**
 * @since 2017.04.26
 */
public abstract class DetectionEstimator {
  List<DetectionFeature> detectables;

  public DetectionEstimator(DetectionFeature...detectables) {
    this.detectables = Arrays.asList(detectables);
  }
  
  protected float[] toScores(Word word) {
    float[] scores = new float[detectables.size()];
    for (int i = 0; i < detectables.size(); i++) {
      scores[i] = detectables.get(i).detect(word);
    }
    return scores;
  }
  
  protected float[][] toScores(List<Word> words) {
    float[][] scores = new float[words.size()][];
    for (int i = 0; i < words.size(); i++) {
      scores[i] = toScores(words.get(i));
    }
    return scores;
  }

  public abstract boolean[] predict(float[][] scores);

  public boolean[] predict(List<Word> words) {
    return predict(toScores(words));
  }
}
