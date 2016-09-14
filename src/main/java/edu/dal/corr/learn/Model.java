package edu.dal.corr.learn;

public interface Model
{
  void fit(float[][] x, int[] y);
  
  void predict(float[] x);
}
