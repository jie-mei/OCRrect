package edu.dal.ocrrect;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.BitSet;
import java.util.List;
import java.util.stream.Collectors;

import edu.dal.ocrrect.suggest.NgramBoundedReaderSearcher;
import edu.dal.ocrrect.util.IOUtils;
import edu.dal.ocrrect.util.PathUtils;
import edu.dal.ocrrect.util.ResourceUtils;
import edu.dal.ocrrect.word.GoogleTokenizer;
import edu.dal.ocrrect.word.Word;
import edu.dal.ocrrect.word.WordTokenizer;

public class Test {
  // word that position smaller than this is used for training
  private static final int SPLIT_POS = 407369;

  public static NgramBoundedReaderSearcher getNgramSearch(String pathname, List<Path> dataPath) {
    try {
      NgramBoundedReaderSearcher ngramSearch =
          NgramBoundedReaderSearcher.read(PathUtils.TEMP_DIR.resolve(Paths.get(pathname)));
      ngramSearch.setNgramPath(dataPath);
      return ngramSearch;
    } catch (IOException e) {
      throw new RuntimeException(
          String.format("Cannot open %s in building NgramBoundedReaderSearcher object.", pathname));
    }
  }

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

    // Read pre-processed 5-gram search indexing.
//    NgramBoundedReaderSearcher bigram = getNgramSearch("2gm.search", ResourceUtils.BIGRAM);
//    NgramBoundedReaderSearcher trigram = getNgramSearch("3gm.search", ResourceUtils.TRIGRAM);
//    NgramBoundedReaderSearcher fourgram = getNgramSearch("4gm.search", ResourceUtils.FOURGRAM);
//    NgramBoundedReaderSearcher fivegram = getNgramSearch("5gm.search", ResourceUtils.FIVEGRAM);

    // Read words from input text.
    List<Word> words = WordTokenizer.tokenize(
        IOUtils.read(ResourceUtils.getResourceInDir("*.txt", "mibio-ocr/ocr")),
        new GoogleTokenizer());
    int lastTrain = 0; // exclusive
    while (lastTrain < words.size() && words.get(lastTrain).position() < SPLIT_POS) lastTrain++;
    List<Word> trainWords = words.subList(0, lastTrain);
    List<Word> testWords = words.subList(lastTrain, words.size());

    List<Boolean> trainLabels = labelWords(trainWords);
    List<Boolean> testLabels = labelWords(testWords);

//    Detectable[] features = {
//      new WordValidityFeature(IOUtils.readList(ResourceUtils.VOCAB)),
//      new CharacterExistenceFeature((char)32),
//      new CharacterExistenceFeature((char)33),
//      new CharacterExistenceFeature((char)34),
//      new CharacterExistenceFeature((char)35),
//      new CharacterExistenceFeature((char)36),
//      new CharacterExistenceFeature((char)37),
//      new CharacterExistenceFeature((char)38),
//      new CharacterExistenceFeature((char)39),
//      new CharacterExistenceFeature((char)40),
//      new CharacterExistenceFeature((char)41),
//      new CharacterExistenceFeature((char)42),
//      new CharacterExistenceFeature((char)43),
//      new CharacterExistenceFeature((char)44),
//      new CharacterExistenceFeature((char)45),
//      new CharacterExistenceFeature((char)46),
//      new CharacterExistenceFeature((char)47),
//      new CharacterExistenceFeature((char)58),
//      new CharacterExistenceFeature((char)59),
//      new CharacterExistenceFeature((char)60),
//      new CharacterExistenceFeature((char)61),
//      new CharacterExistenceFeature((char)62),
//      new CharacterExistenceFeature((char)63),
//      new CharacterExistenceFeature((char)64),
//      new CharacterExistenceFeature((char)91),
//      new CharacterExistenceFeature((char)92),
//      new CharacterExistenceFeature((char)93),
//      new CharacterExistenceFeature((char)94),
//      new CharacterExistenceFeature((char)95),
//      new CharacterExistenceFeature((char)96),
//      new CharacterExistenceFeature((char)123),
//      new CharacterExistenceFeature((char)124),
//      new CharacterExistenceFeature((char)125),
//      new CharacterExistenceFeature((char)126),
//      new CharacterExistenceFeature((char)127),
//      new ContextCoherenceFeature("bigram", bigram, 2),
//      new ContextCoherenceFeature("trigram", trigram, 3),
//      new ContextCoherenceFeature("fourgram", fourgram, 4),
//      new ContextCoherenceFeature("fivegram", fivegram, 5),
//      new ApproximateContextCoherenceFeature("bigram", bigram, 2),
//      new ApproximateContextCoherenceFeature("trigram", trigram, 3),
//      new ApproximateContextCoherenceFeature("fourgram", fourgram, 4),
//      new ApproximateContextCoherenceFeature("fivegram", fivegram, 5),
//    };

    writeWords(trainWords, Paths.get("train-detect.words.txt"));
    writeWords(testWords, Paths.get("test-detect.words.txt"));
    writeLabels(testLabels, Paths.get("test-detect.label.gt.txt"));

//    SVMEstimator svm = new SVMEstimator("/home/default/miniconda3/bin/python", features);
//    svm.train(trainWords, trainLabels);
//
//    boolean[] predLabels = svm.predict(testWords);
//    float correct = 0;
//    for (int i = 0; i < predLabels.length; i++) {
//      if (testLabels.get(i) == predLabels[i]) {
//        correct++;
//      }
//    }
//    System.out.println(String.format("%d / %d = %.4f",
//        (int)correct, predLabels.length, correct / predLabels.length));
  }

  private static void writeWords(List<Word> words, Path path) throws IOException {
    try (BufferedWriter bw = Files.newBufferedWriter(path, StandardOpenOption.CREATE_NEW)) {
      bw.write(words
          .stream()
          .map(w -> w.text())
          .collect(Collectors.joining("\n")));
    }
  }

  private static void writeLabels(List<Boolean> labels, Path path) throws IOException {
    try (BufferedWriter bw = Files.newBufferedWriter(path, StandardOpenOption.CREATE_NEW)) {
      bw.write(labels
          .stream()
          .map(l -> l ? "1" : "0")
          .collect(Collectors.joining("\t")));
    }
  }
}