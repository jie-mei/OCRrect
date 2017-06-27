package edu.dal.ocrrect.expr.detect;

import edu.dal.ocrrect.detect.CharacterExistenceFeature;
import edu.dal.ocrrect.detect.Detectable;
import edu.dal.ocrrect.detect.DetectionFeature;
import edu.dal.ocrrect.detect.WordValidityFeature;
import edu.dal.ocrrect.expr.ExprUtils;
import edu.dal.ocrrect.io.FloatTSVFile;
import edu.dal.ocrrect.text.*;
import edu.dal.ocrrect.util.IOUtils;
import edu.dal.ocrrect.util.ResourceUtils;
import edu.dal.ocrrect.util.Word;
import edu.dal.ocrrect.util.lexicon.Lexicon;
import edu.dal.ocrrect.util.lexicon.Lexicons;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class DetectionExperiment {

  private static Logger LOG = Logger.getLogger(DetectionExperiment.class.toString());

  // word that position smaller than this is used for training
  private static final int SPLIT_POS = 407369;

  private static Detectable[] getFeatures(Lexicon unigram) {
    return new Detectable[]{
        new WordValidityFeature(unigram),
        new CharacterExistenceFeature((char) 32),
        new CharacterExistenceFeature((char) 33),
        new CharacterExistenceFeature((char) 34),
        new CharacterExistenceFeature((char) 35),
        new CharacterExistenceFeature((char) 36),
        new CharacterExistenceFeature((char) 37),
        new CharacterExistenceFeature((char) 38),
        new CharacterExistenceFeature((char) 39),
        new CharacterExistenceFeature((char) 40),
        new CharacterExistenceFeature((char) 41),
        new CharacterExistenceFeature((char) 42),
        new CharacterExistenceFeature((char) 43),
        new CharacterExistenceFeature((char) 44),
        new CharacterExistenceFeature((char) 45),
        new CharacterExistenceFeature((char) 46),
        new CharacterExistenceFeature((char) 47),
        new CharacterExistenceFeature((char) 58),
        new CharacterExistenceFeature((char) 59),
        new CharacterExistenceFeature((char) 60),
        new CharacterExistenceFeature((char) 61),
        new CharacterExistenceFeature((char) 62),
        new CharacterExistenceFeature((char) 63),
        new CharacterExistenceFeature((char) 64),
        new CharacterExistenceFeature((char) 91),
        new CharacterExistenceFeature((char) 92),
        new CharacterExistenceFeature((char) 93),
        new CharacterExistenceFeature((char) 94),
        new CharacterExistenceFeature((char) 95),
        new CharacterExistenceFeature((char) 96),
        new CharacterExistenceFeature((char) 123),
        new CharacterExistenceFeature((char) 124),
        new CharacterExistenceFeature((char) 125),
        new CharacterExistenceFeature((char) 126),
        new CharacterExistenceFeature((char) 127),
//      new ContextCoherenceFeature("bigram", bigram, 2),
//      new ContextCoherenceFeature("trigram", trigram, 3),
//      new ContextCoherenceFeature("fourgram", fourgram, 4),
//      new ContextCoherenceFeature("fivegram", fivegram, 5),
//      new ApproximateContextCoherenceFeature("bigram", bigram, 2),
//      new ApproximateContextCoherenceFeature("trigram", trigram, 3),
//      new ApproximateContextCoherenceFeature("fourgram", fourgram, 4),
//      new ApproximateContextCoherenceFeature("fivegram", fivegram, 5),
    };
  }

  private static final Path GT_ERRORS_PATH = ResourceUtils.getResource(
      "mibio-ocr/error.gt.tsv");
  private static final Path VOCAB_PATH = ResourceUtils.getResource(
      "dictionary/12dicts-6.0.2/International/2of4brif.txt");
  private static final List<Path> OCR_TEXT_PATH = ResourceUtils.getResourceInDir(
      "*.txt", "mibio-ocr/ocr");

  /*
   * Label OCR-generated words by their OCR correctness. An OCR error is the word that overlaps with
   * any ground-truth OCR error listed in the file.
   */
  private static List<Boolean> labelWords(List<Word> ocrWords, Path gtError) throws IOException {
    BitSet errPos;
    {
      Word last = ocrWords.get(ocrWords.size() - 1);
      errPos = new BitSet(last.position() + last.text().length());
      // Read the ground-truth errors.
      try (BufferedReader br = Files.newBufferedReader(gtError)) {
        for (String line = br.readLine(); line != null; line = br.readLine()) {
          String[] splits = line.split("\t");
          int strPos = Integer.parseInt(splits[0]);
          int endPos = strPos + splits[1].length();
          errPos.flip(strPos, endPos);
        }
      }
    }
    return ocrWords
        .stream()
        .map(w -> {
          for (int i = w.position(); i < w.position() + w.text().length(); i++) {
            if (errPos.get(i)) { // overlap with a ground-truth error
              return true;
            }
          }
          return false;
        })
        .collect(Collectors.toList());
  }

  private static final String PATHNAME_PREFIX = "detect";

  /*
   * Compute feature values and write to the TSV file. The computational procedure will be skipped
   * if the TSV file already exists.
   */
  private static void computeFeatureValues(List<Word> words, DetectionFeature feature) {
    ExprUtils.ensureTempPath();
    Path path;
    {
      String filename = String.join(".", PATHNAME_PREFIX, feature.toString());
      path = ExprUtils.TEMP_DIR.resolve(filename);
    }
    LOG.info(feature.toString());
    if (Files.exists(path)) {
      LOG.info("SKIP");
      return;
    }
    Path temp = path.getParent().resolve(path.getFileName().toString() + ".tmp");
    FloatTSVFile tsv = new FloatTSVFile(temp);
    List<Float> vals = Arrays.asList(feature.detect(words));
    tsv.write();
  }

  /*
   * Segment text
   */
  private static List<Word> segmentText(List<Path> ocrText, Path vocabulary) throws IOException {
    Text text = new Text(IOUtils.read(ocrText));
    Lexicon vocab = Lexicons.toLexicon(vocabulary);
    return text.process(new TextLineConcatProcessor(vocab, false))
        .segment(new GoogleGramSegmenter(vocab))
        .process(new FragmentConcatSegmentsProcessor(vocab, false, ",.:;?!"))
        .process(new CommonAbbrSegmentsProcessor())
        .toWords();
  }

  public static void main(String[] args) throws IOException {
    List<Word> words = segmentText(OCR_TEXT_PATH, VOCAB_PATH);
    List<Boolean> labels = labelWords(words, GT_ERRORS_PATH);
    for (int i = 0; i < words.size(); i++) {
      System.out.println(words.get(i).text() + "\t" + labels.get(i).toString());
    }
  }
}
