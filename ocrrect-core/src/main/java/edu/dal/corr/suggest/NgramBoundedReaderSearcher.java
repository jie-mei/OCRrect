package edu.dal.corr.suggest;

import gnu.trove.list.array.TLongArrayList;
import org.apache.commons.io.input.BoundedInputStream;

import java.io.*;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @since 2017.04.20
 */
public class NgramBoundedReaderSearcher implements Serializable {
  private static final long serialVersionUID = -2835908801171656852L;

  /**
   * The path of ngram corpus.
   */
  public String[] ngramPaths;

  /**
   * Offset before each file.
   */
  public long[] fileOffsets;

  /**
   * The mapping from words to their according offset. The offset indicate the
   * starting position for reading ngram records in files.
   */
  public HashMap<String, CorpusSubset> offsetMap;

  /**
   * Construct n-gram searcher.
   * <p>
   * Note that the construction process is time consuming for large corpus.
   *
   * @param ngrams  the path to ngram folder.
   * @throws IOException  if I/O error occurs.
   */
  public NgramBoundedReaderSearcher(List<Path> ngrams) throws IOException {
    ngramPaths = ngrams.stream()
        .map(p -> p.toAbsolutePath().toString())
        .collect(Collectors.toList())
        .toArray(new String[ngrams.size()]);

    offsetMap = new HashMap<>();
    TLongArrayList offsets = new TLongArrayList();
    offsets.add(0);
    long prevFileOffset = 0;
    for (Path p : ngrams) {
      try (RandomAccessFile raf = new RandomAccessFile(p.toFile(), "r")) {

        String line = null;
        String prevWord = null;
        long prevLineEnd = 0;
        long lastRecWordEnd = 0;
        while ((line = raf.readLine()) != null) {
          long lineEndPos = raf.getFilePointer();

          String currWord = extractFirstWord(line);

          // Check and record the new word.
          if (! currWord.equals(prevWord)) {
            if (prevWord != null) {
              addToOffsetMap(prevWord, prevFileOffset, prevLineEnd, lastRecWordEnd);
              lastRecWordEnd = prevLineEnd;
            }
          }
          prevLineEnd = lineEndPos;
          prevWord = currWord;
        }
        // Record the last word in file.
        addToOffsetMap(prevWord, prevFileOffset, prevLineEnd, lastRecWordEnd);

        // Record the size of the current reading file.
        prevFileOffset += raf.length();
        offsets.add(prevFileOffset);
      }
    }
    fileOffsets = offsets.toArray();
  }

  private void addToOffsetMap(String prevWord, long prevFileOffset, long prevLineEndPos, long
      lastRecWordEnd) {
    offsetMap.put(prevWord, new CorpusSubset(
        prevFileOffset + lastRecWordEnd,
        Math.toIntExact(prevLineEndPos - lastRecWordEnd)));
  }

  private String extractFirstWord(String line) {
    String word = "";
    for (int i = 0; i < line.length(); i++) {
      if (line.charAt(i) == ' ') {
        word = line.substring(0, i);
        break;
      }
    }
    if (word.length() == 0) {
      throw new RuntimeException();
    }
    return word;
  }

  /**
   * Get a {@link BufferedReader} feed with a subset of ngram corpus. The records in this subset
   * have the same first word as the given word.
   *
   * @param word A word
   * @return a bufferedReader which underlying stream contains all the records in the ngram corpus
   *     which first word is the same as the given word, or {@code null} if there is no such records
   *     in the corpus.
   * @throws IOException if I/O error occurs.
   */
  public BufferedReader openBufferedRecordsWithFirstWord(String word) throws IOException {
    CorpusSubset subset = offsetMap.get(word);
    if (subset == null) {
      return null;
    }

    // TODO: use binary search.
    int pIdx = 0;
    for (int i = 1; i <= ngramPaths.length; i++) {
      if (subset.offset < fileOffsets[i]) {
        pIdx = i - 1;
        break;
      }
    }
    FileInputStream fis = new FileInputStream(ngramPaths[pIdx]);
    fis.skip(subset.offset - fileOffsets[pIdx]);
    return new BufferedReader(new InputStreamReader(
        new BoundedInputStream(fis, subset.size)));
  }

  public class CorpusSubset implements Serializable {
    private static final long serialVersionUID = 5288864428787553815L;

    public final long offset;
    public final int size;

    private CorpusSubset(long offset, int size) {
      this.offset = offset;
      this.size = size;
    }
  }

  /**
   * Set pathnames to the n-gram data files.
   *
   * @param  pathnames  an array of pathnames.
   */
  public void setNgramPath(String... pathnames) {
    if (pathnames.length == ngramPaths.length) {
      ngramPaths = pathnames;
    } else {
      throw new IllegalArgumentException();
    }
  }

  /**
   * Set pathnames to the n-gram data files.
   *
   * @param  paths  an list of paths.
   */
  public void setNgramPath(List<Path> paths) {
    if (paths.size() == ngramPaths.length) {
      ngramPaths = paths
        .stream()
        .map(Path::toString)
        .collect(Collectors.toList())
        .toArray(new String[paths.size()]);
    } else {
      throw new IllegalArgumentException();
    }
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof NgramBoundedReaderSearcher) {
      NgramBoundedReaderSearcher another = (NgramBoundedReaderSearcher) obj;

      for (int i = 0; i < ngramPaths.length; i++) {
        if (! ngramPaths[i].equals((another.ngramPaths[i]))) {
          return false;
        }
      }
      for (int i = 0; i < fileOffsets.length; i++) {
        if (fileOffsets[i] != another.fileOffsets[i]) {
          return false;
        }
      }
      for (String word :offsetMap.keySet()) {
        CorpusSubset cs1 = offsetMap.get(word);
        CorpusSubset cs2 = another.offsetMap.get(word);
        if (cs1.offset != cs2.offset || cs1.size != cs2.size) {
          return false;
        }
      }
    }
    return true;
  }

  public static void write(NgramBoundedReaderSearcher searcher, Path out)
    throws IOException
  {
    Files.createDirectories(out.getParent());
    try (ObjectOutputStream oos = new ObjectOutputStream(Channels.newOutputStream(
        FileChannel.open(out, StandardOpenOption.CREATE, StandardOpenOption.WRITE)))) {
      oos.writeObject(searcher);
    }
  }

  /**
   * Construct the object from serialized file.
   *
   * @param path a serialized file.
   * @param ngramData ngram data pathnames. It is used to overwrite the ngram reading path in the
   *     constructed object.
   * @throws IOException if I/O error occurs.
   */
  public static NgramBoundedReaderSearcher read(Path path) throws IOException {
    try (ObjectInputStream ois = new ObjectInputStream(Channels.newInputStream(
        FileChannel.open(path)))) {
      NgramBoundedReaderSearcher searcher = (NgramBoundedReaderSearcher) ois.readObject();
      return searcher;
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }
}
