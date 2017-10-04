package edu.dal.ocrrect.expr.detect;

import edu.dal.ocrrect.detect.*;
import edu.dal.ocrrect.expr.Constants;
import edu.dal.ocrrect.expr.ExprUtils;
import edu.dal.ocrrect.io.FloatTSVFile;
import edu.dal.ocrrect.io.IntegerTSVFile;
import edu.dal.ocrrect.io.WordTSVFile;
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

  private static final Path OUTPUT_DIR = ExprUtils.TEMP_DIR.resolve("detect").resolve("data");

  /*
   * Compute feature values and write to the TSV file. The computational procedure will be skipped
   * if the TSV file already exists.
   */
  private static void computeAndWriteFeatureValues(List<Word> words, DetectionFeature feature)
      throws IOException {
    ExprUtils.ensureTempPath();
    String featName;
    {
      featName = feature.toString();
      featName = featName.substring(featName.lastIndexOf('.') + 1);
    }
    Path path;
    {
      String filename = String.join(".", featName, "tsv");
      path = OUTPUT_DIR.resolve(filename);
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
    FEATURED_CHARS.addAll(IntStream.range(58, 65).boxed().collect(Collectors.toList()));
    FEATURED_CHARS.addAll(IntStream.range(91, 97).boxed().collect(Collectors.toList()));
    FEATURED_CHARS.addAll(IntStream.range(123, 127).boxed().collect(Collectors.toList()));
  }

  private static void detectAndWrite() throws IOException {
    // List<Word> words = segmentText(OCR_TEXT_PATH, VOCAB_PATH);
    List<Word> words = new WordTSVFile(OUTPUT_DIR.resolve("words.tsv")).read();
    for (Integer val: FEATURED_CHARS) {
      computeAndWriteFeatureValues(words, new CharacterExistenceFeature((char) val.intValue()));
    }
    {
      Lexicon vocab = new GoogleUnigramLexicon();
      computeAndWriteFeatureValues(words, new WordValidityFeature(vocab));
    }
    /*
    {
      NgramBoundedReaderSearcher bigram = getNgramSearch("2gm.search.bin", ResourceUtils.BIGRAM);
      computeAndWriteFeatureValues(words, new ContextCoherenceFeature("bigram", bigram, 2));
      computeAndWriteFeatureValues(words, new ApproximateContextCoherenceFeature("bigram", bigram, 2));
    }
    {
      NgramBoundedReaderSearcher trigram = getNgramSearch("3gm.search.bin", ResourceUtils.TRIGRAM);
      computeAndWriteFeatureValues(words, new ContextCoherenceFeature("trigram", trigram, 3));
      computeAndWriteFeatureValues(words, new ApproximateContextCoherenceFeature("trigram", trigram, 3));
    }
    {
      NgramBoundedReaderSearcher fourgram = getNgramSearch("4gm.search.bin", ResourceUtils.FOURGRAM);
      computeAndWriteFeatureValues(words, new ContextCoherenceFeature("fourgram", fourgram, 4));
      computeAndWriteFeatureValues(words, new ApproximateContextCoherenceFeature("fourgram", fourgram, 4));
    }
    {
      NgramBoundedReaderSearcher fivegram = getNgramSearch("5gm.search.bin", ResourceUtils.FIVEGRAM);
      computeAndWriteFeatureValues(words, new ContextCoherenceFeature("fivegram", fivegram, 5));
      computeAndWriteFeatureValues(words, new ApproximateContextCoherenceFeature("fivegram", fivegram, 5));
    }
    */
  }

  private static void splitAndWriteMeta() throws IOException {
    // List<Word> words = segmentText(OCR_TEXT_PATH, VOCAB_PATH);
    List<Word> words = new WordTSVFile(OUTPUT_DIR.resolve("words.tsv")).read();

    List<Integer> labels = labelWords(words, GT_ERRORS_PATH)
        .stream().map(b -> b ? 1 : 0).collect(Collectors.toList());

    // Find the split index
    int splitIdx = 0;
    for (int i = 0; i < words.size(); i++) {
      if (words.get(i).position() > Constants.SPLIT_POS) {
        splitIdx  = i;
        break;
      }
    }

    // Write the words and labels for training and testing into different files.
    ExprUtils.ensureTempPath();
    new WordTSVFile(OUTPUT_DIR.resolve("words.train.tsv")).write(words.subList(0, splitIdx));
    new WordTSVFile(OUTPUT_DIR.resolve("words.test.tsv")).write(words.subList(splitIdx, words.size()));
    new IntegerTSVFile(OUTPUT_DIR.resolve("labels.train.tsv")).write(labels.subList(0, splitIdx));
    new IntegerTSVFile(OUTPUT_DIR.resolve("labels.test.tsv")).write(labels.subList(splitIdx, words.size()));
  }

  public static void main(String[] args) throws IOException {
    detectAndWrite();
    //splitAndWriteMeta();
  }
}
