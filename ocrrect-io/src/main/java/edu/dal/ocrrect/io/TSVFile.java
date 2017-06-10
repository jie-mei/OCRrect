package edu.dal.ocrrect.io;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 */
abstract class TSVFile<T> {
  private Path path;

  TSVFile(Path path) {
    this.path = path;
  }

  protected Path path() {
    return path;
  }

  /**
   * Read elements from the TSV file.
   *
   * @return a list of tokens.
   * @throws IOException if I/O error occurs.
   */
  public abstract List<T> read() throws IOException;

  /**
   * Write elements to the TSV file.
   *
   * @param elements an element list.
   * @throws IOException
   */
  public abstract void write(List<T> elements) throws IOException;
}
