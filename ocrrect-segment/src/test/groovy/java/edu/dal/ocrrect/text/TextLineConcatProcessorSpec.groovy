package edu.dal.ocrrect.text

import edu.dal.ocrrect.util.lexicon.Lexicon
import edu.dal.ocrrect.util.lexicon.Lexicons
import gnu.trove.set.hash.THashSet
import spock.lang.Specification

class TextLineConcatProcessorSpec extends Specification {
  def "test process"() {
    given:
      def proc = new TextLineConcatProcessor(Lexicons.toLexicon(vocab))

    expect:
      proc.process(new Text(before)).text() == new Text(after).text()
      proc.process(before) == after

    where:
      vocab || before || after
      [] ||
      "Hello,\nworld!\n" ||
      "Hello, world! "
      ["Hel", "lo"] ||
      "Hel-\nlo, world!\n" ||
      "Hel-lo,  world! "
      [] ||
      "Hel-\nlo, world!\n" ||
      "Hello,   world! "
  }
}
