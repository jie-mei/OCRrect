package edu.dal.corr.suggest;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;

import edu.dal.corr.eval.GroundTruthErrors;
import edu.dal.corr.util.PathUtils;
import edu.dal.corr.util.ResourceUtils;
import edu.stanford.nlp.io.IOUtils;

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
      sorted.stream().limit(limit + 1).forEach(selected::add);
    }

    for (Candidate top3Cand : topCandidates) {
      assertThat(selected.contains(top3Cand), is(true));
    }
  }
  
  @Test
  public void testWriteText()
    throws Exception
  {
    Path out = PathUtils.getTempPath();
    Suggestions.writeText(
        Arrays.asList(top3, top10, top100),
        GroundTruthErrors.read(ResourceUtils.GT_ERROR),
        out);
    try (BufferedReader br = IOUtils.getBufferedFileReader(out.toString())) {
      Iterator<Class<? extends Feature>> typeIter = top3.types().iterator();
      for (String line = br.readLine(); br != null; line = br.readLine()) {
        if (line.length() != 0) {
          assertThat(typeIter.hasNext(), is(true));
          assertThat(typeIter.next().getName(), is(line));
        } else {
          assertThat(typeIter.hasNext(), is(false));
          checkSuggestInText(br, top3);
          checkSuggestInText(br, top10);
          checkSuggestInText(br, top100);
          break;
        }
      }
    }
    Files.delete(out);
  }
  
  public void checkSuggestInText(BufferedReader br, Suggestion suggest)
      throws IOException
  {
    assertThat(br.readLine(), is(suggest.text()));
    float[][] scores = suggest.score(suggest.types());
    Candidate[] candidates = suggest.candidates();
    for (int i = 0; i < candidates.length; i++) {
      String[] fields = br.readLine().split("\t");
      System.out.println(Arrays.toString(fields));
      assertThat(fields[0], is(candidates[i].text()));
      int fidx = 1;
      for (float s : scores[i]) {
        assertThat(Float.parseFloat(fields[fidx++]), is(s));
      }
      assertThat(Integer.parseInt(fields[fidx]), is(0));
    }
    assertThat(br.readLine(), anyOf(is(""), is(nullValue())));
  }
}
