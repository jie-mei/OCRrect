package edu.dal.ocrrect.io;

import edu.dal.ocrrect.util.IOUtils;
import edu.dal.ocrrect.util.Token;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 */
public class IntegerTSVFile extends TSVFile<Integer> {

  public IntegerTSVFile(Path path) {
    super(path);
  }

  /**
   * Read elements from the TSV file.
   *
   * <p>Each line in the file records a token in the list, sorted by position. The token string and
   * its according position should be tab separated.
   *
   * @return a list of tokens.
   * @throws IOException if I/O error occurs.
   */
  @Override
  public List<Integer> read() throws IOException {
    List<Integer> vals = new ArrayList<>();
    try (BufferedReader br = Files.newBufferedReader(this.path())) {
      for (String line; (line = br.readLine()) != null;) {
        vals.add(Integer.parseInt(line));
      }
    }
    return vals;
  }

  @Override
  public void write(List<Integer> elements, OpenOption... options) throws IOException {
    try (BufferedWriter bw = IOUtils.newBufferedWriter(this.path(), options)){
      for (Integer e: elements) {
        bw.write(e.intValue() + "\n");
      }
    }
  }
}
