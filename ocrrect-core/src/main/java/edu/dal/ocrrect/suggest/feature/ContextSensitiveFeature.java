package edu.dal.ocrrect.suggest.feature;

import edu.dal.ocrrect.suggest.batch.ContextSensitiveBatchDetectMixin;
import edu.dal.ocrrect.suggest.batch.ContextSensitiveBatchScoreMixin;
import edu.dal.ocrrect.suggest.batch.ContextSensitiveBatchSearchMixin;
import edu.dal.ocrrect.suggest.batch.ContextSensitiveBatchSuggestMixin;

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
