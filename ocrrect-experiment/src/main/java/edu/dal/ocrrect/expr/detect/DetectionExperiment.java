package edu.dal.ocrrect.expr.detect;

import edu.dal.ocrrect.detect.*;
import edu.dal.ocrrect.expr.ExprUtils;
import edu.dal.ocrrect.io.FloatTSVFile;
import edu.dal.ocrrect.suggest.NgramBoundedReaderSearcher;
import edu.dal.ocrrect.text.*;
import edu.dal.ocrrect.util.IOUtils;
import edu.dal.ocrrect.util.ResourceUtils;
import edu.dal.ocrrect.util.Word;
import edu.dal.ocrrect.util.lexicon.GoogleUnigramLexicon;
import edu.dal.ocrrect.util.lexicon.Lexicon;
import edu.dal.ocrrect.util.lexicon.Lexicons;
import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class DetectionExperiment {

  private static Logger LOG = Logger.getLogger(DetectionExperiment.class.toString());

  // word that position smaller than this is used for training
  private static final int SPLIT_POS = 407369;

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
  private static void computeFeatureValues(List<Word> words, DetectionFeature feature)
      throws IOException {
    ExprUtils.ensureTempPath();
    String featName;
    {
      featName = feature.toString();
      featName = featName.substring(featName.lastIndexOf('.') + 1);
    }
    Path path;
    {
      String filename = String.join(".", PATHNAME_PREFIX, featName, "txt");
      path = ExprUtils.TEMP_DIR.resolve(filename);
    }
    if (Files.exists(path)) {
      LOG.info(feature.toString() + " SKIP");
      return;
    }
    Path temp = path.getParent().resolve(path.getFileName().toString() + ".tmp");
    FloatTSVFile tsv = new FloatTSVFile(temp);
    List<Float> vals = Arrays.asList(ArrayUtils.toObject(feature.detect(words)));
    tsv.write(vals);
    if (! temp.toFile().renameTo(path.toFile())) {
      throw new IOException("Unable to rename the temp file: " + temp.toString());
    }
    LOG.info(feature.toString() + " DONE");
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

  private static final Path DATA_PATH = Paths.get("data");

  private static NgramBoundedReaderSearcher getNgramSearch(String pathname, List<Path> dataPath) {
    try {
      NgramBoundedReaderSearcher ngramSearch =
          NgramBoundedReaderSearcher.read(DATA_PATH.resolve(Paths.get(pathname)));
      ngramSearch.setNgramPath(dataPath);
      return ngramSearch;
    } catch (IOException e) {
      throw new RuntimeException(
        String.format("Cannot open %s in building NgramBoundedReaderSearcher object.", pathname));
    }
  }


  private static final List<Integer> FEATURED_CHARS = new ArrayList<>();
  static {
    FEATURED_CHARS.addAll(IntStream.range(32, 48).boxed().collect(Collectors.toList()));
    FEATURED_CHARS.addAll(IntStream.range(59, 65).boxed().collect(Collectors.toList()));
    FEATURED_CHARS.addAll(IntStream.range(91, 97).boxed().collect(Collectors.toList()));
    FEATURED_CHARS.addAll(IntStream.range(123, 128).boxed().collect(Collectors.toList()));
  }

  private static void detect() throws IOException {
    List<Word> words = segmentText(OCR_TEXT_PATH, VOCAB_PATH);
    List<Boolean> labels = labelWords(words, GT_ERRORS_PATH);
    for (Integer val: FEATURED_CHARS) {
      computeFeatureValues(words, new CharacterExistenceFeature((char) val.intValue()));
    }
    {
      Lexicon vocab = new GoogleUnigramLexicon();
      computeFeatureValues(words, new WordValidityFeature(vocab));
    }
    {
      NgramBoundedReaderSearcher bigram = getNgramSearch("2gm.search.bin", ResourceUtils.BIGRAM);
      computeFeatureValues(words, new ContextCoherenceFeature("bigram", bigram, 2));
      computeFeatureValues(words, new ApproximateContextCoherenceFeature("bigram", bigram, 2));
    }
    {
      NgramBoundedReaderSearcher trigram = getNgramSearch("3gm.search.bin", ResourceUtils.TRIGRAM);
      computeFeatureValues(words, new ContextCoherenceFeature("trigram", trigram, 3));
      computeFeatureValues(words, new ApproximateContextCoherenceFeature("trigram", trigram, 3));
    }
    {
      NgramBoundedReaderSearcher fourgram = getNgramSearch("4gm.search.bin", ResourceUtils.FOURGRAM);
      computeFeatureValues(words, new ContextCoherenceFeature("fourgram", fourgram, 4));
      computeFeatureValues(words, new ApproximateContextCoherenceFeature("fourgram", fourgram, 4));
    }
    {
      NgramBoundedReaderSearcher fivegram = getNgramSearch("5gm.search.bin", ResourceUtils.FIVEGRAM);
      computeFeatureValues(words, new ContextCoherenceFeature("fivegram", fivegram, 5));
      computeFeatureValues(words, new ApproximateContextCoherenceFeature("fivegram", fivegram, 5));
    }
  }

  public static void main(String[] args) throws IOException {
    detect();
  }
}
