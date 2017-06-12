package edu.dal.ocrrect.text

import gnu.trove.set.hash.THashSet
import spock.lang.Specification

class LineConcatTextProcessorSpec extends Specification {
  def "test process"() {
    given:
      def proc = new LineConcatTextProcessor(new THashSet<String>(vocab))

    expect:
      proc.process(new Text(before)).text() == new Text(after).text()
      proc.process(before) == after

    where:
      vocab || before || after
      [] ||
      "Hello,\nworld!\n" ||
      "Hello, world! "
      [] ||
      "Hel-\nlo, world!\n" ||
      "Hel- lo, world! "
      ["Hello"] ||
      "Hel-\nlo, world!\n" ||
      "Hello,   world! "
      ["Hello"] ||
      "Hel-\nlo, world!\n" ||
      "Hello,   world! "
  }
}
