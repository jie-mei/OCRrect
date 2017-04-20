package edu.dal.corr.suggest;

import edu.dal.corr.util.LogUtils;
import edu.dal.corr.util.Timer;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * Static NgramBoundedReader methods.
 *
 * @since 2017.04.20
 */
public class NgramBoundedReaders {
  private NgramBoundedReaders() {}

  public static void write(NgramBoundedReaderSearcher searcher, Path out) throws IOException {
    Timer t = new Timer();

    Files.createDirectories(out.getParent());
    try (ObjectOutputStream oos = new ObjectOutputStream(Channels.newOutputStream(
        FileChannel.open(out, StandardOpenOption.CREATE, StandardOpenOption.WRITE)))) {
      oos.writeObject(searcher);
    }
    LogUtils.logMethodTime(t, 2);
  }

  /**
   * Construct the object from serialized file.
   *
   * @param in A serialized file.
   * @param ngramData Ngram data pathnames. It is used to overwrite the ngram reading path in the
   *     constructed object.
   * @throws IOException If I/O error occurs.
   */
  public static NgramBoundedReaderSearcher read(Path in) throws IOException {
    Timer t = new Timer();

    try (ObjectInputStream ois = new ObjectInputStream(
        Channels.newInputStream(FileChannel.open(in)))) {
      NgramBoundedReaderSearcher searcher = (NgramBoundedReaderSearcher) ois.readObject();
      LogUtils.logMethodTime(t, 2);
      return searcher;
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }
}
