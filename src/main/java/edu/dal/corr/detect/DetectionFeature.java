package edu.dal.corr.detect;

public abstract class DetectionFeature implements Detectable {
  private String name;
  
  protected void setName(String name) {
    this.name = name;
  }
  
  protected String getName() {
    return name;
  }

  @Override
  public String toString() {
    return name == null ? getClass().getName() : String.format("%s$%s", getClass().getName(), name);
  }
}
