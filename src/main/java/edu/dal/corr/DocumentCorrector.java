package edu.dal.corr;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

import org.apache.log4j.Logger;

import edu.dal.corr.suggest.Suggestion;
import edu.dal.corr.suggest.feature.Feature;
import edu.dal.corr.util.Timer;
import edu.dal.corr.word.Word;
import edu.dal.corr.word.WordTokenizer;
import edu.dal.corr.word.filter.WordFilter;

/**
 * The {@code DocumentCorrector} class detect the natural language errors in
 * document and provides a list of candidates for each detected errors.
 * 
 * @since 2017.02.15
 */
public class DocumentCorrector
{
  private static final Logger LOG = Logger.getLogger(DocumentCorrector.class);

  public List<Suggestion> correct(WordTokenizer tokenizer,
                                  List<WordFilter> filters,
                                  List<Feature> features,
                                  String content)
    throws IOException
  {
    Timer t = new Timer();
    List<Word> words = Word.get(content, tokenizer,
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
    List<Suggestion> suggestions = Suggestion.suggest(words, features);
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
    List<Suggestion> top100 = Suggestion.top(suggestions, 100);
    if (LOG.isInfoEnabled()) {
      LOG.info(String.format(
          "Get top 100...\n" +
          "  - time taken:  %4.2f seconds",
          t.interval()));
    }

    t.start();
    Suggestion.write(top100, Paths.get("tmp/suggestion.top.100"), "suggest");
    if (LOG.isInfoEnabled()) {
      LOG.info(String.format(
          "Writing top 100 to file...\n" +
          "  - time taken:  %4.2f seconds",
          t.interval()));
    }
    
    Suggestion.rewriteTop(
        Paths.get("tmp/suggestion.top.100"),
        Paths.get("tmp/suggestion.top.50"), 50);
    
    Suggestion.rewriteTop(
        Paths.get("tmp/suggestion.top.100"),
        Paths.get("tmp/suggestion.top.20"), 20);
    
    Suggestion.rewriteTop(
        Paths.get("tmp/suggestion.top.100"),
        Paths.get("tmp/suggestion.top.10"), 10);
    
    Suggestion.rewriteTop(
        Paths.get("tmp/suggestion.top.100"),
        Paths.get("tmp/suggestion.top.5"), 5);
    
    Suggestion.rewriteTop(
        Paths.get("tmp/suggestion.top.100"),
        Paths.get("tmp/suggestion.top.3"), 3);
    
    return suggestions;
  }
}
