package edu.dal.ocrrect.text

import gnu.trove.set.hash.THashSet
import spock.lang.Specification

class LineConcatProcessorSpec extends Specification {
  def "test construct"() {
    given:
    def proc = new LineConcatProcessor(new THashSet<String>())

    expect:
    proc.process(new Text(before)).text() == new Text(after).text()

      where:
    before || after
    "Hello,\nworld!\n" ||
    "Hello, world! "
  }
}
