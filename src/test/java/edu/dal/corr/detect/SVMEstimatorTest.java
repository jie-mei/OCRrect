package edu.dal.corr.detect;

import static org.junit.Assert.*;

import java.util.Arrays;
import org.junit.Test;

public class SVMEstimatorTest {

  @Test
  public void testTrainAndTest() {
    SVMEstimator svm = new SVMEstimator("/Users/Porzire/miniconda3/bin/python");
    float[][] trainData = {
        {0.0f,0.14f,0.0f,0.67f},
        {0.0f,0.54f,0.0f,0.67f},
        {1.0f,0.32f,1.0f,1.0f},
        {0.0f,0.02f,0.0f,0.0f}
    };
    boolean[] labels = {
        false, true, false, true
    };
    svm.train(trainData, labels);
    float[][] testData = {
        {1.0f,0.04f,1.0f,0.67f},
        {0.0f,0.34f,0.0f,0.33f},
        {1.0f,0.02f,0.0f,0.0f},
        {0.0f,0.32f,1.0f,1.0f}
    };
    boolean[] predicted = svm.predict(testData);
    assertTrue(Arrays.equals(new boolean[]{false, true, true, false}, predicted));
  }
}
