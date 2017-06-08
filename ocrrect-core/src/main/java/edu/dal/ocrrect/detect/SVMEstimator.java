package edu.dal.ocrrect.detect;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;

import edu.dal.ocrrect.util.ResourceUtils;

/**
 * Use scikit-learn SVM model. Methods in this class calls python and requires the following python
 * packages:
 *
 * <ul>
 *   <li>numpy
 *   <li>scikit-learn
 *   <li>pandas
 * </ul>
 *
 * @since 2017.04.27
 */
public class SVMEstimator extends SupervisedDetectionEstimator {

  private static final String SCRIPT_PATH =
      ResourceUtils.getResource("scripts/svm_detect.py").toAbsolutePath().toString();
  private static final String DEAFULT_MODEL_PATH = "temp/model/svm.detect.model";

  private String pythonPath;
  private String modelPath;

  /**
   * TODO: detect Python path automatically.
   *
   * @param pythonPath the pathname to the Python interpreter.
   * @param modelPath the pathname to the pretrained model.
   * @param features
   */
  public SVMEstimator(String pythonPath, String modelPath, Detectable...features) {
    super(features);
    this.pythonPath = pythonPath;
    this.modelPath = modelPath;
  }

  public SVMEstimator(String pythonPath, Detectable...features) {
    this(pythonPath, DEAFULT_MODEL_PATH, features);
  }

  @Override
  public void train(float[][] scores, boolean[] labels) {
    try {
      // Write TSV into a temp file.
      File temp = Files.createTempFile("train-detect-", "").toFile();
      try (BufferedWriter bw = new BufferedWriter(new FileWriter(temp))) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < scores.length; i++) {
          for (float s: scores[i]) {
            sb.append(s).append('\t');
          }
          sb.append(labels[i] ? '1' : '0').append('\n');
        }
        bw.write(sb.toString());
      }
      // Run process.
      Files.createDirectories(Paths.get(modelPath).getParent());
      Process p = Runtime.getRuntime().exec(new String[]{
          pythonPath, SCRIPT_PATH, "train", temp.getCanonicalPath(), modelPath
        });
      p.waitFor();
      // Remove the temp file.
      temp.delete();
    } catch (IOException | InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public boolean[] predict(float[][] scores) {
    try {
      // Write TSV into a temp file.
      File temp = Files.createTempFile(Paths.get("temp"), "predict-detect-", "").toFile();
      try (BufferedWriter bw = new BufferedWriter(new FileWriter(temp))) {
        StringBuilder sb = new StringBuilder();
        for (float[] score: scores) {
          for (float s: score) {
            sb.append(s).append('\t');
          }
          sb.deleteCharAt(sb.length() - 1).append('\n');
        }
        bw.write(sb.toString());
      }
      // Run process.
      Process p = Runtime.getRuntime().exec(new String[]{
          pythonPath, SCRIPT_PATH, "predict", temp.getCanonicalPath(), modelPath
        });
      boolean[] labels = new boolean[scores.length];
      try (BufferedReader stdout = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
        StringBuilder sb = new StringBuilder();
        for (String line = stdout.readLine(); line != null; line = stdout.readLine()) {
          sb.append(line);
        }
        String[] fields = sb.toString().split("\t");
        for (int i = 0; i < fields.length; i++) {
          labels[i] = fields[i].equals("1");
        }
      }
      // Remove the temp file.
      temp.delete();
      return labels;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
