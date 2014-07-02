package sudoku;

/**
 * @author Aditya Pal
 * @Source n1b-algo.blogspot.com
 * 
 * Please contact me at n1b.algo.blog@gmail.com, if you wish to 
 * use this program as is (or modify it) for commercial purposes.
 */
final class Cell {

	Sudoku S;
	CellBlock ROW, COL, CUBE;

	int val;
	int len = Sudoku.N;
	boolean hints[] = new boolean[1 + Sudoku.N];

	/**
	 * Creates a new cell. If h is 0, then cell
	 * has hints, otherwise not.
	 * @param val
	 */
	Cell(int val) {
		if (val != 0) {
			this.val = val;
			// we deliberately let len=0, so that initFix can
			// fix it, setting len=1 would make solution slow,
			// as the hints wont get removed from shared cells.
			this.len = 0;
		} else {
			for (int h = 1; h <= Sudoku.N; ++h)
				hints[h] = true;
		}
	}

	/**
	 * Initializes a cell through another cell.
	 * @param c
	 */
	Cell(Cell c) {
		reset(c);
	}

	/**
	 * Reset a cell by copying status of another cell.
	 * @param c
	 */
	void reset(Cell c) {
		this.val = c.val;
		this.len = c.len;
		if (len > 1) // no point in copying hints for len = 1
			for (int i = 1; i <= Sudoku.N; ++i)
				hints[i] = c.hints[i];
	}

	/**
	 * Internal function to fix cell initially.
	 * @return
	 */
	boolean fastFix() { 
		len = 1;
		S.solved++;
		return updateBlockHints();
	}


	/**
	 * Fixes a cell.
	 * @param val
	 * @return true if fixed, false on error.
	 */
	boolean fix(int val) {
		if (this.val != 0) { // cell is assigned a val
			if (this.val != val) return false; // trying to assign a different val ... ERROR
			return true;
		}

		// fix cell
		this.val = val;
		this.len = 1;

		// book-keeping
		S.solved++;
		return updateBlockHints();
	}

	/**
	 * Removes a hint
	 * @param h
	 * @return true if success, false on error
	 */
	boolean remove(int h) {
		if (val != 0 || !hints[h]) { // cell already fixed or hint h not present
			if (val == h) return false; // trying to remove the fixed val - ERROR
			return true;
		}

		// remove hint hlen
		hints[h] = false;
		len--;

		if (len > 1) {
			// book-keeping
			S.updates++;
			ROW.cellUpdated = true;
			COL.cellUpdated = true;
			CUBE.cellUpdated = true;
			return true;
		} else { // only element left .. fix it
			for (int h1 = 1; h1 <= Sudoku.N; ++h1) {
				if (hints[h1]) 
					return fix(h1);
			}
			return false;
		}
	}

	/**
	 * Removes two hints.
	 * @param h1
	 * @param h2
	 * @return true if success, false on error
	 */
	boolean remove(int h1, int h2) {
		if (val != 0) { // cell already fixed (no point in removing hint)
			if (val == h1 || val == h2) return false; // trying to remove the fixed val - ERROR
			return true;
		}

		int l = len;
		if (hints[h1]) {
			hints[h1] = false;
			len--;
		}
		if (hints[h2]) {
			hints[h2] = false;
			len--;
		}

		if (l != len) { // atleast one hint removed
			if (len > 1) {
				// book-keeping
				S.updates++;
				ROW.cellUpdated = true;
				COL.cellUpdated = true;
				CUBE.cellUpdated = true;
				return true;
			} else { // only element left .. fix it
				for (int h = 1; h <= Sudoku.N; ++h) {
					if (hints[h]) 
						return fix(h);
				}
				return false;
			}
		}
		return true;
	}

	/**
	 * Remove val from other cells in the same block as this cell.
	 * @return
	 */
	private boolean updateBlockHints() {
		ROW.cellUpdated = true;
		COL.cellUpdated = true;
		CUBE.cellUpdated = true;
		if (ROW.updateFixed(this) && COL.updateFixed(this) && CUBE.updateFixed(this))
			return true;
		else
			return false;
	}

	String toString(boolean showHints) {
		StringBuilder sb = new StringBuilder();
		if (val != 0)
			sb.append(val);
		else if (len == 0)
			sb.append("X");
		else {
			if (!showHints) 
				sb.append("*");
			else {
				for (int i = 1; i <= Sudoku.N; ++i) {
					if (hints[i])
						sb.append(i);
				}
			}
		} 
		return sb.toString();
	}
}