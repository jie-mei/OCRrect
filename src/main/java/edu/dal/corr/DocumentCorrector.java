package edu.dal.corr;

import java.nio.file.Paths;
import java.util.List;

import org.apache.log4j.Logger;

import edu.dal.corr.suggest.Feature;
import edu.dal.corr.suggest.Suggestion;
import edu.dal.corr.suggest.Suggestions;
import edu.dal.corr.util.Timer;
import edu.dal.corr.word.Tokenizer;
import edu.dal.corr.word.Word;
import edu.dal.corr.word.WordFilter;
import edu.dal.corr.word.Words;

/**
 * The {@code DocumentCorrector} class detect the natural language errors in
 * document and provides a list of candidates for each detected errors.
 * 
 * @since 2016.09.07
 */
public class DocumentCorrector
{
  private static final Logger LOG = Logger.getLogger(DocumentCorrector.class);

  public List<Suggestion> correct(Tokenizer tokenizer,
                                  List<WordFilter> filters,
                                  List<Feature> features,
                                  String content)
  {
    Timer t = new Timer();
    List<Word> words = Words.get(content, tokenizer,
        filters.toArray(new WordFilter[filters.size()]));
    if (LOG.isInfoEnabled()) {
      LOG.info(String.format(
          "Generate words\n" +
          "  - words:      %d\n" +
          "  - time taken: %4.2f seconds",
          words.size(),
          t.interval()));
    }
    
    t.start();
    List<Suggestion> suggestions = Suggestions.suggest(words, features);
    if (LOG.isInfoEnabled()) {
      LOG.info(String.format(
          "Suggesting candidates using features...\n" +
          "  - suggestions: %d\n" +
          "  - candidates:  %d\n" +
          "  - time taken:  %4.2f seconds",
          suggestions.size(),
          suggestions.stream().mapToInt(s -> s.candidates().length).sum(),
          t.interval()));
    }
    
    t.start();
    Suggestions.write(suggestions, Paths.get("tmp/suggestion"), "suggest");
    if (LOG.isInfoEnabled()) {
      LOG.info(String.format(
          "Writing to file...\n" +
          "  - time taken:  %4.2f seconds",
          t.interval()));
    }
    
    Suggestions.rewriteTop(
        Paths.get("tmp/suggestion"),
        Paths.get("tmp/suggestion.top.1000"), 1000);
    
    Suggestions.rewriteTop(
        Paths.get("tmp/suggestion"),
        Paths.get("tmp/suggestion.top.100"), 100);
    
    Suggestions.rewriteTop(
        Paths.get("tmp/suggestion"),
        Paths.get("tmp/suggestion.top.10"), 100);
    
    Suggestions.rewriteTop(
        Paths.get("tmp/suggestion"),
        Paths.get("tmp/suggestion.top.3"), 3);
    
    return suggestions;
  }
}
