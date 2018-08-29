package edu.dal.ocrrect.util

import spock.lang.Shared
import spock.lang.Specification

class TextualUnitSpec extends Specification {
  @Shared units = [new TextualUnit("Hello"){},
                   new TextualUnit(","){},
                   new TextualUnit("world"){},
                   new TextualUnit("!"){}]

  def "construct textual unit"() {
    expect:
      new TextualUnit("Hello"){}
  }

  def "get text"() {
    expect:
      units[idx].getText() == text
    where:
      idx || text
      0   || "Hello"
      1   || ","
      2   || "world"
      3   || "!"
  }

  def "build hash code with dependency on text"() {
    expect:
      units[idx].hashCode() == new TextualUnit(text){}.hashCode()
    where:
      idx || text
      0   || "Hello"
      1   || ","
      2   || "world"
      3   || "!"
  }

  def "equal to another object"() {
    expect:
      result == (units[idx] == another)
    where:
      idx || another                     || result
      0   || new TextualUnit("Hello"){}  || true
      1   || new TextualUnit(","){}      || true
      2   || new TextualUnit("word"){}   || false
      3   || "?"                         || false
  }
}
