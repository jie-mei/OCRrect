package edu.dal.ocrrect;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestToDelete {

  private static final Pattern FRAGMENT = Pattern.compile("^"
    + "(?<bracketLeft>[('\"]?)"
    + "(?<content>.*?)"
    + "(?<punctTail>(([)'\"]?[:;,.!?]?|[:;,.!?][)'\"])\\*?)?)$"
  );

  private static final Pattern PARTS = Pattern.compile("^"
    + "(?<part>.*?)"
    + "(?<hyphen>-)"
    + "(?<remain>.*?)$"
  );

  private static final Pattern ERROR = Pattern.compile("[^\\p{Alnum}]");

  private static boolean isError(String part) {
    return ! ERROR.matcher(part).find();
  }

  public static void main(String[] args) {
    print("'hello-our-world)!");
    System.out.println();
    print("'hello-our-world.\"");
    System.out.println();
    print("'hello-our-world!");
    System.out.println();
    print("(Kent),");
  }

  private static void print(String s) {
    Matcher m = FRAGMENT.matcher(s);

    if (m.matches()) {
      for (int i = 0; i <= m.groupCount(); i++) {
        System.out.println(i + ": " + m.group(i));
      }
      System.out.println("bracketLeft: " + m.group("bracketLeft"));
      System.out.println("content:     " + m.group("content"));
      System.out.println("punctTail:   " + m.group("punctTail"));
    }
    String content = m.group("content");
    System.out.println(content);
    for (Matcher pm = PARTS.matcher(content); pm.matches();
         content = pm.group("remain"), pm = PARTS.matcher(content)) {
      System.out.println(pm.group("part"));
      System.out.println(pm.group("hyphen"));
    }
    System.out.println(content);
  }
}
