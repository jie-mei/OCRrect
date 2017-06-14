package edu.dal.ocrrect;

import edu.dal.ocrrect.io.TokenTSVFile;
import edu.dal.ocrrect.text.*;
import edu.dal.ocrrect.util.IOUtils;
import edu.dal.ocrrect.util.ResourceUtils;
import edu.dal.ocrrect.util.Token;
import edu.dal.ocrrect.util.lexicon.GoogleUnigramLexicon;
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

  public static final String GT_OCR_SEGMENT_PATH = "mibio-ocr/ocr.token.tsv";
  public static final String GT_OCR_HYPHEN_SEGMENT_PATH = "mibio-ocr/ocr.token.hyphen.tsv";

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

  private static TextSegments segmentText(Text text) throws IOException {
      return text
          .process(new TextLineConcatProcessor(new GoogleUnigramLexicon()))
          .segment(new GoogleGramSegmenter())
          .process(new TextSegmentsConcatProcessor());
  }

//  private static Pattern LINE_BROKEN = Pattern.compile("(.*)-\\s*↵(.*)");
  private static Pattern LINE_BROKEN = Pattern.compile("(.*)-\\s↵(.*)");

  private static boolean matches(String gt, String segmented) {
    if (gt.contains("↵")) {
      System.out.println(gt);
      Matcher m = LINE_BROKEN.matcher(gt);
      m.matches();
      String concat = m.group(1) + m.group(2);
//      System.out.println(gt + ":" + m.group(1) + m.group(2));
      return gt.equals(segmented) || gt.equals(concat);
    } else {
      return gt.equals(segmented);
    }
  }

  private static void eval(List<Segment> gt, String title, List<Segment> predicted, boolean detail)
      throws IOException {
    TIntObjectHashMap<Segment> gtPosMap = new TIntObjectHashMap<>();
    boolean[] correct = new boolean[predicted.size()];
    for (Segment s: gt) {
      gtPosMap.put(s.position(), s);
    }
    int same = 0;
    for (int i = 0; i < predicted.size(); i++) {
      Segment s = predicted.get(i);
      if (gtPosMap.contains(s.position()) &&
          matches(gtPosMap.get(s.position()).text(), s.text())) {
        same += 1;
        correct[i] = true;
      }
    }

    // Write segments into a TSV file.
    Path temp = Paths.get("tmp");
    Files.createDirectories(temp);
    new TokenTSVFile(temp.resolve(title + ".tsv")).write(
        predicted
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

    eval(gtSeg, "no.PennTreebank.no",
      text.segment(new PennTreebankSegmenter())
    );
    eval(gtSeg, "no.Google.no",
      text.segment(new GoogleGramSegmenter())
    );
    eval(gtSeg, "concat.Google.no",
      text.process(new TextLineConcatProcessor(new GoogleUnigramLexicon()))
          .segment(new GoogleGramSegmenter())
    );
    eval(gtSeg, "concat.Google.concat",
      text.process(new TextLineConcatProcessor(new GoogleUnigramLexicon()))
        .segment(new GoogleGramSegmenter())
        .process(new TextSegmentsConcatProcessor())
    );
  }
}
