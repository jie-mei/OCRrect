package edu.dal.ocrrect.expr.detect;

import edu.dal.corr.suggest.NgramBoundedReaderSearcher;
import edu.dal.ocrrect.suggest.NgramBoundedReaders;
import edu.dal.ocrrect.util.ResourceUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class ConvertNBRS {

  private static NgramBoundedReaderSearcher getNgramSearch(String pathname, List<Path> dataPath) {
    try {
      NgramBoundedReaderSearcher ngramSearch =
          NgramBoundedReaderSearcher.read(DATA_PATH.resolve(Paths.get(pathname)));
      ngramSearch.setNgramPath(dataPath);
      return ngramSearch;
    } catch (IOException e) {
      throw new RuntimeException(
        String.format("Cannot open %s in building NgramBoundedReaderSearcher object.", pathname));
    }
  }

  private static void convertAndWrite(NgramBoundedReaderSearcher searcher, String pathname) throws IOException {
    edu.dal.ocrrect.suggest.NgramBoundedReaderSearcher reader =
        new edu.dal.ocrrect.suggest.NgramBoundedReaderSearcher(searcher);
    NgramBoundedReaders.write(reader, Paths.get(pathname));
  }

  private static final Path DATA_PATH = Paths.get("data");

  public static void main(String[] args) throws IOException {
    convertAndWrite(getNgramSearch("2gm.search", ResourceUtils.BIGRAM),   "2gm.search.bin");
    convertAndWrite(getNgramSearch("3gm.search", ResourceUtils.TRIGRAM),  "3gm.search.bin");
    convertAndWrite(getNgramSearch("4gm.search", ResourceUtils.FOURGRAM), "4gm.search.bin");
    convertAndWrite(getNgramSearch("5gm.search", ResourceUtils.FIVEGRAM), "5gm.search.bin");
  }
}
