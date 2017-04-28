package edu.dal.corr.detect;

import com.google.common.primitives.Booleans;

import edu.dal.corr.util.LogUtils;
import edu.dal.corr.word.Word;
import java.util.List;

public abstract class SupervisedDetectionEstimator extends DetectionEstimator {
  
  public SupervisedDetectionEstimator(Detectable...features) {
    super(features);
  }

  public abstract void train(float[][] scores, boolean[] labels);

  public void train(List<Word> words, List<Boolean> labels) {
    LogUtils.logMethodTime(2, () -> train(toScores(words), Booleans.toArray(labels)));
  }
}
