package edu.dal.corr.suggest;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.junit.Test;

import edu.dal.corr.util.ResourceUtils;

public class SuggestionTest {
  
  private static Suggestion top100 = Suggestions.read(
      ResourceUtils.getResource("suggest.top100.0000"));
  private static Suggestion top10 = Suggestions.read(
      ResourceUtils.getResource("suggest.top10.0000"));
  private static Suggestion top3 = Suggestions.read(
      ResourceUtils.getResource("suggest.top3.0000"));
  
  @Test
  public void testRewrite()
  {
    testRewriteImpl(top100, top10, 10);
    testRewriteImpl(top100, top3,  3);
    testRewriteImpl(top10,  top3,  3);
  }

  public void testRewriteImpl(
      Suggestion from,
      Suggestion to,
      int rewriteSize)
  {
    HashSet<Candidate> topCandidates = new HashSet<Candidate>(
        Arrays.asList(to.candidates()));

    // Check the existence of top candidates in the rewritten candidates.
    String errorWord = from.text();
    List<Class<? extends Feature>> types = from.types();
    HashSet<Candidate> selected = new HashSet<Candidate>();
    for (int i = 0; i < types.size(); i++) {
      List<Candidate> sorted = Arrays.asList(from.candidates());
      sorted.sort(Suggestions.sortByScore(errorWord, types.get(i)));
      
      // Add all possible candidate selections, i.e., add candidate that equals
      // to the 3rd sorted candidate but ranks lower.
      int total = sorted.size();
      int limit = rewriteSize;
      while (limit <= total - 2
          && Suggestions.sortByScore(errorWord, types.get(i))
              .compare(sorted.get(limit), sorted.get(limit + 1)) == 0) 
      {
        limit++;
      }
//      sorted.stream().limit(limit).forEach(selected::add);
      System.out.println(types.get(i).getSimpleName());
      int tidx = i;
      sorted.stream().limit(limit).forEach(cand -> {
        float[] s = cand.score();
        System.out.println(String.format("%12s (%d) %6f %6f %6f %6f %6f %6f %6f %6f",
            cand.text(), tidx, s[0], s[1], s[2], s[3], s[4], s[5], s[6], s[7]));
      });
      sorted.stream().limit(limit + 1).forEach(selected::add);
      selected.forEach(c -> System.out.print(c + " "));
      System.out.println();
    }


    for (Candidate top3Cand : topCandidates) {
      assertThat(selected.contains(top3Cand), is(true));
    }
  }
  
  @Test
  public void printTop() {
    print(top10);
    print(top3);
  }
  
  private void print(Suggestion suggest) {
    System.out.println(suggest.text());

    suggest.types()
      .stream()
      .map(t -> t.getSimpleName())
      .forEach(System.out::println);

    for (Candidate cand : suggest.candidates()) {
      float[] s = cand.score();
      System.out.println(String.format("%12s %6f %6f %6f %6f %6f %6f %6f %6f",
          cand.text(), s[0], s[1], s[2], s[3], s[4], s[5], s[6], s[7]));
    }
  }
}
