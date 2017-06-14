package edu.dal.ocrrect;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestToDelete {

  private static Pattern LINE_BROKEN = Pattern.compile("(.*)-↵(.*)");

  public static void main(String[] args) {
    String s = "hello-↵world";
    Matcher m = LINE_BROKEN.matcher(s);
    System.out.println(m);
    System.out.println(m.matches());
//    System.out.println(m.toMatchResult().group(2));
    System.out.println(m);
    System.out.println(m.group(1) + m.group(2));

  }
}
