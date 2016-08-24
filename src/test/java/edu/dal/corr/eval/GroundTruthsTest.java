package edu.dal.corr.eval;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import edu.dal.corr.util.IOUtils;
import edu.dal.corr.util.ResourceUtils;
import edu.dal.corr.util.StringUtils;
import gnu.trove.map.hash.TCharObjectHashMap;

public class GroundTruthsTest
{
  private static List<GroundTruthError> errors;
  private static String input;
  private static String gt;
  private static TCharObjectHashMap<String> unicodeMap;

  @BeforeClass
  public static void setUpBeforeClass()
    throws Exception
  {
    errors = GroundTruthErrors.read(ResourceUtils.GT_ERROR);
    input = StringUtils.fixLineBrokenWords(IOUtils.read(ResourceUtils.INPUT));
    gt = IOUtils.read(ResourceUtils.GT);
    unicodeMap = new TCharObjectHashMap<String>();
    unicodeMap.put((char) 8208, "-");
    unicodeMap.put((char) 8217, "'");
    unicodeMap.put((char) 8220, "\"");
    unicodeMap.put((char) 8221, "\"");
    unicodeMap.put((char) 64256, "ff");
    unicodeMap.put((char) 64257, "fi");
    unicodeMap.put((char) 64258, "fl");
    unicodeMap.put((char) 64259, "ffi");
    unicodeMap.put((char) 64260, "ffl");
  }

  @Test
  public void testMatchErrorText()
    throws Exception
  {
    errors.forEach(e -> {
      String textInInput = input.substring(e.position(),
          e.position() + e.errorText().length());
      System.out.println(e);
      System.out.println(input.substring(e.position() - 10,
          e.position() + e.errorText().length() + 10));
      assertEquals(e.errorText(), textInInput);
    });
  }

//  @Test
  // TODO
  public void testMatchGtText()
    throws Exception
  {
    // Replace the error texts in the input text.
    StringBuilder sb = new StringBuilder();
    int prevEnd = 0;
    for (GroundTruthError e : errors) {
      sb.append(input.substring(prevEnd, e.position()));
      sb.append(e.text());
      prevEnd = e.position() + e.errorText().length();
    }
    sb.append(input.substring(prevEnd, input.length()));
    String inText = sb.toString();
    
    // Replace the unicode in the ground truth text.
    String gtText = gt;
    for (char c : unicodeMap.keys()) {
      gtText = gtText.replaceAll(Character.toString(c), unicodeMap.get(c));
      inText = inText.replaceAll(Character.toString(c), unicodeMap.get(c));
    }
    
    // There may be extract whitespace character inserted in the input text
    // comparing with ground truth text.
    int offsetExp = 0;
    int offsetAct = 0;
    for (int i = 0; i < sb.length(); i++) {
      char expect = Character.toLowerCase(gtText.charAt(i + offsetExp));
      char actual = Character.toLowerCase(inText.charAt(i + offsetAct));
      while (expect != actual) {
        if (actual == ' ' || actual == '\n') {
          offsetAct += 1;
          actual = Character.toLowerCase(inText.charAt(i + offsetAct));
        } else if (expect == ' ' || expect == '\n') {
          offsetExp += 1;
          expect = Character.toLowerCase(gtText.charAt(i + offsetExp));
        } else {
          break;
        }
      }

      // Omit the punctuation mismatch.
      if (! Character.isJavaIdentifierPart(expect) &&
          ! Character.isJavaIdentifierPart(actual)) {
        continue;
      }

      int strIdx = i - 40 > 0 ? i - 40 : 0;
      int endIdx = i + 20;
      assertEquals(
          "\nexpect: (" + expect + ") " + gtText.substring(strIdx, endIdx) +
          "\nactual: (" + actual + ") " + inText.substring(strIdx + offsetAct, endIdx + offsetAct) +
          "\nposition: " + (i + offsetAct),
          expect, actual);
    }
  }
}
