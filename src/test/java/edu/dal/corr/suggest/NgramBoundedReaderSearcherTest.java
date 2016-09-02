package edu.dal.corr.suggest;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import edu.dal.corr.util.PathUtils;
import edu.dal.corr.util.ResourceUtils;
import edu.stanford.nlp.io.IOUtils;

public class NgramBoundedReaderSearcherTest
{
  private static List<Path> ngrams;
  private static NgramBoundedReaderSearcher searcher;

  @BeforeClass
  public static void setUpBeforeClass()
    throws Exception
  {
    ngrams = Arrays.asList(
        ResourceUtils.getResource("5gm-0000.seg"),
        ResourceUtils.getResource("5gm-0098.seg"));
    searcher = new NgramBoundedReaderSearcher(ngrams);
  }

  @Test
  public void testOpenBufferedReader()
    throws Exception
  {
    try (BufferedReader br = IOUtils.getBufferedFileReader(
        ngrams.get(0).toString())) {
      String testWord = null;
      BufferedReader testReader = null;

      String line = null;
      while ((line = br.readLine()) != null) {
        String word = extractFirstWord(line);

        if (! word.equals(testWord)) {

          // Check whether finish reading everything from the buffer.
          if (testReader != null) {
            String testLine = null;
            if ((testLine = testReader.readLine()) != null) {
              StringBuilder sb = new StringBuilder(testLine);
              while ((testLine = testReader.readLine()) != null)  {
                sb.append('\n').append(testLine);
                fail("Buffer remains:\n" + sb.toString());
              }
            }
            testReader.close();
          }
          // Open a new buffer if necessary.
          testReader = searcher.openBufferedRecordsWithFirstWord(word);
          testWord = word;

        }

        // Check whether the reading line matches the line from buffer.
        String testLine = testReader.readLine();
        if (testLine == null) {
          fail("Buffer data incomplete.");
        }
        if (! testLine.equals(line)) {
          fail(String.format(
              "Buffer data unmatch. Buffer: \"%s\" expect: \"%s\"",
              testLine, line));
        } else {
        }
      }
    }
  }

  @Test
  public void testOpenBufferedReaderWithNonexistingWord()
    throws Exception
  {
    String word = "xyz"; // not exist as the first word of any records in input.
    assertEquals(searcher.openBufferedRecordsWithFirstWord(word), null);
  }
  
  private String extractFirstWord(String line)
  {
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
  
  @Test
  public void testReadWrite()
    throws Exception
  {
    Path tmpFile = Files.createTempFile(PathUtils.TEMP_DIR, "tmp", ".tmp");
    searcher.write(tmpFile);
    NgramBoundedReaderSearcher newSearcher = NgramBoundedReaderSearcher.read(tmpFile);
    assertEquals(searcher, newSearcher);
    Files.delete(tmpFile);
  }
}
