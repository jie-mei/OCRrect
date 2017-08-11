package edu.dal.ocrrect.expr.suggest;

import edu.dal.ocrrect.expr.ExprUtils;
import edu.dal.ocrrect.util.ResourceUtils;

import java.nio.file.Path;

public class SuggestConstants {
  public static int SUGGEST_TOP_NUM = 100;
  public static Path OUTPUT_PATH = ExprUtils.TEMP_DIR.resolve("suggest");
  public static Path DATA_PATH = OUTPUT_PATH.resolve("data");

  public static Path ERROR_GT_PATH = ResourceUtils.getResource("mibio-ocr/error.gt.tsv");

  // TSV preparation.
  public static Path TRAIN_WORDS_MAPPED_TSV_PATH = DATA_PATH.resolve("words.train.mapped.tsv");
  public static Path TRAIN_CORRS_MAPPED_TSV_PATH = DATA_PATH.resolve("corrs.train.mapped.tsv");
  public static Path TRAIN_WORDS_MAPPED_IDENTICAL_TSV_PATH = DATA_PATH.resolve("words.train.mapped.identical.tsv");
  public static Path TRAIN_CORRS_MAPPED_IDENTICAL_TSV_PATH = DATA_PATH.resolve("corrs.train.mapped.identical.tsv");
  public static Path TEST_WORDS_MAPPED_TSV_PATH = DATA_PATH.resolve("words.test.tsv");
  public static Path TEST_CORRS_MAPPED_TSV_PATH = DATA_PATH.resolve("corrs.test.tsv");

  // Train suggestion generation.
  public static Path TRAIN_BINARY_PATH = DATA_PATH.resolve("suggest.train.top" + SUGGEST_TOP_NUM);
  public static Path TEST_BINARY_PATH = DATA_PATH.resolve("suggest.test.top" + SUGGEST_TOP_NUM);
  public static Path TRAIN_BINARY_TOP100_PATH = DATA_PATH.resolve("suggest.train.top100");
  public static Path TRAIN_BINARY_TOP10_PATH  = DATA_PATH.resolve("suggest.train.top10");
  public static Path TRAIN_BINARY_TOP5_PATH   = DATA_PATH.resolve("suggest.train.top5");
  public static Path TRAIN_BINARY_TOP3_PATH   = DATA_PATH.resolve("suggest.train.top3");
  public static Path TRAIN_BINARY_TOP1_PATH   = DATA_PATH.resolve("suggest.train.top1");
  public static Path TEST_BINARY_TOP100_PATH = DATA_PATH.resolve("suggest.test.top100");
  public static Path TEST_BINARY_TOP10_PATH  = DATA_PATH.resolve("suggest.test.top10");
  public static Path TEST_BINARY_TOP5_PATH   = DATA_PATH.resolve("suggest.test.top5");
  public static Path TEST_BINARY_TOP3_PATH   = DATA_PATH.resolve("suggest.test.top3");
  public static Path TEST_BINARY_TOP1_PATH   = DATA_PATH.resolve("suggest.test.top1");

  // Text suggestion conversion.
  public static Path TRAIN_SUGGESTS_MAPPED_TOP10_TEXT_PATH = DATA_PATH.resolve("suggests.train.mapped.top10.txt");
  public static Path TRAIN_SUGGESTS_MAPPED_TOP5_TEXT_PATH  = DATA_PATH.resolve("suggests.train.mapped.top5.txt");
  public static Path TRAIN_SUGGESTS_MAPPED_TOP3_TEXT_PATH  = DATA_PATH.resolve("suggests.train.mapped.top3.txt");
  public static Path TRAIN_SUGGESTS_MAPPED_TOP1_TEXT_PATH  = DATA_PATH.resolve("suggests.train.mapped.top1.txt");
  public static Path TRAIN_SUGGESTS_MAPPED_IDENTICAL_TOP10_TEXT_PATH = DATA_PATH.resolve("suggests.train.mapped.identical.top10.txt");
  public static Path TRAIN_SUGGESTS_MAPPED_IDENTICAL_TOP5_TEXT_PATH  = DATA_PATH.resolve("suggests.train.mapped.identical.top5.txt");
  public static Path TRAIN_SUGGESTS_MAPPED_IDENTICAL_TOP3_TEXT_PATH  = DATA_PATH.resolve("suggests.train.mapped.identical.top3.txt");
  public static Path TRAIN_SUGGESTS_MAPPED_IDENTICAL_TOP1_TEXT_PATH  = DATA_PATH.resolve("suggests.train.mapped.identical.top1.txt");
  public static Path TEST_SUGGESTS_TOP10_TEXT_PATH = DATA_PATH.resolve("suggests.test.top10.txt");
  public static Path TEST_SUGGESTS_TOP5_TEXT_PATH  = DATA_PATH.resolve("suggests.test.top5.txt");
  public static Path TEST_SUGGESTS_TOP3_TEXT_PATH  = DATA_PATH.resolve("suggests.test.top3.txt");
  public static Path TEST_SUGGESTS_TOP1_TEXT_PATH  = DATA_PATH.resolve("suggests.test.top1.txt");

  // TSV suggestion conversion.
  public static Path TRAIN_SUGGESTS_MAPPED_TOP10_TSV_PATH = DATA_PATH.resolve("suggests.train.mapped.top10.tsv");
  public static Path TRAIN_LABELS_MAPPED_TOP10_TSV_PATH = DATA_PATH.resolve("labels.train.mapped.top10.tsv");
  public static Path TRAIN_SUGGESTS_MAPPED_IDENTICAL_TOP10_TSV_PATH = DATA_PATH.resolve("suggests.train.mapped.identical.top10.tsv");
  public static Path TRAIN_LABELS_MAPPED_IDENTICAL_TOP10_TSV_PATH = DATA_PATH.resolve("labels.train.mapped.identical.top10.tsv");
}
