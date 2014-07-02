package sudoku;

/**
 * @author Aditya Pal
 * @Source n1b-algo.blogspot.com
 * 
 * Please contact me at n1b.algo.blog@gmail.com, if you wish to 
 * use this program as is (or modify it) for commercial purposes.
 */
public class Sudoku {

	/**
	 * Set size of Sudoku through this. The size n must be a perfect square.
	 */
	public static void SIZE(int n) {
		N_2 = (int) Math.sqrt(n);
		N = N_2 * N_2;
		N2 = N * N;

		CUBE = new int[N2];
		for (int k = 0; k < N2; ++k)
			CUBE[k] = (int) (N_2 * Math.floor(k/(N * N_2)) + Math.floor((k%N)/N_2));
	}

	/**
	 * Solves input Sudoku.
	 * @param S
	 * @return Solved Sudoku, Null if no solution possible.
	 */
	public static Sudoku solve(Sudoku S) {
		if (!S.initFix() || !S.simplify()) return null; // not solvable
		if (S.solved == N2) return S; // solved
		return bruteForceSolve(S); // try brute force
	}

	/**
	 * Brute-Force solver of Sudoku.
	 * @param S
	 * @return
	 */
	public static Sudoku bruteForceSolve(Sudoku S) {
		// find cell with least hints
		int minIdx = 0, minLen = N + 1;
		for (int n = 0; n < N2; ++n) {
			int l = S.M[n].len;
			if (l > 1 && minLen > l) {
				minLen = l;
				minIdx = n;
			}
		}
		boolean[] hints = S.M[minIdx].hints;

		// try these hints on a scratch sudoku's cells
		Sudoku S1 = new Sudoku(S); // get a temp copy
		Cell c = S1.M[minIdx];
		for (int h = 1; h<= N; ++h) {
			if (!hints[h]) continue;

			if (c.fix(h) && S1.simplify()) {
				if (S1.solved == N2) return S1;

				Sudoku S2 = bruteForceSolve(S1);
				if (S2 != null) return S2;
			}

			S1.reset(S);
		}
		return null;
	}

	/**
	 * Simplifies Sudoku by three main algorithms
	 * 1. Numbers that are hints in one cell (in a given block) is assigned that cell.
	 * 2. Numbers that are hints in two cells of a block are assigned those two hints. 
	 * 3. Two cells that refer to two numbers are assigned those two cells.
	 * @return
	 */
	public boolean simplify() {
		int n = 0, m = solved + updates;
		while (solved != N2 && n != m) {
			for (CellBlock cb : B) { // run algos once atleast.
				//if (!cb.fixUniqueRefs() || !cb.fixTwoRefs()) return false;
				if (!cb.fixUniqueRefs() || !cb.fixTwoRefs() || !cb.fixTwoRefsCells())
					return false;
			}
			n = m;
			m = solved + updates;
		}
		return true;
	}

	/**
	 * This must be called once while initializing the 
	 * Sudoku. If a Sudoku is reset, there is no need
	 * to call this method.
	 * @return
	 */
	public boolean initFix() {
		for (Cell c : M) {
			if (c.len == 0 && !c.fastFix()) return false;
		}
		return true;
	}

	int updates = 0;
	int solved = 0;
	private Cell[] M = new Cell[N2];
	private CellBlock[] B = new CellBlock[3 * N];

	/**
	 * Creates a new Sudoku and initializes it through 
	 * input cell values. Value "0" means not-assigned.
	 * @param M0
	 */
	public Sudoku(int[] M0) {
		for (int n = 0; n < N2; ++n)
			M[n] = new Cell(M0[n]);
		init();
	}

	/**
	 * Creates a new Sudoku exactly like input Sudoku.
	 * @param S
	 */
	public Sudoku(Sudoku S) {
		solved = S.solved;
		updates = S.updates;
		for (int n = 0; n < N2; ++n)
			M[n] = new Cell(S.M[n]);
		init();
	}

	/**
	 * Initializes Cell block and registers them with cells.
	 * Must be called with Constructor only.
	 */
	private void init() {
		for (int i=0; i<B.length; ++i) // create cell blocks
			B[i] = new CellBlock();

		for (int n = 0; n < N2; ++n) { // register
			int idxCube = CUBE[n];
			int idxRow = n / N;
			int idxCol = n + 2 * N - N * idxRow;
			idxRow += N;

			// register parents with cell
			M[n].S = this;
			M[n].CUBE = B[idxCube];
			M[n].ROW = B[idxRow];
			M[n].COL = B[idxCol];

			// register cell with blocks
			B[idxCube].addCell(M[n]);
			B[idxRow].addCell(M[n]);
			B[idxCol].addCell(M[n]);
		}
	}

	/**
	 * Copies state of this Sudoku from input Sudoku
	 * @param S
	 */
	private void reset(Sudoku S) {
		solved = S.solved;
		updates = S.updates;
		for (int n = 0; n < N2; ++n) // reset cells
			M[n].reset(S.M[n]);

		for (CellBlock cb : B) // mark cell block dirty
			cb.cellUpdated = true;
	}

	/**
	 * Checks if Sudoku is solved or not. For this check, row sum and 
	 * col sum must be N*(N+1)/2. Also all cells must be assigned a value.
	 * @return
	 */
	public boolean isSolved() {
		int t = (N2 + N)/2;
		int s = 0;
		for (int n = 0; n < N2; ++n) { // check row sum
			if (M[n].len != 1) return false; // cell must have one value
			s += M[n].val;
			if ((n+1)%N == 0) { // row ends
				if (s != t) return false;
				s = 0;
			}
		}

		for (int j = 0; j < N; ++j) { //ensure col sum = 45
			s = 0;
			for (int i = 0; i < N; ++i)
				s += M[N * i + j].val;
			if (s != t) return false;
		}
		return true;
	}

	/**
	 * Pretty print of Sudoku.
	 */
	public void print(boolean showHints) {
		boolean isSolved = (solved == N2);
		if (isSolved)
			showHints = false;
		
		String sep = getSep(!showHints);
		int hz = hintSize(!showHints);
		for (int n = 0; n < N2; ++n) {
			if (n%(N * N_2) == 0) // show row separator (after three rows) 
				System.out.println(sep);

			if (n%N_2 == 0) // block separator 
				System.out.print("| ");

			// print cell data
			String s = M[n].toString(showHints);
			System.out.print(s);
			for (int i=s.length(); i<hz; ++i) 
				System.out.print(' ');
			System.out.print(" ");

			if ((n+1) % N == 0) // show end of a row separator
				System.out.println("|");
		}

		System.out.println(sep);
		
		if (isSolved) 
			System.out.println("[status] SOLVED");
		else {
			int n[] = numSolvedAndhints();
			System.out.println("[status] " + "solved=" + n[0] + " hints=" + n[1]);
		}
	}

	/**
	 * Computes number of fixed cells and hints in the Sudoku.
	 * The first element of arr is fixed num and second is #hints. 
	 * @return
	 */
	int[] numSolvedAndhints() {
		int s=0, h=0;
		for (Cell c : M)
			if (c.val != 0) ++s;
			else h += c.len;
		return new int[] {s, h};
	}

	private static String getSep(boolean isSolved) {
		String SEP = "=";
		int n = 1 + N_2 + hintSize(isSolved) * N_2;

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < n; ++i)
			sb.append(SEP);
		String s = sb.toString();

		StringBuilder sb1 = new StringBuilder();
		for (int i = 0; i < N_2; ++i) {
			sb1.append(" ");
			sb1.append(s);
		}
		return sb1.toString();
	}

	private static int hintSize(boolean isSolved) {
		if (isSolved) {
			if (N > 9) return 2;
			else return 1;
		} else {
			if (N > 9) return 2*N - 9;
			else return N;
		}
	}

	static int N;
	private static int N2;
	private static int N_2;
	private static int[] CUBE;
}