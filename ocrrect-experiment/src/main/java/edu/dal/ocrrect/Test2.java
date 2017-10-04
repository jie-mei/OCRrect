package edu.dal.ocrrect;

import edu.dal.ocrrect.eval.GroundTruthError;
import edu.dal.ocrrect.expr.suggest.SuggestConstants;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;
import edu.dal.ocrrect.suggest.Suggestion;
import edu.dal.ocrrect.suggest.Candidate;

public class Test2 {
  public static void main(String[] args) throws IOException {
    List<GroundTruthError> errors = GroundTruthError.read(SuggestConstants.ERROR_GT_PATH);
    Path path = Paths.get("tmp/suggest/data/suggest.test.top1.part01");
    List<Suggestion> suggests = Suggestion.readList(path);
    List<Suggestion> first3 = new ArrayList<>();
    System.out.println(suggests.get(0).types());
    System.out.println();
    for (int i = 0; i < 3; i++) {
      Suggestion suggest = suggests.get(i);
      System.out.println(suggest.text());
      for (Candidate cand: suggest.candidates()) {
        System.out.println(cand.text() + " " + Arrays.toString(cand.scores(suggest.types())));
        System.out.println();
      }
      first3.add(suggest);
      System.out.println();
    }
    Suggestion.writeText(first3, errors, Paths.get("tmp/test.out.txt"));
  }
}
