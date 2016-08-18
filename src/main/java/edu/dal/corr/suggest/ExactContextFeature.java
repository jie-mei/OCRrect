package edu.dal.corr.suggest;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import edu.dal.corr.suggest.banchmark.ContextSensitiveBenchmarkDetectMixin;
import edu.dal.corr.suggest.banchmark.ContextSensitiveBenchmarkSuggestMixin;
import edu.dal.corr.word.Context;
import gnu.trove.map.TObjectByteMap;
import gnu.trove.map.TObjectFloatMap;
import gnu.trove.map.hash.TObjectByteHashMap;
import gnu.trove.map.hash.TObjectFloatHashMap;

/**
 * @since 2016.08.10
 */
public class ExactContextFeature
  extends AbstractFeature
  implements ContextSensitiveBenchmarkDetectMixin,
             ContextSensitiveBenchmarkSuggestMixin
{
  protected NgramBoundedReaderSearcher searcher;
  
  public ExactContextFeature(NgramBoundedReaderSearcher searcher)
  {
    super();
    this.searcher = searcher;
  }
  
  public ExactContextFeature(List<Path> ngrams)
    throws FileNotFoundException, IOException
  {
    this(new NgramBoundedReaderSearcher(ngrams));
  }

  @Override
  public TObjectByteMap<Context> detect(String first,
      TObjectByteMap<Context> contextMap)
  {
    TObjectByteHashMap<String> ngramMap = new TObjectByteHashMap<>();
    contextMap.keySet().forEach(context -> {
      ngramMap.put(context.toNgram(), (byte) 0);
    });

    // Check for the existence of n-grams in corpus.
    try (BufferedReader br = searcher.openBufferedRecordsWithFirstWord(first)) {
      if (br != null) {
        for (String line = br.readLine(); line != null; line = br.readLine()) {
          String[] splits = line.split("\t");
          String ngram = processDetectionString().apply(splits[0]);
          if (ngramMap.containsKey(ngram)) {
            ngramMap.adjustValue(ngram, (byte) 1);
          }
        }
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    
    // Update detection results to contextMap.
    TObjectByteMap<Context> resultMap = new TObjectByteHashMap<>();
    for (Context key : contextMap.keySet()) {
      byte result = ngramMap.get(key.toNgram());
      resultMap.put(key, result);
    }
    return resultMap;
  }

  @Override
  public List<TObjectFloatMap<String>> suggest(String first,
      List<Context> contexts)
  {
    // Initialize a hash map storing mappings from skipped ngram context to all
    // its candidates. The map is separated by the position of the skipped gram
    // in the context, in order to increase the searching speed.
    int maxNgramSize = contexts.stream().map(c -> c.words().length)
        .max(Comparator.naturalOrder()).get();
    List<HashMap<String, TObjectFloatMap<String>>> skipNgramMaps = new ArrayList<>();
    for (int i = 0; i < maxNgramSize; i++) {
      skipNgramMaps.add(new HashMap<>());
    }
    contexts.forEach(c -> {
      String[] words = c.words();
      int idx = c.index();
      skipNgramMaps.get(idx).putIfAbsent(
          skipNgram(words, idx),
          new TObjectFloatHashMap<>());
    });

    // Check for the existence of n-grams in corpus.
    try (BufferedReader br = searcher.openBufferedRecordsWithFirstWord(first)) {
      if (br != null) {
        for (String line = br.readLine(); line != null; line = br.readLine()) {
          String[] splits = line.split("\t");
          String[] grams = splits[0].split(" ");
          float freq = Float.parseFloat(splits[1]);
          
          // Post-process the reading grams.
          String[] procGrams = Arrays.stream(grams)
              .map(processDetectionString())
              .collect(Collectors.toList())
              .toArray(new String[grams.length]);

          // Check the existence of every possible skipped ngram in map.
          for (int i = 0; i < procGrams.length; i++) {
            HashMap<String, TObjectFloatMap<String>> map = skipNgramMaps.get(i);
            TObjectFloatMap<String> candidateMap = null;
            if ((candidateMap = map.get(skipNgram(procGrams, i))) != null) {
              // Return back the original string representation as the
              // candidate.
              candidateMap.adjustOrPutValue(grams[i], freq, freq);
            }
          }
        }
      } else {
        // Return a list of empty maps.
        return IntStream.range(0, contexts.size())
            .mapToObj(i -> new TObjectFloatHashMap<String>())
            .collect(Collectors.toList());
      }
    } catch (IOException e) {
      throw new RuntimeException();
    }

    return IntStream.range(0, contexts.size())
        .mapToObj(i -> {
          // Merge candidates from different skip-grams to construct the final
          // result.
          Context context = contexts.get(i);
          int pos = context.index();
          return skipNgramMaps.get(pos).get(skipNgram(context.words(), pos));
        })
        .collect(Collectors.toList());
  }
  
  /**
   * Generate a skip ngram.
   * 
   * @param  ngram     A list of {@code n} gram strings.
   * @param  position  The index of the skipped gram in ngram array.
   * @return A concatenated skip-ngram, which skipped gram is replaced by an
   *    space character.
   */
  private String skipNgram(String[] ngram, int position)
  {
    String[] copy = ngram.clone();
    copy[position] = " ";
    return String.join(" ", copy);
  }
}
