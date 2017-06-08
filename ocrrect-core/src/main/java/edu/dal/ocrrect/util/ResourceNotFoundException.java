package edu.dal.ocrrect.util;

/**
 * A runtime exception for reporting non-existing resource.
 *
 * @since 2017.04.20
 */
public class ResourceNotFoundException extends RuntimeException {
  private static final long serialVersionUID = -4963258816141145288L;

  public ResourceNotFoundException() {
    super();
  }

  public ResourceNotFoundException(String message) {
    super(message);
  }

  public ResourceNotFoundException(Throwable throwable) {
    super(throwable);
  }

  public ResourceNotFoundException(String message, Throwable throwable) {
    super(message, throwable);
  }
}
