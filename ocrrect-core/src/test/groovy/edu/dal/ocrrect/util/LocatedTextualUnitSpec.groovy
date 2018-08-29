package edu.dal.ocrrect.util

import spock.lang.Shared
import spock.lang.Specification

class LocatedTextualUnitSpec extends Specification {
  @Shared units = [new LocatedTextualUnit("Hello", 0){},
                   new LocatedTextualUnit(",", 5){},
                   new LocatedTextualUnit("world", 7){},
                   new LocatedTextualUnit("!", 12){}]

  def "construct located textual unit with no position parameter"() {
    expect:
    new LocatedTextualUnit("Hello"){}
  }

  def "construct located textual unit with non-negative position"() {
    when:
      def unit = new LocatedTextualUnit("Hello", position){}
    then:
      unit.getPosition() == position
    where:
      position << [0, 125244, Integer.MAX_VALUE]
  }

  def "construct located textual unit with negative input"() {
    when:
      new LocatedTextualUnit("Hello", position){}
    then:
      def ex = thrown(exception)
      ex.message == "Construct located textual unit with a invalid position: " + position
    where:
      position          || exception
      -1                || IllegalArgumentException
      -1                || IllegalArgumentException
      Integer.MIN_VALUE || IllegalArgumentException
  }

  def "set position position with valid value"() {
    given:
       LocatedTextualUnit unit = new LocatedTextualUnit('unit', 0){}
    expect:
      unit.setPosition(position)
      unit.getPosition() == position
    where:
      position << [Integer.MAX_VALUE, 0, 52]
  }

  def "set position position with invalid value"() {
    given:
      def unit = new LocatedTextualUnit('unit', 0){}
    when:
      unit.setPosition(position)
    then:
      def ex = thrown(IllegalArgumentException)
      ex.message == "Set position with negative value: " + position
    where:
      position << [-1, -3234, Integer.MIN_VALUE]
  }

  def "get unassigned position position"() {
    given:
      def unit = new LocatedTextualUnit('unit'){}
    when:
      unit.position()
    then:
      thrown(PositionUnassignedException)
  }

  def "build hash code with dependency on text and position"() {
    expect:
      result == (units[idx].hashCode() == new LocatedTextualUnit(text, position){}.hashCode())
    where:
      idx || text    || position || result
      0   || "Hello" || 0        || true
      1   || ","     || 5        || true
      2   || "word"  || 7        || false
      3   || "!"     || 11       || false
  }

  def "equal to another object"() {
    expect:
      result == (units[idx] == another)
    where:
      idx || another                              || result
      0   || new LocatedTextualUnit("Hello", 0){} || true
      1   || new LocatedTextualUnit(".", 5){}     || false
      2   || new LocatedTextualUnit("world", 8){} || false
      3   || "!"                                  || false
  }
}
