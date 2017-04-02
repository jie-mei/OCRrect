package edu.dal.corr.suggest;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import edu.dal.corr.suggest.feature.ContextCoherenceFeature;
import edu.dal.corr.suggest.feature.DistanceFeature;
import edu.dal.corr.suggest.feature.Feature;
import edu.dal.corr.suggest.feature.FeatureType;
import edu.dal.corr.suggest.feature.LanguagePopularityFeature;
import edu.dal.corr.eval.GroundTruthError;
import edu.dal.corr.suggest.feature.ApproximateContextCoherenceFeature;
import edu.dal.corr.util.IOUtils;
import edu.dal.corr.util.PathUtils;
import edu.dal.corr.util.ResourceUtils;
import edu.dal.corr.util.Unigram;
import edu.dal.corr.word.GoogleTokenizer;
import edu.dal.corr.word.Word;

public class SuggestionTest {
  
  private static final Path VOCAB_PATH =
      ResourceUtils.getResource("test.suggest/vocab.test.txt");
  private static final Path UNIGRAM_PATH =
      ResourceUtils.getResource("test.suggest/1gm/1gm.test.txt");
  private static final List<Path> BIGRAM_PATH = Arrays.asList(
      ResourceUtils.getResource("test.suggest/2gms/2gms.0001.test.txt"),
      ResourceUtils.getResource("test.suggest/2gms/2gms.0002.test.txt"));
  private static final List<Path> TRIGRAM_PATH = Arrays.asList(
      ResourceUtils.getResource("test.suggest/3gms/3gms.0001.test.txt"));
  private static final Path TXT_PATH =
      ResourceUtils.getResource("test.suggest/input.txt");

  private static List<Feature> features;
  private static List<FeatureType> types;
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

//    Unigram unigram = new Unigram(UNIGRAM_PATH);
//    Unigram.register(unigram);
    ResourceUtils.VOCAB = VOCAB_PATH;
    ResourceUtils.UNIGRAM = UNIGRAM_PATH;
    
    features = Arrays.asList(new Feature[] {
            new DistanceFeature("Levenstein",
                Scoreable.levenshteinDist()),
            new LanguagePopularityFeature("GoogleWebUnigram",
                new Unigram(UNIGRAM_PATH)),
            new ContextCoherenceFeature("GoogleWebBigram",
                new NgramBoundedReaderSearcher(BIGRAM_PATH),  2),
            new ContextCoherenceFeature("GoogleWebTrigram",
                new NgramBoundedReaderSearcher(TRIGRAM_PATH),  3),
            new ApproximateContextCoherenceFeature("GoogleWebTrigram",
                new NgramBoundedReaderSearcher(TRIGRAM_PATH),  3),
        });
    types = features.stream().map(f -> f.type()).collect(Collectors.toList());

    // Generate suggestions.
    List<Suggestion> suggests = Suggestion.suggest(
        Word.get(IOUtils.read(TXT_PATH), new GoogleTokenizer()),
        features,
        99
    );
    suggests.forEach(suggest -> {
      for (Candidate cand : suggest.candidates()) {
        System.out.println(cand.text() + "\t" + Arrays.toString(cand.score(types)));
      }
    });

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
          sug.text(), sug.position(), sug.types(), sug.candidates());
      assertThat(copy, is(sug));
      assertThat(copy.hashCode(), is(sug.hashCode()));
    }
  }
  
  private float[] array(float... values) { return values; }

  private float tf(int max, int min, int freq) {
    // Log and rescaled.
    if (freq == 0) return 0;
    float reduce = (float)Math.log10(min + 1);
    float denorm = (float)Math.log10(max + 1) - reduce;
    return ((float)Math.log10(freq + 1) - reduce) / denorm;
  }

  @Test
  public void testSuggestTop() throws IOException {
    Set<String> candNameSet = Stream
        .of(top3.candidates())
        .map(Candidate::text)
        .collect(Collectors.toSet());
    System.out.println(candNameSet);

    assertThat(candNameSet.contains("word"), is(true));
    assertThat(candNameSet.contains("word1"), is(true));
    assertThat(candNameSet.contains("word2"), is(true));

    assertThat(candNameSet.contains("wordf+k"), is(true));
    assertThat(candNameSet.contains("wordf9k"), is(true));
    assertThat(candNameSet.contains("wordf8k"), is(true));

    assertThat(candNameSet.contains("word2e3"), is(true));
    assertThat(candNameSet.contains("word2e4"), is(true));
    assertThat(candNameSet.contains("word2e5"), is(true));

    // contains word
    assertThat(candNameSet.contains("word3e3"), is(true));
    assertThat(candNameSet.contains("word3e4"), is(true));

    assertThat(candNameSet.contains("word3r3+++"), is(true));  // not in edit distance
    assertThat(candNameSet.contains("word3r4"), is(true));
    assertThat(candNameSet.contains("word3r5"), is(true)); // not in vocab
  }

  @Test
  public void testSuggest() throws IOException {
    for (Candidate cand: all.candidates()) {
      float[] scores = cand.score(types);
      switch (cand.text()) {
        case "word":    assertThat(scores, is(array(1 - 0.0f,  tf(10000, 0, 100),   tf(500, 50, 50),  tf(100, 10, 100), tf(500, 10, 100)))); break;
        case "x":       assertThat(scores, is(array(1 - 2/3f,  tf(10000, 0, 100),   tf(500, 50, 100), tf(100, 10, 10),  tf(500, 10, 10)))); break;
        case "y":       assertThat(scores, is(array(1 - 2/3f,  tf(10000, 0, 100),   tf(500, 50, 150), tf(100, 10, 10),  tf(500, 10, 10)))); break;
        case "z":       assertThat(scores, is(array(1 - 2/3f,  tf(10000, 0, 100),   tf(500, 50, 250), tf(100, 10, 10),  tf(500, 10, 10)))); break;
        case "word1":   assertThat(scores, is(array(1 - 1/6f,  tf(10000, 0, 100),   tf(500, 50, 0),   tf(100, 10, 0),   tf(500, 10, 0)))); break;
        case "word2":   assertThat(scores, is(array(1 - 1/6f,  tf(10000, 0, 100),   tf(500, 50, 0),   tf(100, 10, 0),   tf(500, 10, 0)))); break;
        case "worde3":  assertThat(scores, is(array(1 - 1/3f,  tf(10000, 0, 100),   tf(500, 50, 0),   tf(100, 10, 0),   tf(500, 10, 0)))); break;
        case "worde4":  assertThat(scores, is(array(1 - 1/3f,  tf(10000, 0, 100),   tf(500, 50, 0),   tf(100, 10, 0),   tf(500, 10, 0)))); break;
        case "worde5":  assertThat(scores, is(array(1 - 1/3f,  tf(10000, 0, 100),   tf(500, 50, 0),   tf(100, 10, 0),   tf(500, 10, 0)))); break;
        case "wordf1k": assertThat(scores, is(array(1 - 0.5f, tf(10000, 0, 1000),  tf(500, 50, 0),   tf(100, 10, 0),   tf(500, 10, 0)))); break;
        case "wordf2k": assertThat(scores, is(array(1 - 0.5f, tf(10000, 0, 2000),  tf(500, 50, 0),   tf(100, 10, 0),   tf(500, 10, 0)))); break;
        case "wordf3k": assertThat(scores, is(array(1 - 0.5f, tf(10000, 0, 3000),  tf(500, 50, 0),   tf(100, 10, 0),   tf(500, 10, 0)))); break;
        case "wordf4k": assertThat(scores, is(array(1 - 0.5f, tf(10000, 0, 4000),  tf(500, 50, 0),   tf(100, 10, 0),   tf(500, 10, 0)))); break;
        case "wordf5k": assertThat(scores, is(array(1 - 0.5f, tf(10000, 0, 5000),  tf(500, 50, 0),   tf(100, 10, 0),   tf(500, 10, 0)))); break;
        case "wordf6k": assertThat(scores, is(array(1 - 0.5f, tf(10000, 0, 6000),  tf(500, 50, 0),   tf(100, 10, 0),   tf(500, 10, 0)))); break;
        case "wordf7k": assertThat(scores, is(array(1 - 0.5f, tf(10000, 0, 7000),  tf(500, 50, 0),   tf(100, 10, 0),   tf(500, 10, 0)))); break;
        case "wordf8k": assertThat(scores, is(array(1 - 0.5f, tf(10000, 0, 8000),  tf(500, 50, 0),   tf(100, 10, 0),   tf(500, 10, 0)))); break;
        case "wordf9k": assertThat(scores, is(array(1 - 0.5f, tf(10000, 0, 9000),  tf(500, 50, 0),   tf(100, 10, 0),   tf(500, 10, 0)))); break;
        case "wordf+k": assertThat(scores, is(array(1 - 0.5f, tf(10000, 0, 10000), tf(500, 50, 0),   tf(100, 10, 0),   tf(500, 10, 0)))); break;
        case "word2e1": assertThat(scores, is(array(1 - 0.5f, tf(10000, 0, 100),   tf(500, 50, 100), tf(100, 10, 0),   tf(500, 10, 0)))); break;
        case "word2e2": assertThat(scores, is(array(1 - 0.5f, tf(10000, 0, 200),   tf(500, 50, 200), tf(100, 10, 0),   tf(500, 10, 0)))); break;
        case "word2e3": assertThat(scores, is(array(1 - 0.5f, tf(10000, 0, 300),   tf(500, 50, 300), tf(100, 10, 0),   tf(500, 10, 0)))); break;
        case "word2e4": assertThat(scores, is(array(1 - 0.5f, tf(10000, 0, 400),   tf(500, 50, 400), tf(100, 10, 0),   tf(500, 10, 0)))); break;
        case "word2e5": assertThat(scores, is(array(1 - 0.5f, tf(10000, 0, 500),   tf(500, 50, 500), tf(100, 10, 0),   tf(500, 10, 0)))); break;
        case "word2e6": assertThat(scores, is(array(1 - 0.5f, tf(10000, 0, 600),   tf(500, 50, 0),   tf(100, 10, 0),   tf(500, 10, 0)))); break;
        case "word2e7": assertThat(scores, is(array(1 - 0.5f, tf(10000, 0, 700),   tf(500, 50, 0),   tf(100, 10, 0),   tf(500, 10, 0)))); break;
        case "word2e8": assertThat(scores, is(array(1 - 0.5f, tf(10000, 0, 800),   tf(500, 50, 0),   tf(100, 10, 0),   tf(500, 10, 0)))); break;
        case "word3e1": assertThat(scores, is(array(1 - 0.5f, tf(10000, 0, 100),   tf(500, 50, 0),   tf(100, 10, 10),  tf(500, 10, 10)))); break;
        case "word3e2": assertThat(scores, is(array(1 - 0.5f, tf(10000, 0, 200),   tf(500, 50, 0),   tf(100, 10, 20),  tf(500, 10, 20)))); break;
        case "word3e3": assertThat(scores, is(array(1 - 0.5f, tf(10000, 0, 300),   tf(500, 50, 0),   tf(100, 10, 30),  tf(500, 10, 30)))); break;
        case "word3e4": assertThat(scores, is(array(1 - 0.5f, tf(10000, 0, 400),   tf(500, 50, 0),   tf(100, 10, 40),  tf(500, 10, 40)))); break;
        case "word3r1":    assertThat(scores, is(array(1 - 0.5f, tf(10000, 0, 100),   tf(500, 50, 0),   tf(100, 10, 0),   tf(500, 10, 100)))); break;
        case "word3r2":    assertThat(scores, is(array(1 - 0.5f, tf(10000, 0, 200),   tf(500, 50, 0),   tf(100, 10, 0),   tf(500, 10, 200)))); break;
        case "word3r3+++": assertThat(scores, is(array(1 - 1f,   tf(10000, 0, 0),     tf(500, 50, 0),   tf(100, 10, 0),   tf(500, 10, 300)))); break;
        case "word3r4":    assertThat(scores, is(array(1 - 0.5f, tf(10000, 0, 400),   tf(500, 50, 0),   tf(100, 10, 0),   tf(500, 10, 400)))); break;
        case "word3r5":    assertThat(scores, is(array(1 - 0.5f, tf(10000, 0, 0),     tf(500, 50, 0),   tf(100, 10, 0),   tf(500, 10, 500)))); break;
        default:           assertThat(scores, is(array(1 - 1.0f,  0.00f,     0.0f, 0.0f, 0.0f))); break;
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
   * 
   */
  private int compareCandidates(String error, FeatureType type, Candidate c1, Candidate c2) {
    int diff = Suggestion.sortByScore(type).compare(c1, c2);
    if (diff == 0) {
      return Suggestion.sortByMetric(error).compare(c1, c2);
    }
    return diff;
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
    List<FeatureType> features = from.types();
    HashSet<Candidate> selected = new HashSet<Candidate>();
    for (int i = 0; i < features.size(); i++) {
      List<Candidate> sorted = Arrays.asList(from.candidates());
      sorted.sort(Suggestion.sortByMetric(errorWord));
      sorted.sort(Suggestion.sortByScore(features.get(i)));
      
      // Add all possible candidate selections, i.e., add candidate that equals
      // to the 3rd sorted candidate but ranks lower.
      int total = sorted.size();
      int limit = rewriteSize;
      while (limit <= total - 2
          && compareCandidates(errorWord, features.get(i), sorted.get(limit),
                sorted.get(limit + 1)) == 0)
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
    String suggestName = br.readLine().split("\t")[0];
    assertThat(suggestName, is(suggest.text()));

    // Check error corrections.
    float[][] scores = suggest.score(suggest.types());
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
    Suggestion.writeText(Arrays.asList(top3, top10, top100),
        new ArrayList<GroundTruthError>(), out);
    try (BufferedReader br = IOUtils.newBufferedReader(out)) {
      Iterator<FeatureType> typeIter = top3.types().iterator();
      for (String line = br.readLine(); br != null; line = br.readLine()) {
        if (line.length() != 0) {

          // Check feature names.
          assertThat(typeIter.hasNext(), is(true));
          assertThat(typeIter.next().toString(), is(line));
        } else {

          // Check the empty line between feature names and errors.
          assertThat(typeIter.hasNext(), is(false));

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
