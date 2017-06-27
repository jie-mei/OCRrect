package edu.dal.ocrrect.text;

import edu.dal.ocrrect.util.lexicon.Lexicon;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class FragmentConcatSegmentsProcessor implements Processor<TextSegments> {

  private static final Pattern TEXT = Pattern.compile("^"
    + "(?<bracketLeft>(\\(?['\"]?|['\"]?\\(?))"
    + "(?<content>.*?)"
    + "(?<punctTail>(((['\"]?\\)?|\\)['\"])?\\*?[:;,.!?]?|[:;,.!?]\\*?\\)?['\"]?)\\*?)?)$"
  );

  private boolean contains(String word) {
    return vocab.contains(word)
      || (! caseSensitive && vocab.contains(word.toLowerCase()));
  }

  private static final Pattern CORRECT = Pattern.compile("^"
      + "(" + "[a-zA-Z-]*[a-zA-Z]"  // pure letters
      + "|" + "\\p{Digit}+"  // pure number
      + "|" + "\\p{Digit}*(1st|2nd|[34567890]th)"  // ordering number
      + ")$");

  private boolean isError(String part) {
     return ! CORRECT.matcher(part).matches();
//     return ! contains(part);
  }

  private static HashMap<String, String[]> SEGMENT = new HashMap<>();
  static {
    SEGMENT.put("cannot", new String[]{"can", "not"});
    SEGMENT.put("don't", new String[]{"do", "n't"});
  }

  private static final Pattern SUFFIX = Pattern.compile("^(?<content>.*?)(?<suffix>'s)$");

  private boolean caseSensitive;
  private Lexicon vocab;
  private Pattern tailPunct;

  public FragmentConcatSegmentsProcessor(Lexicon vocab, boolean caseSensitive, String tailPuncts) {
    this.vocab = vocab;
    this.caseSensitive = caseSensitive;
    this.tailPunct = tailPuncts.length() > 0
        ? Pattern.compile("^(?<content>.*?)(?<punct>[" + tailPuncts + "])$")
        : null;
  }

  public FragmentConcatSegmentsProcessor(Lexicon vocab) {
    this(vocab, false, "");
  }

  private static final Pattern DEHYPHENATE = Pattern.compile("^"
    + "(?<part>[^-]*)"  // while allowing the heading hyphen in the pattern, we will detect in code.
    + "(?<hyphen>-)"
    + "(?<remain>.+?)$"  // avoid the tailing hyphen
  );

  private List<String> dehyphenate(String str) {
    String input = str;
    List<String> parts = new ArrayList<>();
    // De-hyphenate the word where the hyphen character occurs.
    for (Matcher pm = DEHYPHENATE.matcher(str); pm.matches();
         str = pm.group("remain"), pm = DEHYPHENATE.matcher(str)) {
      parts.add(pm.group("part"));
      parts.add("-");
    }
    // Allow the tailing hyphen only if the part ahead is in vocabulary.
    if (str.charAt(str.length() - 1) == '-'
      && contains(str.substring(0, str.length() - 1))) {
      parts.add(str.substring(0, str.length() - 1));
      parts.add("-");
    } else {
      parts.add(str);  // the remaining content
    }
    // Remove the empty strings from parts.
    for (int i = parts.size() - 1; i >= 0; i--) {
      if (parts.get(i).length() == 0) {
        parts.remove(i);
      }
    }
    // Confirm de-hyphenation if the internal hyphens are true punctuations. We check if any one
    // of the de-hyphenated parts is in vocabulary.
    boolean confirm = false;
    for (int i = 0; i < parts.size(); i ++) {
      String part = parts.get(i);
      if (! part.equals("-") && contains(part)) {
        // System.out.print(part + " ");
        confirm = true;
        break;
      }
    }
    // System.out.println(parts);
    if (! confirm) {
      parts = Arrays.asList(input);
    }
    // System.out.println((confirm ? "T" : "F") + parts);
    return parts;
  }

  private List<Segment> defragment(List<Segment> fragments) {
    if (fragments.size() == 1) {
      return fragments;
    }
    String text = fragments.stream().map(Segment::text).collect(Collectors.joining());
    List<String> splits = new ArrayList<>();

    // Always treat the given tailing punctuation characters as the real punctuations.
    String content = text;
    String punct = "";
    if (tailPunct != null) {
      Matcher tailPunctMatcher = tailPunct.matcher(text);
      if (tailPunctMatcher.matches()) {
        content = tailPunctMatcher.group("content");
        punct = tailPunctMatcher.group("punct");
      }
    }

    Matcher m = TEXT.matcher(content);
    m.matches();

    // Retrieve de-hyphenated content splits
    content = m.group("content");
    String suffix = "";
    boolean lError = false;  // if split on the either side is an error
    boolean rError = false;
    if (content.length() > 0) {

      // Extract PTB-defined suffices.
      Matcher suffixMatcher = SUFFIX.matcher(content);
      if (suffixMatcher.matches()) {
        content = suffixMatcher.group("content");
        suffix = suffixMatcher.group("suffix");
      }

      // Check if the word fits a PTB-defined segmentation rule.
      if (SEGMENT.containsKey(content)) {
        splits.addAll(Arrays.asList(SEGMENT.get(content)));
      } else {
        splits.addAll(dehyphenate(content));
      }

      // Determine the correctness of the word according to the parts at either side.
      lError = isError(splits.get(0));
      rError = isError(splits.get(splits.size() - 1));
    }

    // Retrieve the left bracket. The first content part concatenates this part if it is an error.
    String lBracket = m.group("bracketLeft");
    if (lBracket.length() > 0) {
      if (lError) {
        splits.set(0, lBracket + splits.get(0));
      } else {
        for (int i = 0; i < lBracket.length(); i++) {
          splits.add(i, lBracket.charAt(i) + "");
        }
      }
    }

    // Retrieve the right bracket and the tailing punctuation. If the last part of the content is an
    // error, concat it with all the retrieved characters.
    String puncts = m.group("punctTail");
    // System.out.println(Boolean.toString(rError) + " " + puncts + " " + puncts.length());
    if (puncts.length() > 0) {
      if (rError) {
        // System.out.println("true");
        int last = splits.size() - 1;
        splits.set(last, splits.get(last) + suffix + puncts);
      } else {
        if (suffix.length() > 0) {
          splits.add(suffix);
        }
        for (int i = 0; i < puncts.length(); i++) {  // each character should be a separated split
          splits.add(puncts.charAt(i) + "");
        }
      }
    }

//    System.out.println(String.format("%-7d %-20s", fragments.get(0).position(), text)
//        + "(" + Boolean.toString(lError) + ", " + Boolean.toString(rError)
//        + "): " + splits);

    // Construct segments from splits.
    List<Segment> segments = new ArrayList<>();
    int pos = fragments.get(0).position();
    for (String split: splits) {
      segments.add(new Segment(split, pos));
      pos += split.length();
    }
    if (punct.length() > 0) {
      segments.add(new Segment(punct, pos));
    }
    return segments;
  }

  @Override
  public TextSegments process(TextSegments textSegments) {
    List<Segment> segments = new ArrayList<>();

    List<Segment> fragments = new ArrayList<>();
    Segment prev = null;
    for (Segment curr: textSegments) {
      if (prev != null && curr.position() > prev.position() + prev.text().length()) {
        segments.addAll(defragment(fragments));
        fragments = new ArrayList<>();
      }
      fragments.add(curr);
      prev = curr;
    }
    return new TextSegments(segments);
  }
}
