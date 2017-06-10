package edu.dal.ocrrect.text

import gnu.trove.set.hash.THashSet
import spock.lang.Specification

class LineConcatProcessorSpec extends Specification {
  def "test process"() {
    given:
      def proc = new LineConcatProcessor(new THashSet<String>(vocab))
      def actual = proc.process(new Text(before))
      def expect = new Text(after)

    expect:
      actual.text() == expect.text()

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
