package edu.dal.ocrrect.util

import spock.lang.Shared
import spock.lang.Specification

class WordsSpec extends Specification {
  @Shared tokens = [new Token("Hello", 0),
                    new Token(",", 5),
                    new Token("world", 7),
                    new Token("!", 12)]

  def "test construct"() {
    expect:
      new Words()
  }

  def "test tokens to words"() {
    given:
      def cvrted = Words.toWords(tokens)

    expect:
      cvrted[idx].position() == pos
      cvrted[idx].context() == context

    where:
      idx || pos || context
      0   || 0   || ["", "", "", "", "Hello", ",", "world", "!"]
      1   || 5   || ["", "", "", "Hello", ",", "world", "!", ""]
      2   || 7   || ["", "", "Hello", ",", "world", "!", "", ""]
      3   || 12  || ["", "Hello", ",", "world", "!", "", "", ""]
  }
}
