package edu.dal.ocrrect.text;

interface StringProcessMixin extends Processor<edu.dal.ocrrect.Text> {
  default edu.dal.ocrrect.Text process(edu.dal.ocrrect.Text text) {
    return new edu.dal.ocrrect.Text(process(text.text()));
  }

  String process(String text);
}
