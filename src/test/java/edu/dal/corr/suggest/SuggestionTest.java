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
import java.util.stream.Stream;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import edu.dal.corr.suggest.feature.ContextCoherenceFeature;
import edu.dal.corr.suggest.feature.Feature;
import edu.dal.corr.suggest.feature.LanguagePopularityFeature;
import edu.dal.corr.suggest.feature.LevenshteinDistanceFeature;
import edu.dal.corr.suggest.feature.ApproximateContextCoherenceFeature;
import edu.dal.corr.util.IOUtils;
import edu.dal.corr.util.PathUtils;
import edu.dal.corr.util.ResourceUtils;
import edu.dal.corr.util.Unigram;
import edu.dal.corr.word.GoogleTokenizer;
import edu.dal.corr.word.Word;

public class SuggestionTest {
  
  private static final Path UNIGRAM_PATH =
      ResourceUtils.getResource("test.suggest/1gm/vocab.test.txt");
  private static final List<Path> BIGRAM_PATH = Arrays.asList(
      ResourceUtils.getResource("test.suggest/2gms/2gms.0001.test.txt"),
      ResourceUtils.getResource("test.suggest/2gms/2gms.0002.test.txt"));
  private static final List<Path> TRIGRAM_PATH = Arrays.asList(
      ResourceUtils.getResource("test.suggest/3gms/3gms.0001.test.txt"));
  private static final Path TXT_PATH =
      ResourceUtils.getResource("test.suggest/input.txt");

  public static Suggestion top3;
  public static Suggestion top10;
  public static Suggestion top100;
  public static Suggestion all;

  @Rule
  public TemporaryFolder tempFolder = new TemporaryFolder();
  
  @BeforeClass
  public static void beforeClass()
    throws Exception
  {
    Logger.getRootLogger().setLevel(Level.OFF);

    Unigram unigram = new Unigram(UNIGRAM_PATH);
    Unigram.register(unigram);

    // Generate suggestions.
    List<Suggestion> suggests = Suggestion.suggest(
        Word.get(IOUtils.read(TXT_PATH), new GoogleTokenizer()),
        Arrays.asList(new Feature[] {
            new LevenshteinDistanceFeature(),
            new LanguagePopularityFeature("GoogleWebUnigram",
                Unigram.getInstance()),
            new ContextCoherenceFeature("GoogleWebBigram",
                new NgramBoundedReader(BIGRAM_PATH),  2),
            new ContextCoherenceFeature("GoogleWebTrigram",
                new NgramBoundedReader(TRIGRAM_PATH),  3),
            new ApproximateContextCoherenceFeature("GoogleWebTrigram",
                new NgramBoundedReader(TRIGRAM_PATH),  3),
        })
    );

    // Get suggestions for 'word'.
    all = null;
    for (Suggestion sug: suggests) {
      if (sug.text().equals("word")) {
        all = sug;
        break;
      }
    }
    top3   = Suggestion.top(all, 3);
    top10  = Suggestion.top(all, 10);
    top100 = Suggestion.top(all, 100);
  }

  @Test
  public void testEquals()
  {
    for (Suggestion sug: Arrays.asList(top3, top10, top100)) {
      Suggestion copy = new Suggestion(
          sug.text(), sug.position(), sug.features(), sug.candidates());
      assertThat(copy, is(sug));
      assertThat(copy.hashCode(), is(sug.hashCode()));
    }
  }
  
  private float[] array(float... values) { return values; }

  @Test
  public void testSuggest()
    throws IOException
  {
    System.out.println(all.candidates().length);
    Stream.of(all.candidates()).forEach(c -> {
      System.out.println(String.format("%20s %s",
          c.text(), Arrays.toString(c.score())));
    });

    for (Candidate cand: all.candidates()) {
      float[] scores = cand.score();
      System.out.println(cand.text());
      switch (cand.text()) {
        case "word":    assertThat(scores, is(array(1.0f, 0.01f, 0.1f, 1.0f, 0.2f))); break;
        case "worde1":  assertThat(scores, is(array(1/3f, 0.01f, 0.0f, 0.0f, 0.0f))); break;
        case "worde2":  assertThat(scores, is(array(1/3f, 0.01f, 0.0f, 0.0f, 0.0f))); break;
        case "worde3":  assertThat(scores, is(array(1/3f, 0.01f, 0.0f, 0.0f, 0.0f))); break;
        case "worde4":  assertThat(scores, is(array(1/3f, 0.01f, 0.0f, 0.0f, 0.0f))); break;
        case "worde5":  assertThat(scores, is(array(1/3f, 0.01f, 0.0f, 0.0f, 0.0f))); break;
        case "wordf1k": assertThat(scores, is(array(0.0f, 0.10f, 0.0f, 0.0f, 0.0f))); break;
        case "wordf2k": assertThat(scores, is(array(0.0f, 0.20f, 0.0f, 0.0f, 0.0f))); break;
        case "wordf3k": assertThat(scores, is(array(0.0f, 0.30f, 0.0f, 0.0f, 0.0f))); break;
        case "wordf4k": assertThat(scores, is(array(0.0f, 0.40f, 0.0f, 0.0f, 0.0f))); break;
        case "wordf5k": assertThat(scores, is(array(0.0f, 0.50f, 0.0f, 0.0f, 0.0f))); break;
        case "wordf6k": assertThat(scores, is(array(0.0f, 0.60f, 0.0f, 0.0f, 0.0f))); break;
        case "wordf7k": assertThat(scores, is(array(0.0f, 0.70f, 0.0f, 0.0f, 0.0f))); break;
        case "wordf8k": assertThat(scores, is(array(0.0f, 0.80f, 0.0f, 0.0f, 0.0f))); break;
        case "wordf9k": assertThat(scores, is(array(0.0f, 0.90f, 0.0f, 0.0f, 0.0f))); break;
        case "wordf+k": assertThat(scores, is(array(0.0f, 1.00f, 0.0f, 0.0f, 0.0f))); break;
        case "word2e1": assertThat(scores, is(array(0.0f, 0.01f, 0.2f, 0.0f, 0.0f))); break;
        case "word2e2": assertThat(scores, is(array(0.0f, 0.02f, 0.4f, 0.0f, 0.0f))); break;
        case "word2e3": assertThat(scores, is(array(0.0f, 0.03f, 0.6f, 0.0f, 0.0f))); break;
        case "word2e4": assertThat(scores, is(array(0.0f, 0.04f, 0.8f, 0.0f, 0.0f))); break;
        case "word2e5": assertThat(scores, is(array(0.0f, 0.05f, 1.0f, 0.0f, 0.0f))); break;
        case "word2e6": assertThat(scores, is(array(0.0f, 0.06f, 0.0f, 0.0f, 0.0f))); break;
        case "word2e7": assertThat(scores, is(array(0.0f, 0.07f, 0.0f, 0.0f, 0.0f))); break;
        case "word2e8": assertThat(scores, is(array(0.0f, 0.08f, 0.0f, 0.0f, 0.0f))); break;
        case "word3e1": assertThat(scores, is(array(0.0f, 0.01f, 0.0f, 0.1f, 0.02f))); break;
        case "word3e2": assertThat(scores, is(array(0.0f, 0.02f, 0.0f, 0.2f, 0.04f))); break;
        case "word3e3": assertThat(scores, is(array(0.0f, 0.03f, 0.0f, 0.3f, 0.06f))); break;
        case "word3e4": assertThat(scores, is(array(0.0f, 0.04f, 0.0f, 0.4f, 0.08f))); break;
        case "word3r1": assertThat(scores, is(array(0.0f, 0.01f, 0.0f, 0.0f, 0.2f))); break;
        case "word3r2": assertThat(scores, is(array(0.0f, 0.02f, 0.0f, 0.0f, 0.4f))); break;
        case "word3r3": assertThat(scores, is(array(0.0f, 0.03f, 0.0f, 0.0f, 0.6f))); break;
        case "word3r4": assertThat(scores, is(array(0.0f, 0.04f, 0.0f, 0.0f, 0.8f))); break;
        case "word3r5": assertThat(scores, is(array(0.0f, 0.00f, 0.0f, 0.0f, 1.0f))); break;
        default:        assertThat(scores, is(array(0.0f, 0.00f, 0.0f, 0.0f, 0.0f))); break;
      }
    }
  }

  @Test
  public void testReadAndWrite()
    throws IOException
  {
    for (Suggestion sug: Arrays.asList(top3, top10, top100)) {
      Path file = tempFolder.newFile().toPath();
      Suggestion.write(sug, file);
      assertEquals(Suggestion.read(file), sug);
    }
  }

  @Test
  public void testWriteToFolder()
    throws IOException
  {
    List<Suggestion> data = Arrays.asList(top3, top10, top100);
    Path folder = tempFolder.newFolder().toPath();
    String prefix = "suggest";
    Suggestion.write(data, folder, prefix);
    List<Path> files = PathUtils.listPaths(folder, prefix + "*");
    for (int i = 0; i < files.size(); i++) {
      assertEquals(Suggestion.read(files.get(i)), data.get(i));
    }
  }

  @Test
  public void testReadAndWriteToSingleFile()
    throws IOException
  {
    List<Suggestion> data = Arrays.asList(top3, top10, top100);
    Path file = tempFolder.newFile().toPath();
    Suggestion.write(data, file);
    assertEquals(data, Suggestion.readList(file));
  }

  /*
   * Test the correctness of rewriting serialized suggestion data.
   */
  private void testRewriteImpl(Suggestion from, Suggestion to, int rewriteSize)
  {
    HashSet<Candidate> topCandidates = new HashSet<Candidate>(
        Arrays.asList(to.candidates()));

    // Check the existence of top candidates in the rewritten candidates.
    String errorWord = from.text();
    List<Feature> features = from.features();
    HashSet<Candidate> selected = new HashSet<Candidate>();
    for (int i = 0; i < features.size(); i++) {
      List<Candidate> sorted = Arrays.asList(from.candidates());
      sorted.sort(Suggestion.sortByScore(errorWord, features.get(i)));
      
      // Add all possible candidate selections, i.e., add candidate that equals
      // to the 3rd sorted candidate but ranks lower.
      int total = sorted.size();
      int limit = rewriteSize;
      while (limit <= total - 2
          && Suggestion.sortByScore(errorWord, features.get(i))
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
  
  /**
   * Test the correctness of {@code Suggestion#rewriteTop(Path, Path, int)}.
   */
  @Test
  public void testRewrite()
  {
    testRewriteImpl(top100, top10, 10);
    testRewriteImpl(top100, top3,  3);
    testRewriteImpl(top10,  top3,  3);
  }
  
  /*
   * Test the format of one error in a text buffer.
   */
  private void testSuggestFormatInText(BufferedReader br, Suggestion suggest)
    throws IOException
  {
    // Check error name.
    String suggestName = br.readLine();
    assertThat(suggestName, is(suggest.text()));

    // Check error corrections.
    float[][] scores = suggest.score(suggest.features());
    Candidate[] candidates = suggest.candidates();
    for (int i = 0; i < candidates.length; i++) {
      String[] fields = br.readLine().split("\t");
      String candName = fields[0];
      assertThat(candName, is(candidates[i].text()));
      int fidx = 1;
      for (float s : scores[i]) {
        assertThat(Float.parseFloat(fields[fidx++]), is(s));
      }
      assertThat(Integer.parseInt(fields[fidx]),
          is(candName.equals(suggestName) ? 1 : 0));
    }

    // Check the termination empty line.
    assertThat(br.readLine(), anyOf(is(""), is(nullValue())));
  }

  /**
   * Test {@link Suggestion#writeText(List, Path)} method.
   * 
   * @throws Exception
   */
  @Test
  public void testWriteText()
    throws Exception
  {
    Path out = PathUtils.getTempFile();
    Suggestion.writeText(Arrays.asList(top3, top10, top100), out);
    try (BufferedReader br = IOUtils.newBufferedReader(out)) {
      Iterator<Feature> featIter = top3.features().iterator();
      for (String line = br.readLine(); br != null; line = br.readLine()) {
        if (line.length() != 0) {

          // Check feature names.
          assertThat(featIter.hasNext(), is(true));
          assertThat(featIter.next().toString(), is(line));
        } else {

          // Check the empty line between feature names and errors.
          assertThat(featIter.hasNext(), is(false));

          // Check the format of the recorded three errors.
          testSuggestFormatInText(br, top3);
          testSuggestFormatInText(br, top10);
          testSuggestFormatInText(br, top100);
          break;
        }
      }
    }
    Files.delete(out);
  }
}
