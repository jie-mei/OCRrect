package edu.dal.ocrrect;

import edu.dal.ocrrect.io.TokenTSVFile;
import edu.dal.ocrrect.text.*;
import edu.dal.ocrrect.util.IOUtils;
import edu.dal.ocrrect.util.ResourceUtils;
import edu.dal.ocrrect.util.Token;
import edu.dal.ocrrect.util.lexicon.GoogleUnigramLexicon;
import edu.dal.ocrrect.util.lexicon.Lexicon;
import edu.dal.ocrrect.util.lexicon.Lexicons;
import gnu.trove.map.hash.TIntObjectHashMap;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class SegmentationExperiment {

  private static final String GT_OCR_SEGMENT_PATH = "mibio-ocr/ocr.token.tsv";
  private static final String GT_OCR_HYPHEN_SEGMENT_PATH = "mibio-ocr/ocr.token.hyphen.tsv";

  private static TextSegments readGTOCRSegments(String pathname) throws IOException {
    List<Segment> segments = new ArrayList<>();
    try (BufferedReader br = Files.newBufferedReader(ResourceUtils.getResource(pathname))) {
      for (String line = br.readLine(); line != null; line = br.readLine()) {
        String[] splits =line.split("\t");
        if (splits.length > 1) {
          segments.add(new Segment(splits[0], Integer.parseInt(splits[1])));
        }
      }
    }
    return new TextSegments(segments);
  }

  private static Pattern LINE_BROKEN = Pattern.compile("(.*)-\\s*↵(.*)");

  private static boolean matches(String segmented, List<String> gts) {
    boolean matches = false;
    for (String gt: gts) {
      if (gt.contains("↵")) {
        Matcher m = LINE_BROKEN.matcher(gt);
        m.matches();
        String concat = m.group(1) + m.group(2);
        matches = gt.equals(segmented) || concat.equals(segmented);
      } else {
        matches = gt.equals(segmented);
      }
      if (matches) {
        return true;
      }
    }
    return matches;
  }

  private static final int MAX_OFFSET = 4;

  private static List<String> getGT(TIntObjectHashMap<Segment> map, int position) {
    // Find ground truth text. It allows at most four whitespace offsets when looking-up. This
    // mechanism is used to deal with the position inconsistency generated in line concatenation.
    List<String> gts = new ArrayList<>();
    for (int offset = 0; offset < MAX_OFFSET; offset++) {
      if (map.containsKey(position + offset)) {
        gts.add(map.get(position + offset).text());
      }
    }
    return gts;
  }

  private static void eval(List<Segment> gt, String title, List<Segment> predicted, boolean detail)
      throws IOException {
    TIntObjectHashMap<Segment> gtPosMap = new TIntObjectHashMap<>();
    boolean[] correct = new boolean[predicted.size()];
    for (Segment s: gt) {
      gtPosMap.put(s.position(), s);
    }
    int same = 0;
    List<Segment> errors = new ArrayList<>();
    for (int i = 0; i < predicted.size(); i++) {
      Segment s = predicted.get(i);
      if (matches(s.text(), getGT(gtPosMap, s.position()))) {
        same += 1;
        correct[i] = true;
      } else {
        errors.add(s);
      }
    }

    Path temp = Paths.get("tmp");
    Files.createDirectories(temp);
    // Write segments into a TSV file.
    new TokenTSVFile(temp.resolve("token." + title + ".tsv")).write(
        predicted
            .stream()
            .map(s -> new Token(s.text(), s.position()))
            .collect(Collectors.toList()));
    // Write erroneous segments into a TSV file.
    new TokenTSVFile(temp.resolve("token." + title + ".error.tsv")).write(
        errors
            .stream()
            .map(s -> new Token(s.text(), s.position()))
            .collect(Collectors.toList()));

    // Print out result
    System.out.println(title);
    System.out.println();
    System.out.println("         segmentation accuracy: " + ((float) same) / predicted.size());
    System.out.println();
    System.out.println("         ground truth segments: " + gt.size());
    System.out.println("            predicted segments: " + predicted.size());
    System.out.println("  correctly predicted segments: " + same);
    System.out.println("erroneously predicted segments: " + (predicted.size() - same));
    System.out.println();
    if (! detail) return;
    System.out.println("erroneously predicted segments: ");
    for (int i = 0; i < predicted.size(); i++) {
      if (! correct[i]) {
        Segment err = predicted.get(i);
        System.out.printf("\t%30s %d\n", err.text(), err.position());
      }
    }
    System.out.println();
  }

  private static void eval(List<Segment> gt, String title, List<Segment> predicted)
      throws IOException {
    eval(gt, title, predicted, false);
  }

  public static void main(String[] args) throws Exception {
    List<Segment> gtSeg = readGTOCRSegments(GT_OCR_SEGMENT_PATH);

    Text text = new Text(IOUtils
        .read(ResourceUtils.getResourceInDir("*.txt", "mibio-ocr/ocr")));

    Lexicon vocab = Lexicons.includeNumericWords(
        Lexicons.toLexicon(ResourceUtils.getResource(
            "dictionary/12dicts-6.0.2/International/2of4brif.txt")));

    Lexicon vocabNoNum =
      Lexicons.toLexicon(ResourceUtils.getResource(
        "dictionary/12dicts-6.0.2/International/2of4brif.txt"));

//    Lexicon univocab = new GoogleUnigramLexicon();
//    Lexicon wordVocab = Lexicons.includeNumericWords(
//      Lexicons.toLexicon(ResourceUtils.getPath(
//        "/home/default/Downloads/words.txt")));
//    Lexicon merged = Lexicons.merge(vocab, wordVocab, univocab);
//    Lexicon vocab =
//        Lexicons.toLexicon(ResourceUtils.getResource(
//            "dictionary/12dicts-6.0.2/International/2of4brif.txt"));

//    eval(gtSeg, "no.whitespace.no",
//      text.segment(new WhiteSpaceSegmenter())
//    );
//    eval(gtSeg, "no.PennTreebank.no",
//      text.segment(new PennTreebankSegmenter())
//    );
//    eval(gtSeg, "no.Google.no",
//      text.segment(new GoogleGramSegmenter())
//    );
//    eval(gtSeg, "concat-dic.Google.no",
//      text.process(new TextLineConcatProcessor(vocab, false, true))
//          .segment(new GoogleGramSegmenter())
//    );
//    eval(gtSeg, "concat-dic.Google.non-concat-cs",
//      text.process(new TextLineConcatProcessor(vocab, false, true))
//          .segment(new GoogleGramSegmenter())
//          .process(new NonWordConcatProcessor(vocab, true))
//    );
//    eval(gtSeg, "concat-dic.Google.non-concat-ci",
//      text.process(new TextLineConcatProcessor(vocab, false, true))
//          .segment(new GoogleGramSegmenter())
//          .process(new CommonAbbrSegmentsProcessor())
//          .process(new NonWordConcatProcessor(vocab, false))
//    );
//    eval(gtSeg, "concat-dic.Google.concat",
//      text.process(new TextLineConcatProcessor(vocab, false, true))
//          .segment(new GoogleGramSegmenter())
//          .process(new CommonAbbrSegmentsProcessor())
//          .process(new TextSegmentsConcatProcessor())
//    );
//    eval(gtSeg, "concat-dic.split.frag-concat.common",
//      text.process(new TextLineConcatProcessor(vocab, false, true))
//        .segment(new SplitSegmenter())
//        .process(new FragmentConcatSegmentsProcessor())
//        .process(new CommonAbbrSegmentsProcessor())
//    );
//    eval(gtSeg, "concat-dic.Google.frag-concat",
//      text.process(new TextLineConcatProcessor(vocab, false))
//        .segment(new GoogleGramSegmenter(vocab))
//        .process(new FragmentConcatSegmentsProcessor(vocab, false, ""))
//        .process(new CommonAbbrSegmentsProcessor())
//    );

    eval(gtSeg, "concat-dic.Google.frag-concat-and",
      text.process(new TextLineConcatProcessor(vocabNoNum, false))
        .segment(new GoogleGramSegmenter(vocabNoNum))
        .process(new FragmentConcatSegmentsProcessor(vocabNoNum, false, ",.:;?!"))
        .process(new CommonAbbrSegmentsProcessor())
    );
    eval(gtSeg, "concat-dic.Google.frag-concat-and",
      text.process(new TextLineConcatProcessor(vocab, false))
        .segment(new GoogleGramSegmenter(vocabNoNum))
        .process(new FragmentConcatSegmentsProcessor(vocabNoNum, false, ",.:;?!"))
        .process(new CommonAbbrSegmentsProcessor())
    );
    eval(gtSeg, "concat-dic.Google.frag-concat-and",
      text.process(new TextLineConcatProcessor(vocab, false))
        .segment(new GoogleGramSegmenter(vocab))
        .process(new FragmentConcatSegmentsProcessor(vocabNoNum, false, ",.:;?!"))
        .process(new CommonAbbrSegmentsProcessor())
    );
    eval(gtSeg, "concat-dic.Google.frag-concat-and",
      text.process(new TextLineConcatProcessor(vocab, false))
        .segment(new GoogleGramSegmenter(vocab))
        .process(new FragmentConcatSegmentsProcessor(vocab, false, ",.:;?!"))
        .process(new CommonAbbrSegmentsProcessor())
    );
//    eval(gtSeg, "concat-dic.Google.frag-concat-uni",
//      text.process(new TextLineConcatProcessor(vocab, false))
//        .segment(new GoogleGramSegmenter(vocab))
//        .process(new FragmentConcatSegmentsProcessor(univocab, false, ",.:;?!"))
//        .process(new CommonAbbrSegmentsProcessor())
//    );
//    eval(gtSeg, "concat-dic.Google.frag-concat",
//      text.process(new TextLineConcatProcessor(vocab, false))
//        .segment(new GoogleGramSegmenter(vocab))
//        .process(new FragmentConcatSegmentsProcessor(vocab, false, ",.:;?!"))
//        .process(new CommonAbbrSegmentsProcessor())
//        .process(new NeighbourConcatSegmentsProcessor(univocab))
//    );
  }
}
