package edu.dal.ocrrect;

import edu.dal.ocrrect.suggest.Suggestion;
import edu.dal.ocrrect.suggest.feature.Feature;
import edu.dal.ocrrect.text.GoogleGramSegmenter;
import edu.dal.ocrrect.text.LineConcatTextProcessor;
import edu.dal.ocrrect.text.Text;
import edu.dal.ocrrect.util.Timer;
import edu.dal.ocrrect.util.Words;
import edu.dal.ocrrect.util.Word;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import edu.dal.ocrrect.util.lexicon.GoogleUnigramLexicon;
import org.apache.log4j.Logger;

/**
 * The {@code DocumentCorrector} class detect the natural language errors in document and provides a
 * list of candidates for each detected errors.
 *
 * @since 2017.04.20
 */
public class DocumentCorrector {
  public static final Logger LOG = Logger.getLogger(DocumentCorrector.class);

  public void correct(
      List<Feature> features,
      String content,
      int top)
      throws IOException {
    Timer t = new Timer();
    List<Word> words = Words.toWords(
      new Text(content)
        .process(new LineConcatTextProcessor(new GoogleUnigramLexicon()))
        .segment(new GoogleGramSegmenter()));
    if (LOG.isInfoEnabled()) {
      LOG.info(String.format(
          "Generate words\n" +
          "  - words:      %d\n" +
          "  - time taken: %4.2f seconds",
          words.size(),
          t.interval()));
    }

    List<List<Word>> wordSubLists = split(words, 1000);
    for (int i = 0; i < wordSubLists.size(); i++) {
      correctImpl(wordSubLists.get(i), features, top, String.format("part%03d", i));
    }
  }

  static <T> List<List<T>> split(List<T> list, final int L) {
    List<List<T>> parts = new ArrayList<List<T>>();
    final int N = list.size();
    for (int i = 0; i < N; i += L) {
      parts.add(new ArrayList<T>(list.subList(i, Math.min(N, i + L))));
    }
    return parts;
  }

  public List<Suggestion> correctImpl(
      List<Word> words, List<Feature> features, int top, String sign) throws IOException {
    String name = "suggestion" + (sign.length() == 0 ? "" :  "." + sign);

    Timer t = new Timer();
    t.start();
    List<Suggestion> suggestions = Suggestion.suggest(words, features, top);
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
    Suggestion.write(top100, Paths.get("tmp/" + name + ".top.100"), "suggest");
    if (LOG.isInfoEnabled()) {
      LOG.info(String.format(
          "Writing top 100 to file...\n" +
          "  - time taken:  %4.2f seconds",
          t.interval()));
    }

    Suggestion.rewriteTop(
        Paths.get("tmp/" + name + ".top.100"),
        Paths.get("tmp/" + name + ".top.50"), 50);

    Suggestion.rewriteTop(
        Paths.get("tmp/" + name + ".top.100"),
        Paths.get("tmp/" + name + ".top.20"), 20);

    Suggestion.rewriteTop(
        Paths.get("tmp/" + name + ".top.100"),
        Paths.get("tmp/" + name + ".top.10"), 10);

    Suggestion.rewriteTop(
        Paths.get("tmp/" + name + ".top.100"),
        Paths.get("tmp/" + name + ".top.5"), 5);

    Suggestion.rewriteTop(
        Paths.get("tmp/" + name + ".top.100"),
        Paths.get("tmp/" + name + ".top.3"), 3);

    return suggestions;
  }
}
