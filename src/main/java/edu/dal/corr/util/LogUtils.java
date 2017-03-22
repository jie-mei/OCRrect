package edu.dal.corr.util;

import java.io.IOException;
import java.util.stream.Stream;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

/**
 * @since 2016.08.10
 */
public class LogUtils
{
  public static final Logger RUNTIME = Logger.getLogger("system.runtime");
  public static final String LOG_DIR = "log";

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

	public static boolean isInfoEnabled()  { return getCallerLogger().isInfoEnabled();  }
	public static boolean isDebugEnabled() { return getCallerLogger().isDebugEnabled(); }
	public static boolean isTraceEnabled() { return getCallerLogger().isTraceEnabled(); }
	
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
	  if (level <= 0 || level > MAX_LEVEL) {
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
	  logMethodTime(timer, level, 4);
	}
	
	public static <T> T logMethodTime(int level, CodeReturnRunner<T> mt)
	{
    Timer t = new Timer();
    T result = mt.run();
	  logMethodTime(t, level);
    return result;
	}
	
	public static <T> T logMethodTimeWithIOException(
    int level,
    CodeReturnRunnerWithIOException<T> mt
  )
    throws IOException
	{
    Timer t = new Timer();
    T result = mt.run();
	  logMethodTime(t, level);
    return result;
	}

	public static <T> T logTime(int level, CodeReturnRunner<T> mt, String... info)
	{
    Timer t = new Timer();
    T result = mt.run();
	  logTime(t, level, info);
    return result;
	}

	public static <T> T logTime(String info, int level, CodeReturnRunner<T> mt)
	{
    Timer t = new Timer();
    T result = mt.run();
	  logTime(t, level, info);
    return result;
	}

	public static <T> T logToFile(Logger logger, String pathname, CodeReturnRunner<T> mt)
	{
	  FileAppender fa = newFileAppender(pathname);
	  logger.addAppender(fa);
	  T result = mt.run();
	  logger.removeAppender(fa);
    return result;
	}
	
	
	public interface CodeReturnRunnerWithIOException<T> {
	   T run() throws IOException;
	}
	
	public interface CodeReturnRunner<T> {
	   T run();
	}
	
	public static void logMethodTime(int level, CodeRunner mt)
	{
    Timer t = new Timer();
    mt.run();
	  logMethodTime(t, level, 3);
	}
	
	public interface CodeRunner {
	   void run();
	}
	
	public static void logTime(int level, CodeRunner mt, String... info)
	{
    Timer t = new Timer();
    mt.run();
	  logTime(t, level, info);
	}

  /*
   *  Utilities for logging to file.
   */
	
	private static final Logger LOG = Logger.getLogger(LogUtils.class);
	private static final Logger UNADDITIVE_LOG = Logger.getLogger(
	    LogUtils.class.getName() + ".unadditive.log");
	static {
	  UNADDITIVE_LOG.setAdditivity(false);
	}

	public static void logToFile(String pathname, boolean additivity, CodeLogRunner mt)
	{
	  Logger logger = additivity ? LOG : UNADDITIVE_LOG;
	  FileAppender fa = newFileAppender(pathname);
	  logger.addAppender(fa);
	  mt.run(logger);
	  logger.removeAppender(fa);
	}
	
	public interface CodeLogRunner
	{
	   void run(Logger logger);
	}

  public static FileAppender newFileAppender(String pathname, Layout layout)
  {
    try {
      pathname = pathname.startsWith(LOG_DIR + "/") ? pathname :
          LOG_DIR + "/" + pathname;
      FileAppender fa = new FileAppender(layout, pathname, false);
      fa.setThreshold(Level.ALL);
      return fa;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static FileAppender newFileAppender(String pathname)
  {
    return newFileAppender(pathname, new PatternLayout("%m%n"));
  }
}
