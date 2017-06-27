package edu.dal.ocrrect.util.lexicon;

import edu.dal.ocrrect.util.IOUtils;
import gnu.trove.set.hash.THashSet;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

public class Lexicons {

  public static Lexicon toLexicon(THashSet<String> words) {
    return new StringSetLexicon(words);
  }

  public static Lexicon toLexicon(List<String> words) {
    return new StringSetLexicon(words);
  }

  public static Lexicon toLexicon(Path... paths) throws IOException {
    return toLexicon(IOUtils.readList(Arrays.asList(paths)));
  }

  public static Lexicon includeNumericWords(Lexicon lexicon) {
    return new DecoratedLexicon(lexicon) {
      @Override
      protected boolean decorateContains(String word) {
        for (char c: word.toCharArray()) {
          if (! Character.isDigit(c)) {
            return false;
          }
        }
        return true;
      }
    };
  }

  private abstract static class DecoratedLexicon implements Lexicon {

    private Lexicon lexicon;

    public DecoratedLexicon(Lexicon lexicon) {
      this.lexicon = lexicon;
    }

    @Override
    public boolean contains(String word) {
      return lexicon.contains(word) || decorateContains(word);
    }

    protected abstract boolean decorateContains(String word);
  }

  private static class MergedLexicon implements Lexicon {

    private List<Lexicon> lexicons;

    public MergedLexicon(Lexicon... lexicons) {
      this.lexicons = Arrays.asList(lexicons);
    }

    @Override
    public boolean contains(String word) {
      for (Lexicon lexicon: lexicons) {
        if (lexicon.contains(word)) {
          return true;
        }
      }
      return false;
    }
  }

  public static MergedLexicon merge(Lexicon... lexicons) {
    return new MergedLexicon(lexicons);
  }
}
