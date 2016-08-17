package edu.dal.corr.suggest;

/**
 * @since 2016.08.11
 */
class LevenshteinDistance
{
  private LevenshteinDistance() {}

  /**
   * Compute the minimum Levenshtein Distance between two strings.
   * 
   * @param  s1  A string.
   * @param  s2  Another string.
   * @return The minimum Levenshtein distance.
   * @throws DistanceExceedException  If distance greater than the expected
   *            maximum value.
   * @see    {@linkplain https://en.wikibooks.org/wiki/Algorithm_Implementation/Strings/Levenshtein_distance#Java}
   */
  static int compute(String s1, String s2)
  {
    int len1 = s1.length() + 1;
    int len2 = s2.length() + 1;
    
    // the array of distances
    int[] cost = new int[len1];
    int[] newCost = new int[len1];
    
    // initial cost of skipping prefix in String s1
    for (int i = 0; i < len1; i++) cost[i] = i;
    
    // dynamically computing the array of distances
    for (int j = 1; j < len2; j++) {
      newCost[0] = j;
      
      // transformation cost for each letter in s1
      for(int i = 1; i < len1; i++) {
        int match = (s1.charAt(i - 1) == s2.charAt(j - 1)) ? 0 : 1;
        int costReplace = cost[i - 1] + match;
        int costInsert  = cost[i] + 1;
        int costDelete  = newCost[i - 1] + 1;
        newCost[i] = Math.min(Math.min(costInsert, costDelete), costReplace);
      }                                                                           
      int[] swap = cost; cost = newCost; newCost = swap;                          
    }                                                                               
    return cost[len1 - 1];                       
  }
}
