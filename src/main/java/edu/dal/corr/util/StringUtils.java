package edu.dal.corr.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils
{
  private StringUtils() {}
  
  private static Pattern BROKEN_WORD = Pattern.compile("([a-zA-Z]+)(-\\s*)$");
  private static Pattern FIRST_WORD = Pattern.compile("^([a-zA-Z]*)(\\S*)(.*)$");
  
  /**
   * Concatenate content lines and fix the broken word created by line wrap.
   * If fixing a word changes the text length, extra white space characters are
   * padded following the modified words to keep the position of other words
   * unchanged.
   * 
   * @param  content A string.
   * @return The input content without internal line-break and broken word
   *    fixed.
   */
  public static String fixLineBrokenWords(String str)
  {
    StringBuilder sb = new StringBuilder();
    try (BufferedReader br = new BufferedReader(new StringReader(str))) {
      Dictionary dict = GoogleUnigramDictionary.getInstance();

      String curr = null;
      String bkPart1 = null;
      int pad = 0;
      for (curr = br.readLine(); curr != null; curr = br.readLine()) {
        
        // Pad is greater than zero, if the previous line is truncated.
        if (pad > 0) {
          // Pad white spaces to before the first white space characters on this
          // line.
          Matcher m = FIRST_WORD.matcher(curr);
          m.find();
          String bkPart2 = m.group(1);
          String bkPart3 = m.group(2);
          String remain = m.group(3);
          
          if (bkPart2.length() == 0) {
            // If current line start with a white space, then restore the
            // character sequence on the previous line.
            sb.append('-').append(repeat(" ", pad));

          } else {
            // Check if the combining word is a valid word in the dictionary.
            // Otherwise keep the character sequence in the original text.
            String fixedWord1 = bkPart1 + bkPart2;
            String fixedWord2 = bkPart1 + "-" + bkPart2;
            LogUtils.info(">>> " + fixedWord1 + ", " + fixedWord2);

            if (dict.contains(fixedWord2.toLowerCase())) {
              curr = "-" + bkPart2 + bkPart3 + repeat(" ", pad) + remain;

              LogUtils.info("F2: " + fixedWord2 + bkPart3 + " = " + bkPart1 + "," + bkPart2);

            } else if (dict.contains(fixedWord1.toLowerCase())) {
              curr = bkPart2 + bkPart3 + repeat(" ", pad + 1) + remain;

              LogUtils.info("F1: " + fixedWord1 + bkPart3 + " = " + bkPart1 + "," + bkPart2);

            } else if (dict.contains(bkPart1.toLowerCase())
                && dict.contains(bkPart2.toLowerCase())) {
              curr = "-" + bkPart2 + bkPart3 + repeat(" ", pad) + remain;

              LogUtils.info("F2: " + fixedWord2 + bkPart3 + " = " + bkPart1 + "," + bkPart2);

            } else {
              curr = "-" + repeat(" ", pad) + curr;

              LogUtils.info("F3: " + fixedWord2 + bkPart3 + " = " + bkPart1 + "," + repeat(" ", pad) + "," + bkPart2);

            }
          }
          pad = 0;
        }
        
        // Check the line tail. If there is word broken by line wrap, remove the
        // internal hyphen and trailing white-spaces.
        Matcher brokenWord = BROKEN_WORD.matcher(curr);
        if (brokenWord.find()) {
          bkPart1 = brokenWord.group(1);
          pad = brokenWord.group(2).length();
          curr = curr.substring(0, curr.length() - pad);
          sb.append(curr);

        } else {
          // Insert a white-space characters to replace the newline character in
          // the input text.
          sb.append(curr).append(' ');
        }
      }
      // Concatenate the last line.
      sb.append(repeat(" ", pad));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return sb.toString();
  }
	
	public static String repeat(String str, int times)
	{
	  if (times < 0) {
	    throw new RuntimeException();
	  } else if (times == 0) {
	    return "";
	  }
	  return new String(new char[times]).replace("\0", str);
	}
}
