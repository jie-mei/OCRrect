package edu.dal.ocrrect.suggest.feature;

public class DuplicateFeatureException extends Exception {
  private static final long serialVersionUID = 7547955836388427357L;

  public DuplicateFeatureException() {}

  public DuplicateFeatureException(String message) {
    super(message);
  }
}
