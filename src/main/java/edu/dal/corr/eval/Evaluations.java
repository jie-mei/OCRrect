package edu.dal.corr.eval;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import edu.dal.corr.util.LogUtils;
import edu.dal.corr.word.Word;

/**
 * This class defines the static procedures used for evaluation.
 *
 * @since 2017.01.18
 */
public class Evaluations
{
  private Evaluations() {}
  
  /**
   * Evaluate the error detection process. The evaluation result will be stored
   * in {@code log} directory with the following names:
   * <ul>
   *  <li> {@code eval.detect.<prefix>.bounded}
   *  <li> {@code eval.detect.<prefix>.unbounded}
   *  <li> {@code eval.detect.<prefix>.undetected}
   *  <li> {@code eval.detect.<prefix>.unmatched}
   *  <li> {@code eval.detect.<prefix>.result}
   * </ul>
   * where {@code prefix} is a string given from parameter identifying the
   * evaluation case.
   *
   * @param  prefix  the prefix used for naming the generated log files.
   * @param  errors  a list of ground truth errors.
   * @param  words   a list of error words, i.e. words will be sending to
   *                 further correction process.
   */
  public static void evalDetection(
    String prefix,
    List<GroundTruthError> errors,
    List<Word> words
  ){
    Map<GroundTruthError, Word> bounded = new HashMap<>();
    Map<GroundTruthError, List<Word>> unbounded = new HashMap<>();
    List<GroundTruthError> undetected = new ArrayList<>();
    Map<Word, GroundTruthError> wordMatch = new HashMap<>();
    words.forEach(s -> wordMatch.put(s, null));
    
    // An unbounded detection may let one word match to multiple errors.
    int overlapUnbound = 0;
    
    for (GroundTruthError error : errors) {
      int errStr = error.position();
      String errText = error.errorText();
      int errEnd = errStr + errText.length();
      boolean match = false;
      
      for (Word word : words) {
        int wStr = word.position();
        String wText = word.text();
        int wEnd = wStr + wText.length();
        
        // Bounded detect.
        if (wStr == errStr && wText.equals(errText)) {
          bounded.put(error, word);
          match = true;
          wordMatch.put(word, error);
        
        // Unbounded detect.
        } else if ((errStr <= wStr && errEnd > wStr)
            || (errStr < wEnd && errEnd >= wEnd)) {
          List<Word> wordList = null;
          if ((wordList = unbounded.get(error)) == null) {
            wordList = new ArrayList<>();
            unbounded.put(error, wordList);
          }
          wordList.add(word);
          match = true;
          if (wordMatch.get(word) != null) {
            overlapUnbound += 1;
          }
          wordMatch.put(word, error);
        }
      }
      
      if (! match) {
        undetected.add(error);
      }
    }
    
    String pnamePrefix = "eval.detect." + prefix + ".";
    
    LogUtils.logToFile(pnamePrefix + "bounded", false, (logger) -> {
      bounded.keySet()
        .stream()
        .sorted((a, b) -> a.position() - b.position())
        .forEachOrdered(err -> {
          Word w = bounded.get(err);
          logger.debug(String.format("%6d %-25s %-25s",
              err.position(), err.errorText(), w.text()));
        });
    });
    
    LogUtils.logToFile(pnamePrefix + "unbounded", false, (logger) -> {
      unbounded.keySet()
        .stream()
        .sorted((a, b) -> a.position() - b.position())
        .forEachOrdered(err -> {
          List<Word> wordList = unbounded.get(err);
          String str = wordList.stream()
            .map(Word::text)
            .collect(Collectors.joining(", "));
          logger.debug(String.format("%6d %-25s <%s>",
              err.position(), err.errorText(), str));
        });
    });
    
    LogUtils.logToFile(pnamePrefix + "undetected", false, (logger) -> {
      undetected.stream()
        .sorted((a, b) -> a.position() - b.position())
        .forEachOrdered(err -> {
          logger.debug(String.format("%6d %-25s %s",
              err.position(), err.errorText(), err.text()));
        });
    });
    
    LogUtils.logToFile(pnamePrefix + "unmatched", false, (logger) -> {
      wordMatch.keySet()
        .stream()
        .filter(w -> wordMatch.get(w) == null)
        .sorted((a, b) -> a.position() - b.position())
        .forEachOrdered(w -> {
          logger.debug(String.format("%6d %-25s",
              w.position(), w.text()));
        });
    });
    
    LogUtils.logToFile(pnamePrefix + "matchState", false, (logger) -> {
      words
        .stream()
        .sorted((a, b) -> a.position() - b.position())
        .forEach(w -> {
          String state = "";
          GroundTruthError err = wordMatch.get(w);
          if (bounded.containsValue(w)) {
            state = "BOUND";
          } else if (err != null) {
            state = "UNBOUND";
          } else {
            state = "UNMATCH";
          }
          logger.debug(String.format("%7d %-25s %-8s %s",
              w.position(), w.text(), state,
              err == null ? "" : err.errorText()));
        });
    });
    
    final int overlapUnboundSug = overlapUnbound;
    LogUtils.logToFile(pnamePrefix + "result", true, (logger) -> {
      logger.debug(String.format("Total:      %4d %4d",
          errors.size(),
          words.size()));
      logger.debug(String.format("Bound:      %4d %4d",
          bounded.keySet().size(),
          bounded.values().size()));
      logger.debug(String.format("Unbound:    %4d %4d",
          unbounded.keySet().size(),
          unbounded.values().stream()
            .flatMap(list -> list.stream())
            .count() - overlapUnboundSug));
      logger.debug(String.format("undetected: %4d",
          undetected.size()));
      logger.debug(String.format("unmatched:       %4d",
          wordMatch.keySet().stream()
          .filter(w -> wordMatch.get(w) == null).count()));
    });
  }
}
