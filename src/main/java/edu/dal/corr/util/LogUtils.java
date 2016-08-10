package edu.dal.corr.util;

import java.util.stream.Stream;

import org.apache.log4j.Logger;

public class LogUtils {
  
  public static final Logger RUNTIME = Logger.getLogger("system.runtime");

	private static Logger getCallerLogger()
	{
		return Logger.getLogger(Thread.currentThread().getStackTrace()[3].getClassName());
	}
	
	private static final int MAX_LEVEL = 3;
	
	private static String buildMessage(Object... messages)
	{
	  return messages.length == 1
	      ? String.valueOf(messages[0])
	      : String.join("", Stream.of(messages)
            .map(String::valueOf)
            .reduce("", (a, b) -> a + b));
	}
	
	public static void trace(Object... messages) 
	{ 
		getCallerLogger().trace(buildMessage(messages));
	}
	
	public static void debug(Object... messages) 
	{ 
		getCallerLogger().debug(buildMessage(messages));
	}
	
	public static void info(Object... messages) 
	{ 
		getCallerLogger().info(buildMessage(messages));
	}
	
	public static void warn(Object... messages) 
	{ 
		getCallerLogger().warn(buildMessage(messages));
	}
	
	public static void error(Object... messages) 
	{ 
		getCallerLogger().error(buildMessage(messages));
	}
	
	public static void fatal(Object... messages) 
	{ 
		getCallerLogger().fatal(buildMessage(messages));
	}
	
	private static String repeat(String str, int times)
	{
	  if (times < 0) {
	    throw new RuntimeException();
	  } else if (times == 0) {
	    return "";
	  }
	  return new String(new char[times]).replace("\0", str);
	}
	
	public static void logTime(Timer timer, int level, String... info)
	{
	  if (level <= 0 || level >= MAX_LEVEL) {
	    throw new RuntimeException();
	  }
	  if (LogUtils.RUNTIME.isInfoEnabled()) {
      String header = new StringBuilder(repeat("| ", level - 1))
          .append("+-")
          .append(repeat("--", MAX_LEVEL - level))
          .toString();
      LogUtils.RUNTIME.info(String.format("%s [%8.2f seconds] %s",
          header, timer.interval(), String.join(" ", info)));
	  }
	}
	
	private static void logMethodTime(Timer timer, int level, int stackIdx)
	{
	  StackTraceElement ste = Thread.currentThread().getStackTrace()[stackIdx];
	  logTime(timer, level, ste.getClassName() + "." + ste.getMethodName() + "()");
	}
	
	public static void logMethodTime(Timer timer, int level)
	{
	  logMethodTime(timer, level, 3);
	}
	
	public static <T> T logMethodTime(int level, MethodReturnRunner<T> mt)
	{
    Timer t = new Timer();
    T result = mt.run();
	  logMethodTime(t, level, 3);
    return result;
	}
	
	public interface MethodReturnRunner<T>
	{
	   T run();
	}
	
	public static void logMethodTime(int level, MethodRunner mt)
	{
    Timer t = new Timer();
    mt.run();
	  logMethodTime(t, level, 3);
	}
	
	public interface MethodRunner
	{
	   void run();
	}
}