package sudoku;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * @author Aditya Pal
 * @Source n1b-algo.blogspot.com
 * 
 * Please contact me at n1b.algo.blog@gmail.com, if you wish to 
 * use this program as is (or modify it) for commercial purposes.
 */
public class Main {

  public static void main(String args[]) throws IOException {
    Sudoku.SIZE(1);
    solve(new Sudoku(getArrFromStr(p1)));

    Sudoku.SIZE(4);
    solve(new Sudoku(getArrFromStr(p4)));

    Sudoku.SIZE(9);
    solve(new Sudoku(getArrFromStr(p9)));

    Sudoku.SIZE(16);
    solve(new Sudoku(p16));

    Sudoku.SIZE(9);
    testAccuracyAndSpeed(500, "./src/sudoku/hardest.txt");
    testAccuracyAndSpeed(500, "./src/sudoku/hard.txt");
    testAccuracyAndSpeed(500, "./src/sudoku/easy.txt");
  }

  static String p1 = ".";

  static String p4 = "4.2...411.....14";

  static String p9 = 
      "...7..6.37.39...12...53....1.......79.........45....963.....9.......1.65.1.36..74";

  static int[] p16 = new int[] {
    8, 7, 0, 0, 0, 0, 0, 0, 0, 3, 0, 0, 13, 0, 4, 0, 
    0, 5, 14, 0, 0, 0, 3, 10, 15, 9, 1, 0, 0, 6, 0, 0, 
    16, 0, 0, 0, 5, 8, 7, 0, 0, 14, 0, 0, 9, 0, 11, 12, 
    0, 0, 4, 0, 0, 14, 6, 13, 0, 11, 10, 12, 0, 7, 0, 3,
    14, 0, 0, 8, 0, 0, 1, 0, 0, 0, 0, 3, 7, 4, 12, 0, 
    9, 0, 0, 0, 0, 6, 15, 12, 0, 0, 13, 14, 0, 3, 1, 0, 
    11, 0, 10, 3, 0, 0, 13, 0, 0, 8, 0, 1, 0, 0, 6, 0, 
    6, 0, 0, 1, 14, 0, 4, 0, 0, 5, 0, 9, 11, 0, 0, 13,
    0, 0, 0, 0, 15, 0, 0, 0, 0, 0, 9, 0, 5, 0, 2, 10, 
    10, 1, 0, 0, 6, 0, 5, 0, 13, 15, 7, 16, 0, 0, 0, 0,
    0, 0, 16, 11, 0, 4, 0, 8, 2, 0, 0, 0, 0, 13, 0, 7,
    0, 9, 0, 7, 1, 3, 0, 2, 6, 0, 8, 10, 16, 15, 14, 4,
    7, 0, 13, 0, 9, 16, 0, 5, 0, 0, 14, 4, 3, 8, 0, 2,
    0, 0, 3, 0, 10, 0, 0, 0, 0, 0, 0, 0, 0, 16, 15, 0, 
    1, 0, 9, 0, 0, 0, 14, 4, 0, 0, 0, 0, 0, 0, 7, 0,
    0, 6, 8, 0, 3, 0, 0, 0, 10, 7, 0, 0, 0, 0, 0, 0,
  };

  static String p9_imp = 
      ".....5.8....6.1.43..........1.5........1.6...3.......553.....61........4.........";

  static void testAccuracyAndSpeed(double maxIter, String fname) throws IOException {
    long solved=0, n=0;
    long finSolv = 0, expSolv = 0, inpSolv = 0;
    long inpHint = 0, finHint = 0;
    double ttime=0, t0, t1;
    for (long iter=0; iter<maxIter; ++iter) {
      BufferedReader br = new BufferedReader(new FileReader(fname));
      String line;
      while ((line = br.readLine()) != null) {
        Sudoku S = new Sudoku(getArrFromStr(line));
        int[] a = S.numSolvedAndhints();
        inpSolv += a[0];
        inpHint += a[1];

        // solve Sudoku and record time
        t0 = System.nanoTime();
        Sudoku S1 = Sudoku.solve(S);
        t1 = System.nanoTime();

        // update vars
        ttime += (t1-t0);
        ++n;
        if (S1 != null) {
          int[] b = S1.numSolvedAndhints();
          finSolv += b[0];
          finHint += b[1];
          expSolv += 81;
        }
        if (S1 != null && S1.solved == 81) ++solved;
        //else System.out.println(line); // failed for this problem
      }
      br.close();
    }
    ttime = ttime/1000000.0; // ns to ms
    solved /= maxIter; n /= maxIter; 
    inpSolv /= maxIter; finSolv /= maxIter; expSolv /= maxIter;
    inpHint /= maxIter; finHint /= maxIter;
    ttime /= maxIter;
    System.out.println("\nSolving all puzzles from: " + fname);
    System.out.println("Solved       :  " + solved + "/" + n);
    System.out.println("Solved Cells :  " + inpSolv + "(input) " + finSolv + "(finally) " + expSolv + "(expected)");
    System.out.println("Cell Hints   :  " + inpHint + "(input) " + finHint + "(finally) ");
    System.out.println("Time Taken   :  " + ttime + " milli-seconds");
    System.out.println("Avg time     :  " + (ttime/n) + " milli-seconds");
    //System.out.println(inpSolv/(double)expSolv);
    //System.out.println(finSolv/(double)expSolv);
    //System.out.println(inpHint/(double)n);
    //System.out.println(finHint/(double)n);
  }

  static void solve(Sudoku S0) {
    System.out.println("\nSolving a Sudoku puzzle of size " + Sudoku.N + " ...");
    S0.print(false); // input

    long t0 = System.nanoTime();
    Sudoku S1 = Sudoku.solve(S0);
    long t1 = System.nanoTime();
    if (S1 == null)
      System.out.println("NOT SOLVABLE");
    else 
      S1.print(true); // output
    System.out.println("Time taken=" + (t1-t0)/1000000.0 + " ms");
  }

  static int[] getArrFromStr(String S) {
    int[] M = new int[S.length()];
    for (int n=0; n<S.length(); ++n) {
      char c = S.charAt(n);
      if (c == '.') M[n] = 0;
      else M[n] = c-'0';
    }
    return M;
  }
}