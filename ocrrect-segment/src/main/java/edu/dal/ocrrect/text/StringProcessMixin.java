package edu.dal.ocrrect.text;

import java.util.function.Function;

interface StringProcessMixin {
  default String process(Function<String, String> function) {
    return function.apply(this);
  }
}
