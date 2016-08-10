package edu.dal.corr.util;

public class Timer
{
  private long str;

  public Timer()
  {
    str = System.currentTimeMillis();
  }
  
  /**
   * Reset the timer.
   * 
   * @return  This timer.
   */
  public Timer start()
  {
    str = System.currentTimeMillis();
    return this;
  }

  /**
   * Return the time interval between the last start of the time to this method
   * call.
   * 
   * @return The time interval measured in seconds.
   */
  public float interval()
  {
    long curr = System.currentTimeMillis();
    return (curr - str) / 1000f;
  }
}
