package edu.dal.corr;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.BitSet;
import java.util.List;
import java.util.stream.Collectors;

import edu.dal.corr.detect.SVMEstimator;
import edu.dal.corr.util.IOUtils;
import edu.dal.corr.util.ResourceUtils;
import edu.dal.corr.word.GoogleTokenizer;
import edu.dal.corr.word.Word;

public class Test {
  // word that position smaller than this is used for training
  private static final int SPLIT_POS = 407369;

  private static List<Boolean> labelWords(List<Word> ocrWords) throws IOException {
    BitSet errPos = new BitSet(SPLIT_POS);
    // Read ground truth errors.
    try (BufferedReader br = Files.newBufferedReader(
        ResourceUtils.getResource("mibio-ocr/error.gt.tsv"))) {
      for (String line = br.readLine(); line != null; line = br.readLine()) {
        String[] splits = line.split("\t");
        int strPos = Integer.parseInt(splits[0]);
        int endPos = strPos + splits[1].length();
        errPos.flip(strPos, endPos);
      }
    }
    return ocrWords
        .stream()
        .map(w -> {
          for (int i = w.position(); i < w.position() + w.text().length(); i++) {
            if (errPos.get(i)) { // overlap with the ground truth error
              return true;
            }
          }
          return false;
        })
        .collect(Collectors.toList());
  }

  public static void main(String[] args) throws IOException {
    // Read words from input text.
    List<Word> words = Word.tokenize(
        IOUtils.read(ResourceUtils.getResourceInDir("*.txt", "mibio-ocr/ocr")),
        new GoogleTokenizer());
    int lastTrain = 0; // exclusive
    while (lastTrain < words.size() && words.get(lastTrain).position() < SPLIT_POS) lastTrain++;
    List<Word> trainWords = words.subList(0, lastTrain);
    //List<Word> testWords = words.subList(lastTrain, words.size());
    
    List<Boolean> trainLabels = labelWords(trainWords);
    //List<Boolean> testLabels = labelWords(testWords);
    
    SVMEstimator svm = new SVMEstimator("/Users/Porzire/miniconda3/bin/python");
    svm.train(trainWords, trainLabels);
  }
}
