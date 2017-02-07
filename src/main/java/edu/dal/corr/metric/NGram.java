package edu.dal.corr.metric;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NGram
{
  private static char PREFIX = '\n';

  private int n;
  private CostFunction cf;
  
  public NGram(int size, CostFunction func) {
    n = size;
    cf = func;
  }
  
  public interface CostFunction {
    public float cost(char[] ngram1, char[] ngram2);
  }
  
  public static CostFunction BINARY_COST = (s1, s2) -> {
    int n = s1.length;
    for (int ni = 0; ni < n; ni++) {
      if (s1[ni] != s2[ni]) {
        return 1;
      }
    }
    return 0;
  };
  
  public static CostFunction POSITIONAL_COST = (s1, s2) -> {
    int n = s1.length;
    int tn = n;
    int cost = 0;
    for (int ni = 0; ni < n; ni++) {
      if (s1[ni] != s2[ni]) {
        cost++;
      } else if (s1[ni] == PREFIX) {
        tn--; //discount matches on prefix
      }
    }
    return (float) cost / tn;
  };

  public static CostFunction COMPREHENSIVE_COST = (s1, s2) -> {
    int n = s1.length;
    int tn = n;
    int cost = 0;
    List<Character> l2 = new ArrayList<>();
    for (char c: s2) {
      l2.add(c);
    }
    for (char c: s1) {
      if (! l2.contains(c)) {
        cost++;
      } else {
        l2.remove((Character)c);
      }
      if (c == PREFIX) {
        tn--;
      }
    }
    return (float) cost / tn;
  };

  public final double distance(final String s0, final String s1) {
    if (s0 == null) {
        throw new NullPointerException("s0 must not be null");
    }

    if (s1 == null) {
        throw new NullPointerException("s1 must not be null");
    }

    if (s0.equals(s1)) {
        return 0;
    }

    final char special = '\n';
    final int sl = s0.length();
    final int tl = s1.length();

    if (sl == 0 || tl == 0) {
        return 1;
    }

    int cost = 0;
    if (sl < n || tl < n) {
        for (int i = 0, ni = Math.min(sl, tl); i < ni; i++) {
            if (s0.charAt(i) == s1.charAt(i)) {
                cost++;
            }
        }
        return (float) cost;
    }

    char[] sa = new char[sl + n - 1];
    float[] p; //'previous' cost array, horizontally
    float[] d; // cost array, horizontally
    float[] d2; //placeholder to assist in swapping p and d

    //construct sa with prefix
    for (int i = 0; i < sa.length; i++) {
        if (i < n - 1) {
            sa[i] = special; //add prefix
        } else {
            sa[i] = s0.charAt(i - n + 1);
        }
    }
    p = new float[sl + 1];
    d = new float[sl + 1];

    // indexes into strings s and t
    int i; // iterates through source
    int j; // iterates through target

    char[] t_j = new char[n]; // jth n-gram of t

    for (i = 0; i <= sl; i++) {
        p[i] = i;
    }

    for (j = 1; j <= tl; j++) {
        //construct t_j n-gram
        if (j < n) {
            for (int ti = 0; ti < n - j; ti++) {
                t_j[ti] = special; //add prefix
            }
            for (int ti = n - j; ti < n; ti++) {
                t_j[ti] = s1.charAt(ti - (n - j));
            }
        } else {
            t_j = s1.substring(j - n, j).toCharArray();
        }
        d[0] = j;
        for (i = 1; i <= sl; i++) {
            float ec = cf.cost(Arrays.copyOfRange(sa, i - 1, i - 1 + n), t_j);
            
            // minimum of cell to the left+1, to the top+1,
            // diagonally left and up +cost
            d[i] = Math.min(
                    Math.min(d[i - 1] + 1, p[i] + 1), p[i - 1] + ec);
        }
        // copy current distance counts to 'previous row' distance counts
        d2 = p;
        p = d;
        d = d2;
    }

    // our last action in the above loop was to switch d and p, so p now
    // actually has the most recent cost counts
    return p[sl];
  }
}
