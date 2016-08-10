package edu.dal.corr.word;

import java.util.regex.Pattern;

import edu.dal.corr.util.Unigram;

public class HuristicWordFilter
  implements WordFilter
{
  private static Pattern VALID_PATTERN = Pattern.compile("[^a-zA-Z]+");
  private static Unigram UNIGRAM = Unigram.getInstance();

  @Override
  public boolean filter(Word word)
  {
    String name = word.text();
    if(name.length() <= 1 || isRomanNumber(name)
        || VALID_PATTERN.matcher(name).matches()) {
      return true;
    } else {
      return UNIGRAM.freq(name) >= freqThreshold(name);
    }
  }

  private int freqThreshold(String word) {
    int threshold;
    switch (word.length()) {
      case 1:  threshold = 0; break;
      case 2:  threshold = 10000000; break;
      case 3:  threshold = 1000000; break;
      case 4:  threshold = 100000; break;
      case 5:  threshold = 100000; break;
      case 6:  threshold = 10000; break;
      case 7:  threshold = 10000; break;
      case 8:  threshold = 10000; break;
      case 9:  threshold = 10000; break;
      case 10: threshold = 10000; break;
      case 11: threshold = 1000; break;
      case 12: threshold = 1000; break;
      case 13: threshold = 1000; break;
      case 14: threshold = 1000; break;
      case 15: threshold = 1000; break;
      case 16: threshold = 1000; break;
      default: threshold = 200;
    }
    return threshold;
  }
  
  private boolean isRomanNumber(String word)
  {
    boolean romanflag = false;
    switch(word) {
      case "i":
      case "ii":
      case "iii":
      case "iv":
      case "v":
      case "vi":
      case "vii":
      case "viii":
      case "ix":
      case "x":
      case "xi":
      case "xii":
      case "xiii":
      case "xiv":
      case "xv":
      case "xvi":
      case "xvii":
      case "xviii":
      case "xix":
        romanflag = true;
        break;
      default:
        romanflag = false;
    }
    return romanflag;
  }
}
