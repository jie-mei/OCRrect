package edu.dal.corr.util;

import java.util.List;

import gnu.trove.map.TObjectByteMap;
import gnu.trove.map.hash.TObjectByteHashMap;

class AbstractDictionary
  implements Dictionary
{
  private TObjectByteMap<String> dict;
 
  
  protected AbstractDictionary(TObjectByteMap<String> map)
  {
    dict = map;
  }
  
  private static TObjectByteMap<String> toMap(List<String> list)
  {
    TObjectByteMap<String> map = new TObjectByteHashMap<>();
    list.forEach(w -> map.put(w, (byte) 0));
    return map;
  }
  
  protected AbstractDictionary(List<String> list)
  {
    this(toMap(list));
  }

  @Override
  public boolean contains(String word) {
    return dict.containsKey(word);
  }
}
