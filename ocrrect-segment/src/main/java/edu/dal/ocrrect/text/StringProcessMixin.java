package edu.dal.ocrrect.text;

interface StringProcessMixin extends Processor<Text> {
  default Text process(Text text) {
    return new Text(process(text.text()));
  }

  String process(String text);
}
