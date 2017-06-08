package edu.dal.ocrrect.detect;

import edu.dal.ocrrect.util.LogUtils;
import edu.dal.ocrrect.word.Word;

import java.util.Arrays;
import java.util.List;

/**
 * @since 2017.04.26
 */
public abstract class DetectionEstimator {
  List<Detectable> features;

  public DetectionEstimator(Detectable...features) {
    if (features.length == 0) {
      throw new IllegalArgumentException("At least one feature is required");
    }
    this.features = Arrays.asList(features);
  }
  
  protected float[][] toScores(List<Word> words) {
    float[][] scores = new float[words.size()][];
    for (int i = 0; i < words.size(); i++) {
      scores[i] = new float[features.size()];
    }
    for (int i = 0; i < features.size(); i++) {
      final int fidx = i;
      LogUtils.logTime(3, ()-> {
        // make detections by each feature.
        float[] vals = features.get(fidx).detect(words);
        for (int j = 0; j < words.size(); j++) {
          scores[j][fidx] = vals[j];
        }
      }, features.get(i) + ".detect()");
    }
    return scores;
  }

  public abstract boolean[] predict(float[][] scores);

  public boolean[] predict(List<Word> words) {
    return LogUtils.logMethodTime(2, () -> predict(toScores(words)));
  }
}
