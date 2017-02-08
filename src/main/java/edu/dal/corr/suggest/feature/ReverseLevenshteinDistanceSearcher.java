package edu.dal.corr.suggest.feature;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import edu.dal.corr.util.Unigram;
import edu.dal.corr.word.Word;
import gnu.trove.map.hash.TObjectByteHashMap;
import info.debatty.java.stringsimilarity.Levenshtein;

/**
 * This object support searching candidates within maximum levenshtein distance.
 * This class is implemented in a thread-safe lazy-loading singleton manner.
 *
 * @since 2016.08.11
 */
class ReverseLevenshteinDistanceSearcher
  implements Searchable, Serializable
{
  private static final long serialVersionUID = -693442884043428965L;

  /**
   * Categorize unigram by length for reducing search space of
   * reverse-edit-distance computation. This data is store and loaded once and
   * shared by all levenshtein distance instances.
   * 
   * TODO: unsafe for different unigram data.
   */
  private static String[][] unigramInLen;
  
  private Levenshtein lev;
  private Unigram unigram;
  private int maxDistance;

  /**
   * Construct an object with unigram and search distance threshold.
   * 
   * @param unigram  an unigram instance.
   * @param maxDistance  the maximum number of search distance.
   */
  ReverseLevenshteinDistanceSearcher(Unigram unigram, int maxDistance)
  {
    if (maxDistance > 0) {
      this.maxDistance = maxDistance;
    } else {
      throw new IllegalArgumentException(
          "Non-positive distance threshold given: " + maxDistance);
    }
    this.unigram = unigram;
    
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
    lev = new Levenshtein();
  }

  @Override
  public List<String> search(Word word) {
    return search(word.text());
  }
  
  private List<String> search(String word)
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
      try {
        for (String gram : unigramInLen[i]) {
          if (lev.distance(gram, word) <= maxDistance) {
            candidates.add(gram);
          }
        }
      } catch (ArrayIndexOutOfBoundsException e) {
        // If the searching length overflows the unigram data.
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
}
