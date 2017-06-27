package edu.dal.ocrrect.expr;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ExprUtils {
  public static Path TEMP_DIR = Paths.get("tmp");

  public static void ensureTempPath() {
    try {
      Files.createDirectories(TEMP_DIR);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
