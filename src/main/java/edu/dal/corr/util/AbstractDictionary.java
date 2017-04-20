package edu.dal.corr.util;

import java.util.List;

import gnu.trove.set.hash.THashSet;

class AbstractDictionary implements Dictionary {
  private THashSet<String> dict;

  /**
   * Construct the dictionary with a set of words.
   *
   * @param set
   */
  protected AbstractDictionary(THashSet<String> set) {
    dict = set;
  }

  /**
   * Construct the dictionary with a list of words.
   *
   * @param  list  a word list.
   */
  protected AbstractDictionary(List<String> list) {
    this(toTHashSet(list));
  }

  private static THashSet<String> toTHashSet(List<String> list) {
    THashSet<String> set = new THashSet<>();
    list.forEach(w -> set.add(w));
    return set;
  }

  @Override
  public boolean contains(String word) {
    return dict.contains(word);
  }
}
