package edu.dal.corr.suggest.feature;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import gnu.trove.map.hash.TObjectByteHashMap;
import gnu.trove.set.hash.THashSet;
import info.debatty.java.stringsimilarity.Levenshtein;

/**
 * This object support searching candidates within maximum levenshtein distance.
 * This class is implemented in a thread-safe lazy-loading singleton manner.
 *
 * @since 2016.08.11
 */
class ReverseLevenshteinDistanceSearcher
    implements Serializable
{
  private static final long serialVersionUID = -693442884043428965L;
  
  private static Map<THashSet<String>, ReverseLevenshteinDistanceSearcher> instanceMap =
      new WeakHashMap<>();
  
  public static ReverseLevenshteinDistanceSearcher getInstance(THashSet<String> dictionary) {
    if (instanceMap.containsKey(dictionary)) {
      return instanceMap.get(dictionary);
    }
    for (THashSet<String> instDict: instanceMap.keySet()) {
      if (instDict.size() != dictionary.size()) {
        continue;
      }
      boolean equals = true;
      for (String key: instDict) {
        if (! dictionary.contains(key)) {
          equals = false;
          break;
        }
      }
      if (equals) {
        return instanceMap.get(instDict);
      }
    }
    ReverseLevenshteinDistanceSearcher newInstance =
        new ReverseLevenshteinDistanceSearcher(dictionary);
    instanceMap.put(dictionary, newInstance);
    return newInstance;
  }

  /**
   * Categorize unigram by length for reducing search space of
   * reverse-edit-distance computation. This data is store and loaded once and
   * shared by all levenshtein distance instances.
   * 
   * TODO: unsafe for different unigram data.
   */
  private String[][] lexiconInLen;

  private Levenshtein lev;
  private THashSet<String> dict;

  /**
   * Construct an object with unigram and search distance threshold.
   * 
   * @param unigram  an unigram instance.
   * @param maxDistance  the maximum number of search distance.
   */
  ReverseLevenshteinDistanceSearcher(THashSet<String> dictionary) {
    this.dict = dictionary;
    this.lev = new Levenshtein();
    
    // Separate unigram by length.
    ArrayList<ArrayList<String>> uniList = new ArrayList<>();
    for (String gram : dictionary) {
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
    lexiconInLen = new String[uniList.size()][];
    for (int i = 0; i < uniList.size(); i++) {
      ArrayList<String> lenList = uniList.get(i);
      lexiconInLen[i] = lenList.toArray(new String[lenList.size()]);
    }
  }
  
  public Set<String> search(String word, int maxDistance) {
    Set<String> candidates = new HashSet<String>();
    int len = word.length();
    
    // Add unigrams with `maxDistance` number of inserting into candidate list.
    if (len - maxDistance > 0) {
      TObjectByteHashMap<String> permMap = new TObjectByteHashMap<>();
      permuteSubstrings(word, maxDistance).forEach(str -> {
        if (dict.contains(str)) {
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
        for (String gram : lexiconInLen[i]) {
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
  
  private List<String> permuteSubstrings(String word, int maxDistance)
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
