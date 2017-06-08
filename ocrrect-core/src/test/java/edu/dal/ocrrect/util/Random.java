package edu.dal.ocrrect.util;

import org.apache.commons.lang.RandomStringUtils;

/**
 * This class extends {@link java.util.Random} and provide extra operations for
 * unit testing.
 * 
 * @since
 */
public class Random
  extends java.util.Random
{
  private static final long serialVersionUID = -564122495796518479L;

  /**
   * Returns a pseudorandom, uniformly distributed {@code int} value which is
   * not equals to a given value.
   * 
   * @param  val  a int value.
   * @return the next pseudorandom, uniformly distributed int value which is not
   *         equal to a specific value from this random number generator's
   *         sequence.
   */
  public int nextIntNot(int val)
  {
    int value = val;
    while ((value = nextInt()) == val);
    return value;
  }

  /**
   * Returns a pseudorandom, uniformly distributed {@code int} value which is
   * not in a specific range.
   * 
   * @param  lowerbound  a range lowerbound, inclusive.
   * @param  upperbound  a range upperbound, exclusive.
   * @return the next pseudorandom, uniformly distributed int value out of a
   *         range defined by a lowerbound (inclusive) and a upperbound
   *         (exclusive) from this random number generator's sequence.
   */
  public int nextIntOutOfRange(int lowerbound, int upperbound)
  {
    int val = lowerbound;
    do {
      val = nextInt();
    } while (val >= lowerbound && val < upperbound);
    return val;
  }

  /**
   * Returns a pseudorandom, uniformly distributed {@code int} value which is
   * in a specific range.
   * 
   * @param  lowerbound  a range lowerbound, inclusive.
   * @param  upperbound  a range upperbound, exclusive.
   * @return the next pseudorandom, uniformly distributed int value between a
   *         lowerbound (inclusive) and a upperbound (exclusive) from this
   *         random number generator's sequence.
   */
  public int nextIntInRange(int lowerbound, int upperbound) {
    return nextInt(upperbound - lowerbound) + lowerbound;
  }

  /**
   * Generate a random ASCII strings.
   * 
   * @param  size  the size of the output string.
   * @return a randomly generated string.
   */
  public String nextString(int size) {
    return RandomStringUtils.randomAscii(size);
  }

  /**
   * Generate a random ASCII strings, which length is smaller than 10.
   * 
   * @param  size  the size of the output string.
   * @return a randomly generated string.
   */
  public String nextString() {
    return RandomStringUtils.randomAscii(nextInt(10));
  }

  /**
   * Generate a random ASCII strings, which length is smaller than 10 and not
   * equals to a specific value.
   * 
   * @param  str  a string object.
   * @return a randomly generated string, which length is smaller than 10 and
   *         not equals to a specific value.
   */
  public String nextStringNot(String str)
  {
    String newStr = null;
    while ((newStr = nextString()).equals(str));
    return newStr;
  }

  /**
   * Generate an array of random ASCII strings. Each string is filled with
   * randomly selected ASCII characters, which length is smaller than 10.
   * 
   * @param  size  the size of the output string array.
   * @return an array of randomly generated strings.
   */
  public String[] nextStringArray(int size)
  {
    String[] randStrs = new String[size];
    for (int i = 0; i < size; i++) {
      randStrs[i] = nextString();
    }
    return randStrs;
  }
}
