package edu.dal.ocrrect.word.filter;

import edu.dal.ocrrect.util.Unigram;

/**
 * @since 2017.04.20
 */
public class HuristicGoogleNgramThresholdWordFilter extends AbstractNgramThresholdWordFilter {
  public HuristicGoogleNgramThresholdWordFilter() {
    super(Unigram.getInstance(), new float[]{
      0,        // 0
      0,        // 1
      10000000, // 2
      1000000,  // 3
      100000,   // 4
      100000,   // 5
      10000,    // 6
      10000,    // 7
      10000,    // 8
      10000,    // 9
      10000,    // 10
      1000,     // 11
      1000,     // 12
      1000,     // 13
      1000,     // 14
      1000,     // 15
      1000,     // 16
      200,      // 17
      200,      // 18
      200,      // 19
      200,      // 20
    });         // 20+ (threshold is 0 as default)
  }
}
