package edu.dal.ocrrect.expr.suggest;

import edu.dal.ocrrect.io.StringTSVFile;
import edu.dal.ocrrect.io.WordTSVFile;
import edu.dal.ocrrect.suggest.Candidate;
import edu.dal.ocrrect.suggest.Suggestion;
import edu.dal.ocrrect.suggest.feature.FeatureType;
import edu.dal.ocrrect.util.IOUtils;
import edu.dal.ocrrect.util.Word;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

public class ConvertSuggestionToTSV {
  /*
   * Format of the data TSV: error_str, error_pos, candidate_str, feature_val_1, ...
   */
  private static void writeSuggestToTSV(List<Suggestion> suggests, Path output) {
    try (BufferedWriter bw = IOUtils.newBufferedWriter(output,
        StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {

      { // Write the header line.
        StringBuffer sb = new StringBuffer();
        List<FeatureType> types = suggests.get(0).types();
        List<String> names = types.stream().map(t -> t.toString()).collect(Collectors.toList());
        sb.append("error").append('\t')
          .append("position").append('\t')
          .append("candidate").append('\t')
          .append(String.join("\t", names)).append('\n');
        bw.write(sb.toString());
      }
      { // Write the data
        List<FeatureType> types = suggests.get(0).types();
        for (int i = 0; i < suggests.size(); i++) {
          Suggestion suggest = suggests.get(i);
          StringBuilder sb = new StringBuilder();
          String error = suggest.text();
          int pos = suggest.position();
          for (Candidate cand: suggest.candidates()) {
            sb.append(error).append('\t')
              .append(pos).append('\t')
              .append(cand.text()).append('\t');
            for(float val: cand.scores(types)) {
              sb.append(val).append('\t');
            }
            sb.deleteCharAt(sb.length() - 1).append("\n");
            bw.write(sb.toString());
          }
        }
      }
    } catch (IOException e) {
      throw new RuntimeException(e); // expect no error
    }
  }

  private static void writeLabelToTSV(List<Suggestion> suggests, Path correction, Path output) {
    try (BufferedWriter bw = IOUtils.newBufferedWriter(output,
        StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
      bw.write("label\n");

      // Read the ground truth corrections.
      List<String> corrs = new StringTSVFile(correction).read();

      // Write the label
      for (int i = 0; i < suggests.size(); i++) {
        for (Candidate cand: suggests.get(i).candidates()) {
          bw.write((cand.text().equals(corrs.get(i)) ? 1 : 0) + "\n");
        }
      }
    } catch (IOException e) {
      throw new RuntimeException(e); // expect no error
    }
  }

  private static List<Suggestion> selectIdentical(List<Suggestion> mapped, List<Word> identical) {
    HashSet<Integer> posSet = new HashSet<>();
    identical.forEach(w -> posSet.add(w.position()));
    return mapped.stream()
      .filter(suggest -> posSet.contains(suggest.position()))
      .collect(Collectors.toList());
  }

  public static void main(String[] args) throws IOException {
    List<Suggestion> trainSuggests = Suggestion.readList(SuggestConstants.TRAIN_BINARY_TOP10_PATH);

    System.out.println("Train mapped");
    writeSuggestToTSV(trainSuggests,
        SuggestConstants.TRAIN_SUGGESTS_MAPPED_TOP10_TSV_PATH);
    writeLabelToTSV(trainSuggests,
        SuggestConstants.TRAIN_CORRS_MAPPED_TSV_PATH,
        SuggestConstants.TRAIN_LABELS_MAPPED_TOP10_TSV_PATH);

    System.out.println("Train mapped identical");
    List<Word> mappedIdentical =
        new WordTSVFile(SuggestConstants.TRAIN_WORDS_MAPPED_IDENTICAL_TSV_PATH).read();
    List<Suggestion> suggestsIdentical =
        selectIdentical(trainSuggests, mappedIdentical);
    writeSuggestToTSV(suggestsIdentical,
        SuggestConstants.TRAIN_SUGGESTS_MAPPED_IDENTICAL_TOP10_TSV_PATH);
    writeLabelToTSV(suggestsIdentical,
        SuggestConstants.TRAIN_CORRS_MAPPED_IDENTICAL_TSV_PATH,
        SuggestConstants.TRAIN_LABELS_MAPPED_IDENTICAL_TOP10_TSV_PATH);
  }
}
