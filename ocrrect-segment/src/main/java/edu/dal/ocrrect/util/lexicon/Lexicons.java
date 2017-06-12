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
}
