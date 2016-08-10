package edu.dal.corr.suggest;

import java.util.ArrayList;
import java.util.List;

import edu.dal.corr.util.Unigram;
import gnu.trove.map.hash.TObjectByteHashMap;

/**
 * This object support searching candidates within maximum levenshtein distance.
 * This class is implemented in a thread-safe lazy-loading singleton manner.
 *
 * @since 2016.07.28
 */
class LevenshteinDistance
{
  /**
   * Categorize unigram by length for reducing search space of
   * reverse-edit-distance computation. This data is store and loaded once and
   * shared by all levenshtein distance instances.
   * 
   * TODO: unsafe for different unigram data.
   */
  private static String[][] unigramInLen;
  
  private Unigram unigram;
  private int maxDistance;

  LevenshteinDistance(int maxDistance)
  {
    if (maxDistance > 0) {
      this.maxDistance = maxDistance;
    } else {
      throw new IllegalArgumentException(
          "Non-positive distance threshold given: " + maxDistance);
    }
    unigram = Unigram.getInstance();
    
    if (unigramInLen == null) {
      // Separate unigram by length.
      ArrayList<ArrayList<String>> uniList = new ArrayList<>();
      for (String gram : unigram.keys()) {
        int len = gram.length();
        try {
          uniList.get(len).add(gram);
        } catch (IndexOutOfBoundsException e) {
          int uniListSize = uniList.size();
          for (int i = uniListSize; i <= len; i++) {
            uniList.add(new ArrayList<>());
          }
          uniList.get(len).add(gram);
        }
      }
      unigramInLen = new String[uniList.size()][];
      for (int i = 0; i < uniList.size(); i++) {
        ArrayList<String> lenList = uniList.get(i);
        unigramInLen[i] = lenList.toArray(new String[lenList.size()]);
      }
    }
  }
  
  List<String> search(String word)
  {
    List<String> candidates = new ArrayList<String>();
    int len = word.length();
    
    // Add unigrams with `maxDistance` number of inserting into candidate list.
    if (len - maxDistance > 0) {
      TObjectByteHashMap<String> permMap = new TObjectByteHashMap<>();
      permuteSubstrings(word).forEach(str -> {
        if (unigram.contains(str)) {
          // Avoid inserting duplicate candidate.
          if (! permMap.containsKey(str)) {
            permMap.put(str, (byte) 0);
            candidates.add(str);
          }
        }
      });
    }
    
    // Compare the given word with all unigrams with length difference less than
    // `maxDistance`.
    for (int i = (len - maxDistance + 1 > 0 ? len - maxDistance + 1 : 0);
         i <= len + maxDistance; i++) {
      for (String gram : unigramInLen[i]) {
        if (distance(gram, word) <= maxDistance) {
          candidates.add(gram);
        }
      }
    }
    return candidates;
  }
  
  private List<String> permuteSubstrings(String word)
  {
    List<String> candidates = new ArrayList<String>();
    int len = word.length();
    // Assume length boundary checked outside and omit here for the efficiency
    // consideration.
    // TODO

    // Iterate through all possible selecting positions combinations. Preset all
    // selecting positions to the left most slots. And start moving from the
    // smallest possible selecting position. Each time a selecting position
    // moves, all position smaller than it need to be reset to the left most
    // slots. Repeat until there is no more movement can be made.
    int[] pos = new int[len - maxDistance];
    
    // Initial state.
    for (int i = 0; i < pos.length; i++) pos[i] = i;
    candidates.add(buildString(word, pos));

    int moveIdx = 0;
    while (pos[0] < len - pos.length) {

      // Find a movable position with the smallest index.
      if (moveIdx == 0) {
        while (moveIdx < pos.length - 1 && pos[moveIdx] + 1 == pos[moveIdx + 1]) {
          moveIdx++;
        }
      } else {
        moveIdx--;
      }
      if (moveIdx == pos.length - 1) {
        while (moveIdx >= 0 && pos[moveIdx] == len - pos.length + moveIdx) {
          moveIdx--;
        }
      }

      // Make the move.
      pos[moveIdx]++;
      
      // Reset positions with smaller index number than `moveIdx`.
      if (moveIdx != 0 && pos[moveIdx - 1] > moveIdx - 1) {
        for (int i = 0; i < moveIdx; i++) pos[i] = i;
      }

      candidates.add(buildString(word, pos));
    }
    return candidates;
  }
  
  private String buildString(String str, int[] pos)
  {
    StringBuilder sb = new StringBuilder();
    for (int i : pos) {
      sb.append(str.charAt(i));
    }
    return sb.toString();
  }
  
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
  int distance(String s1, String s2)
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
