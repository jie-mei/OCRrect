package edu.dal.corr.suggest.feature;

import edu.dal.corr.suggest.batch.ContextSensitiveBatchDetectMixin;
import edu.dal.corr.suggest.batch.ContextSensitiveBatchScoreMixin;
import edu.dal.corr.suggest.batch.ContextSensitiveBatchSearchMixin;
import edu.dal.corr.suggest.batch.ContextSensitiveBatchSuggestMixin;

public abstract class ContextSensitiveFeature extends Feature
    implements ContextSensitiveBatchDetectMixin,
        ContextSensitiveBatchSearchMixin,
        ContextSensitiveBatchScoreMixin,
        ContextSensitiveBatchSuggestMixin {
  private static final long serialVersionUID = 6802336749995466573L;
  
  public ContextSensitiveFeature(String name) {
	  super(name);
  }
}
