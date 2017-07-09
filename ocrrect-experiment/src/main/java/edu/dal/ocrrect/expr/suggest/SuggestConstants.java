package edu.dal.ocrrect.expr.suggest;

import edu.dal.ocrrect.expr.ExprUtils;

import java.nio.file.Path;

public class SuggestConstants {
  public static int SUGGEST_TOP_NUM = 100;
  public static Path OUTPUT_PATH = ExprUtils.TEMP_DIR.resolve("suggest");
  public static Path DATA_PATH = OUTPUT_PATH.resolve("data");

  // TSV preparation.
  public static Path TRAIN_WORDS_MAPPED_TSV_PATH = DATA_PATH.resolve("words.train.mapped.tsv");
  public static Path TRAIN_CORRS_MAPPED_TSV_PATH = DATA_PATH.resolve("corrs.train.mapped.tsv");
  public static Path TRAIN_WORDS_MAPPED_IDENTICAL_TSV_PATH = DATA_PATH.resolve("words.train.mapped.identical.tsv");
  public static Path TRAIN_CORRS_MAPPED_IDENTICAL_TSV_PATH = DATA_PATH.resolve("corrs.train.mapped.identical.tsv");
  public static Path TEST_WORDS_MAPPED_TSV_PATH = DATA_PATH.resolve("words.test.mapped.tsv");
  public static Path TEST_CORRS_MAPPED_TSV_PATH = DATA_PATH.resolve("corrs.test.mapped.tsv");

  // Train suggestion generation.
  public static Path TRAIN_BINARY_PATH = DATA_PATH.resolve("suggest.train.top" + SUGGEST_TOP_NUM);
  public static Path TEST_BINARY_PATH = DATA_PATH.resolve("suggest.test.top" + SUGGEST_TOP_NUM);

  // TSV suggestion conversion.
  public static Path TRAIN_SUGGESTS_MAPPED_TSV_PATH = DATA_PATH.resolve("suggests.train.mapped.tsv");
  public static Path TRAIN_LABELS_MAPPED_TSV_PATH = DATA_PATH.resolve("labels.train.mapped.tsv");
  public static Path TRAIN_SUGGESTS_MAPPED_IDENTICAL_TSV_PATH = DATA_PATH.resolve("suggests.train.mapped.identical.tsv");
  public static Path TRAIN_LABELS_MAPPED_IDENTICAL_TSV_PATH = DATA_PATH.resolve("labels.train.mapped.identical.tsv");
}
