package edu.dal.ocrrect.detect;

import com.google.common.primitives.Booleans;

import edu.dal.ocrrect.util.LogUtils;
import edu.dal.ocrrect.util.Word;

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
