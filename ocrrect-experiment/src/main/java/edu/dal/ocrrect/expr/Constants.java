package edu.dal.ocrrect.expr;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Constants {

  public static Path SUGGEST_PATH = ExprUtils.TEMP_DIR.resolve("suggest");
  public static Path SUGGEST_DATA_PATH = ExprUtils.TEMP_DIR.resolve("suggest");

  // word that position smaller than this is used for training
  public static final int SPLIT_POS = 407369;
}
