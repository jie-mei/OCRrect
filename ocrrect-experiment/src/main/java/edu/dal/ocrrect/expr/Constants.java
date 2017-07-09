package edu.dal.ocrrect.expr;

import edu.dal.ocrrect.suggest.Suggestion;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Constants {

  public static Path DATA_PATH = Paths.get("data");

  // word that position smaller than this is used for training
  public static final int SPLIT_POS = 407369;
}
