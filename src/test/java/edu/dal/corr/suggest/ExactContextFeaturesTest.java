package edu.dal.corr.suggest;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.junit.BeforeClass;
import org.junit.Test;

import edu.dal.corr.util.IOUtils;
import edu.dal.corr.util.ResourceUtils;
import edu.dal.corr.word.Context;
import gnu.trove.map.TObjectByteMap;
import gnu.trove.map.TObjectFloatMap;
import gnu.trove.map.hash.TObjectByteHashMap;

public class ExactContextFeaturesTest
{
  private static List<Path> ngrams;
  private static NgramBoundedReaderSearcher searcher;
  private static ExactContextFeature feature;

  @BeforeClass
  public static void setUpBeforeClass()
    throws Exception
  {
    ngrams = Arrays.asList(
        ResourceUtils.getResource("5gm-0000.seg"),
        ResourceUtils.getResource("5gm-0098.seg"));
    searcher = new NgramBoundedReaderSearcher(ngrams);
    feature = new ExactContextFeature(searcher);
  }

  @Test
  public void testDetect()
    throws Exception
  {
    Map<String, TObjectByteMap<Context>> firstContextMap = new HashMap<>();
    try (BufferedReader br = IOUtils.newBufferedReader(ngrams.get(0))) {
      for (String line = br.readLine(); line != null; line = br.readLine()) {
        String[] grams = line.split("\t")[0].split(" ");

        // Add a context with random word position.
        int pos = new Random().nextInt(grams.length);

        TObjectByteMap<Context> contexts = null;
        String first = grams[0];
        if ((contexts = firstContextMap.get(first)) == null) {
          contexts = new TObjectByteHashMap<>();
          firstContextMap.put(grams[0], contexts);
        }
        contexts.put(new Context(-1, pos, grams), (byte) 0);
      }
    }
    for (String first : firstContextMap.keySet()) {
      TObjectByteMap<Context> contexts = firstContextMap.get(first);
      TObjectByteMap<Context> results = feature.detect(first, contexts);
      results.keySet().forEach(c -> {
        assertEquals(results.get(c), (byte) 1);
      });
    }
  }

  @Test
  public void testSuggestSingleCase()
    throws Exception
  {
    String[] words = {"recent", "strip", "available", "is", "01/12/06."};
    Context context = new Context(-1, 4, words);
    TObjectFloatMap<String> candidateMap = feature.suggest(context);
    candidateMap.keySet().forEach(k -> {
      System.out.println(k + ":" + candidateMap.get(k));
    });
  }

  @Test
  public void testSuggestCase()
    throws Exception
  {
    String[] words = {"recent", "strip", "available", "is", "01/12/06."};
    Context context = new Context(-1, 4, words);
    List<TObjectFloatMap<String>> candidateMaps = feature.suggest(words[0], Arrays.asList(context));
    TObjectFloatMap<String> map = candidateMaps.get(0);
    List<String> candidates = Arrays.asList("01/17/06.", "01/16/06.", "01/14/06.", "01/13/06.", "01/12/06.");
    map.keySet().forEach(k -> {
      boolean contains = false;
      for (String c : candidates) {
        if (c.equals(k)) {
          contains = true;
        }
      }
      if (! contains) {
        fail();
      }
    });
  }
}
