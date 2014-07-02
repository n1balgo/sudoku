package sudoku;

import java.util.ArrayList;

/**
 * @author Aditya Pal
 * @Source n1b-algo.blogspot.com
 * 
 * Please contact me at n1b.algo.blog@gmail.com, if you wish to 
 * use this program as is (or modify it) for commercial purposes.
 */
final class CellBlock {

  private int k=0;
  private Cell cells[] = new Cell[Sudoku.N];

  // indicates if a cell is changed
  boolean cellUpdated = true;

  @SuppressWarnings("unchecked")
  private ArrayList<Cell>[] refMap = new ArrayList[1 + Sudoku.N];

  /**
   * Initialize a cell block.
   */
  CellBlock() {
    for (int h = 1; h <= Sudoku.N; ++h)
      refMap[h] = new ArrayList<Cell>();
  }

  /**
   * Add a cell to this cell block.
   * @param c
   */
  void addCell(Cell c) {
    cells[k] = c;
    ++k;
  }

  /**
   * Remove fixed cell's value from hint of other cells.
   * @param A
   * @return
   */
  boolean updateFixed(Cell A) {
    for (Cell c : cells) {
      if (c != A && !c.remove(A.val)) return false;
    }
    return true;
  }

  /**
   * Cell is fixed if it uniquely refers a hint (not 
   * referred by others in this block). 
   */
  boolean fixUniqueRefs() {
    checkRefMap();
    for (int h = 1; h <= Sudoku.N; ++h) {
      ArrayList<Cell> l = refMap[h];
      if (l.size() == 1 && !l.get(0).fix(h)) return false;
    }
    return true;
  }

  /**
   * Cell hints are pruned if they bi-refer two hints.
   * @return
   */
  boolean fixTwoRefs() {
    checkRefMap();
    for (int h1 = 1; h1 < Sudoku.N; ++h1) {
      ArrayList<Cell> l1 = refMap[h1];
      if (l1.size() != 2) continue; // hint referred by more than 2 cells

      Cell c1 = l1.get(0);
      Cell c2 = l1.get(1);
      if (c1.len == 2 && c2.len == 2) continue; // nothing to do here.

      for (int h2 = h1 + 1; h2 <= Sudoku.N; ++h2) {
        ArrayList<Cell> l2 = refMap[h2];
        if (l2.size() != 2 || !l1.containsAll(l2)) continue; // not compatible with h1

        // remove other hints from the cells in l1 (leaving only n1 and n2).
        for (int h = 1; h <= Sudoku.N; ++h) {
          if (h != h1 && h != h2)
            if (!c1.remove(h) || !c2.remove(h)) return false;
        }

        // remove hints from blocks that are common to these two cells
        if (c1.ROW == c2.ROW && !c1.ROW.removeTwoHints(c1, c2, h1, h2))
          return false;

        if (c1.COL == c2.COL && !c1.COL.removeTwoHints(c1, c2, h1, h2))
          return false;

        if (c1.CUBE == c2.CUBE && !c1.CUBE.removeTwoHints(c1, c2, h1, h2))
          return false;
        break;
      }
    }
    return true;
  }

  /**
   * Cell hints are pruned if they bi-refer two hints.
   * @return
   */
  boolean fixTwoRefsCells() {
    checkRefMap();
    for (int i = 0; i < cells.length-1; ++i) {
      Cell c1 = cells[i];
      if (c1.len != 2)
        continue;

      for (int j = i+1; j < cells.length; ++j) {
        Cell c2 = cells[j];
        if (c2.len != 2)
          continue;

        boolean match = true;
        int h1 = -1, h2 = -1;
        for (int h = 1; h <= Sudoku.N; ++h) {
          if (c1.hints[h] != c2.hints[h]) {
            match = false;
            break;
          }
          if (c1.hints[h]) {
            if (h1 == -1) 
              h1 = h;
            else 
              h2 = h;
          }
        }

        if (match) {
          // remove hints from blocks that are common to these two cells
          if (c1.ROW == c2.ROW && !c1.ROW.removeTwoHints(c1, c2, h1, h2))
            return false;

          if (c1.COL == c2.COL && !c1.COL.removeTwoHints(c1, c2, h1, h2))
            return false;

          if (c1.CUBE == c2.CUBE && !c1.CUBE.removeTwoHints(c1, c2, h1, h2))
            return false;
          break;
        }
      }
    }
    return true;
  }

  private boolean removeTwoHints(Cell A, Cell B, int h1, int h2) {
    for (Cell c : cells) {
      if (c != A && c != B && !c.remove(h1, h2))
        return false;
    }
    return true;
  }

  private void checkRefMap() {
    if (!cellUpdated) return;
    for (int h = 1; h <= Sudoku.N; ++h)
      refMap[h].clear();

    for (Cell c : cells) {
      if (c.val != 0) continue;
      for (int h = 1; h <= Sudoku.N; ++h)
        if (c.hints[h])
          refMap[h].add(c);
    }
    cellUpdated = false;
  }
}