package edu.dal.ocrrect.text;

import gnu.trove.set.hash.THashSet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.commons.lang.StringUtils.repeat;

public class LineConcatProcessor implements TextProcessor, StringProcessMixin {

  private static final Pattern BROKEN_WORD = Pattern.compile("([a-zA-Z]+)(-\\s*)$");
  private static final Pattern FIRST_WORD = Pattern.compile("^([a-zA-Z]*)(\\S*)(.*)$");

  private THashSet<String> dict;

  public LineConcatProcessor(THashSet<String> dict) {
    this.dict = dict;
  }

  @Override
  public String process(String text) {
    StringBuilder sb = new StringBuilder();
    try (BufferedReader br = new BufferedReader(new StringReader(text))) {

      String bkPart1 = null;
      int pad = 0;
      for (String curr = br.readLine(); curr != null; curr = br.readLine()) {

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
            // LogUtils.info(">>> " + fixedWord1 + ", " + fixedWord2);

            if (dict.contains(fixedWord2)
                || dict.contains(fixedWord2.toLowerCase())) {
              curr = "-" + bkPart2 + bkPart3 + repeat(" ", pad) + remain;
              // LogUtils.info("F2: " + fixedWord2 + bkPart3 + " = " + bkPart1 + "," + bkPart2);

            } else if (dict.contains(fixedWord1)
                || dict.contains(fixedWord1.toLowerCase())) {
              curr = bkPart2 + bkPart3 + repeat(" ", pad + 1) + remain;
              // LogUtils.info("F1: " + fixedWord1 + bkPart3 + " = " + bkPart1 + "," + bkPart2);

            } else if ((dict.contains(bkPart1)
                || dict.contains(bkPart1.toLowerCase()))
                && dict.contains(bkPart2.toLowerCase())) {
              curr = "-" + bkPart2 + bkPart3 + repeat(" ", pad) + remain;
              // LogUtils.info("F2: " + fixedWord2 + bkPart3 + " = " + bkPart1 + "," + bkPart2);

            } else {
              curr = "-" + repeat(" ", pad) + curr;
              // LogUtils.info("F3: " + fixedWord2 + bkPart3 + " = " + bkPart1 + "," + repeat(" ", pad) + "," + bkPart2);
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
}
