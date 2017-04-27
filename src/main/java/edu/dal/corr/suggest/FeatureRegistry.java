package edu.dal.corr.suggest;

import gnu.trove.map.hash.TObjectIntHashMap;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.builder.HashCodeBuilder;

import edu.dal.corr.suggest.feature.DuplicateFeatureException;
import edu.dal.corr.suggest.feature.Feature;
import edu.dal.corr.suggest.feature.FeatureType;

/**
 * An abstract registry for the applied features. Features can be registered in order. It restrict
 * features with the same type name to be used in the same process batch.
 *
 * @author Jie Mei
 * @since 2017.04.23
 */
class FeatureRegistry implements Serializable {
  private static final long serialVersionUID = -4951559358593534810L;

  private TObjectIntHashMap<FeatureType> typeMap;
  private List<FeatureType> typeList;

  /**
   * Construct a feature registry.
   */
  FeatureRegistry() {
    typeMap = new TObjectIntHashMap<>();
    typeList = new ArrayList<>();
  }

  /**
   * Construct a feature registry with a list of features to be registered.
   * 
   * @throws DuplicateFeatureException if the given feature has already been registered.
   */
  FeatureRegistry(List<FeatureType> types) throws DuplicateFeatureException {
    this();
    for (FeatureType type: types) {
      register(type);
    }
  }

  /**
   * Register a new feature.
   *
   * @param type a feature type.
   * @throws DuplicateFeatureException if the given feature has already been registered.
   */
  void register(FeatureType type) throws DuplicateFeatureException {
    if (typeMap.contains(type)) {
      throw new DuplicateFeatureException("Duplicate feature: " + type.toString());
    } else {
      typeMap.put(type, typeList.size());
      typeList.add(type);
    }
  }

  /**
   * Get a list of registered feature types.
   *
   * @return a list of feature types.
   */
  public List<FeatureType> types() {
    return typeList;
  }

  /**
   * Get the index of the registered feature type.
   *
   * @param type a feature type.
   * @return the index of the registered feature type, or {@code null} if this feature does not
   *     exist in the registry.
   */
  public int getIndex(FeatureType type) {
    return typeMap.get(type);
  }

  /**
   * Get the index of the registered feature.
   *
   * @param type a feature type.
   * @return the index of the registered feature, or {@code null} if this feature does not exist in
   *     the registry.
   */
  public int getIndex(Feature feature) {
    return getIndex(feature.type());
  }

  /**
   * Get the type of the feature registered at a specific position.
   *
   * @param index a integer index.
   * @return A feature type, or {@code null} if the index is out of the bound.
   */
  public FeatureType getType(int index) {
    if (index >= typeList.size()) {
      return null;
    } else {
      return typeList.get(index);
    }
  }

  @Override
  public int hashCode() {
    HashCodeBuilder hcb = new HashCodeBuilder();
    for (FeatureType type: typeList) {
      hcb.append(type.hashCode());
    }
    return hcb.toHashCode();
  }
}
