package edu.dal.ocrrect.text

import spock.lang.Specification

class TextSpec extends Specification {
  def "test construct"() {
    given:
      def txt = "Hello, world!"

    expect:
      new edu.dal.ocrrect.Text(txt).text() == txt
  }

  def "test process"() {
    // TODO add test
  }
}
