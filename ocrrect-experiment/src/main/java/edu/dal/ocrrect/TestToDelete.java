package edu.dal.ocrrect;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestToDelete {

  public static void main(String[] args) {
    List<Integer> vals = Arrays.asList(1, 2, 3, 4, 5);
    vals.stream()
        .filter(val -> val < 3)
        .forEach(System.out::println);
  }
}
