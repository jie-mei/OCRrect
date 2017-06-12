package edu.dal.ocrrect.text;

public interface Processor<T> {
  T process(T textualUnits);
}
