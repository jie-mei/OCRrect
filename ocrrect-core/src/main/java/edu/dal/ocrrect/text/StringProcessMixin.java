package edu.dal.ocrrect.text;

import edu.dal.ocrrect.Text;

interface StringProcessMixin extends Processor<Text> {
  default Text process(Text text) {
    return new Text(process(text.text()));
  }

  String process(String text);
}
