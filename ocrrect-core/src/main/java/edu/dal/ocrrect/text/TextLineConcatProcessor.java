package edu.dal.ocrrect.text;

import edu.dal.ocrrect.Text;
import edu.dal.ocrrect.util.lexicon.Lexicon;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.commons.lang.StringUtils.repeat;

public class TextLineConcatProcessor implements Processor<Text>, StringProcessMixin {

  private static final Pattern BROKEN_WORD = Pattern.compile("([a-zA-Z]+)(-\\s*)$");
  private static final Pattern FIRST_WORD = Pattern.compile("^(([a-zA-Z]*)\\S*)(.*)$");

  private Lexicon vocab;
  private boolean caseSensitive;

  /**
   * @param lexicon a vocabulary lexicon.
   * @param caseSensitive {@code true} if adopt case sensitive matching with vocabulary.
   */
  public TextLineConcatProcessor(Lexicon lexicon, boolean caseSensitive) {
    this.vocab = lexicon;
    this.caseSensitive = caseSensitive;
  }

  public TextLineConcatProcessor(Lexicon lexicon) {
    this(lexicon, true);
  }

  private boolean contains(String word) {
    return vocab.contains(word)
        || (! caseSensitive && vocab.contains(word.toLowerCase()));
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
          // Pad white spaces to before the first white space characters on this line.
          Matcher m = FIRST_WORD.matcher(curr);
          m.find();
          String bkPart2 = m.group(1);
          String bkPart2En = m.group(2);
          String remain = m.group(3);

          if (bkPart2.length() == 0) {
            // If current line start with a white space, then restore the character sequence on the
            // previous line.
            sb.append('-').append(repeat(" ", pad));

          } else {
            // If either part is a real word, the two parts is heuristically regarded as one
            // hyphenated word. This rule is set to best identify potential hyphenated words.
            if ((contains(bkPart1) && contains(bkPart2En)) || contains(bkPart1 + "-" + bkPart2En)) {
              curr = "-" + bkPart2 + repeat(" ", pad) + remain;

            // Otherwise, the two parts are regarded as broken pieces. Thus we merge these two
            // pieces with hyphen character removed.
            } else {
              curr = bkPart2 + repeat(" ", pad + 1) + remain;
            }
          }
          pad = 0;
        }

        // Check the line tail. If there is word broken by line wrap, remove the internal hyphen and
        // trailing white-spaces.
        Matcher brokenWord = BROKEN_WORD.matcher(curr);
        if (brokenWord.find()) {
          bkPart1 = brokenWord.group(1);
          pad = brokenWord.group(2).length();
          curr = curr.substring(0, curr.length() - pad);
          sb.append(curr);

        } else {
          // Insert a white-space characters to replace the newline character in the input text.
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
