package com.company;

import java.util.ArrayList;
import java.util.concurrent.RejectedExecutionException;

import static com.company.Board.boardSize;
import static com.company.Board.tasks;

/**
 * Solves given board, a board may have only one solver and a solver can only have one board active
 */
public class PuzzleSolver implements Runnable {
	Board  board;
	String pos   = "";
	int    guess = 0;

	/**
	 * @param board board to solve
	 */
	public PuzzleSolver(Board board) {
		this.board = board;
	}

	/**
	 * Used when starting new boards from existing ones including a guess
	 * @param board board which to solve
	 * @param pos cell to which guess is assigned
	 * @param guess number which is assigned to pos
	 * @param importBoard board from which to copy into own [Puzzlesolver's] board
	 */
	public PuzzleSolver(Board board, String pos, int guess, Board importBoard) {
		this.board = board;
		this.pos   = pos;
		this.guess = guess;
		this.board.importBetter(importBoard.exportBetter());
	}

	/**
	 * Used by {@link #run} to find all indices of given number
	 * @param list list in which to find all indices of lookup
	 * @param lookup number for which to look for
	 * @return a List of integers
	 */
	private static ArrayList<Integer> getAllIndices(ArrayList<Integer> list, int lookup) {
		ArrayList<Integer> temp = new ArrayList<>();
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i) == lookup) {
				temp.add(i);
			}
		}
		return temp;
	}

	/**
	 * Main algorithm used for solving boards, method run is overridden from Thread
	 * <p>Algorithm works by iterating over tasks and
	 * <ol>
	 *     <li>Looking for all simple moves in tasks such as 1 or boardSize which indicate the next cell is either
	 *     a 1 or the entire row is [1, boardSize] and values are assigned to each cell accordingly</li>
	 *     <li>Eliminating every number in [x - j - 1, boardSize] where x is the task on the row it is affected by
	 *     and j the distance from the task to the cell (starting at 0 from nearest cell to task) from the
	 *     following x - 1 cells</li>
	 *     <li>Checking the board and, if the board is incomplete, starting as many new boards as the best guess
	 *     has choices (for example, if we ran into a board which has 2 cells which both can contain a 1 and a 2
	 *     this step would create 2 new boards where on one board the cell has a 1 and on the other board the cell
	 *     has a 2)</li>
	 * </ol>
	 */
	public void run() {
		board.inProgress = true;
//		System.out.println(Thread.currentThread().getName());
		// Check for guess
		if (guess != 0) {
			board.assign(pos, guess);
		}
		// Default moves, step 1
		if (tasks.get("Top").contains(1)) {
			ArrayList<Integer> indexList = getAllIndices(tasks.get("Top"), 1);
			// If our clue is 1 then from the next cell remove all choices but boardSize
			for (Integer i : indexList) {
				board.assign("A" + i, boardSize);
			}
		}
		if (tasks.get("Top").contains(boardSize)) {
			ArrayList<Integer> indexList = getAllIndices(tasks.get("Top"), boardSize);
			// If our clue is boardSize then remove all choices but 1
			// And complete column
			for (Integer i : indexList) {
				for (int j = 1; j <= boardSize; j++) {
					board.assign((char) (64 + j) + Integer.toString(i), j);
				}
			}
		}


		if (tasks.get("Bottom").contains(1)) {
			ArrayList<Integer> indexList = getAllIndices(tasks.get("Bottom"), 1);
			// If our clue is 1 then from the next cell remove all choices but boardSize
			for (Integer i : indexList) {
				board.assign((char) (65 + boardSize - 1) + Integer.toString(i), boardSize);
			}
		}
		if (tasks.get("Bottom").contains(boardSize)) {
			ArrayList<Integer> indexList = getAllIndices(tasks.get("Bottom"), boardSize);
			// If our clue is boardSize then remove all choices but 1
			// And complete column
			for (Integer i : indexList) {
				for (int j = 1; j <= boardSize; j++) {
					board.assign((char) (65 + boardSize - j) + Integer.toString(i), j);
				}
			}
		}

		if (tasks.get("Left").contains(1)) {
			ArrayList<Integer> indexList = getAllIndices(tasks.get("Left"), 1);
			// If our clue is 1 then from the next cell remove all choices but boardSize
			for (Integer i : indexList) {
				board.assign((char) (65 + i) + Integer.toString(0), boardSize);
			}
		}
		if (tasks.get("Left").contains(boardSize)) {
			ArrayList<Integer> indexList = getAllIndices(tasks.get("Left"), boardSize);
			// If our clue is boardSize then remove all choices but 1
			// And complete column
			for (Integer i : indexList) {
				for (int j = 1; j <= boardSize; j++) {
					board.assign((char) (65 + i) + Integer.toString(j - 1), j);
				}
			}
		}


		if (tasks.get("Right").contains(1)) {
			ArrayList<Integer> indexList = getAllIndices(tasks.get("Right"), 1);
			// If our clue is 1 then from the next cell remove all choices but boardSize
			for (Integer i : indexList) {
				board.assign((char) (65 + i) + Integer.toString(boardSize - 1), boardSize);
			}
		}
		if (tasks.get("Right").contains(boardSize)) {
			ArrayList<Integer> indexList = getAllIndices(tasks.get("Right"), boardSize);
			// If our clue is boardSize then remove all choices but 1
			// And complete column
			for (Integer i : indexList) {
				for (int j = 1; j <= boardSize; j++) {
					board.assign((char) (65 + i) + Integer.toString(boardSize - j), j);
				}
			}
		}

		// Advanced steps
		for (int i = 0; i < boardSize; i++) {
			int x = tasks.get("Top").get(i);
			for (int j = 0; j < x - 1; j++) {
				for (int k = 0; k < x - 1 - j; k++) {
					board.eliminate((char) (65 + j) + Integer.toString(i), boardSize - k);
				}
			}

			x = tasks.get("Bottom").get(i);
			for (int j = 0; j < x - 1; j++) {
				for (int k = 0; k < x - 1 - j; k++) {
					board.eliminate((char) (65 + boardSize - 1 - j) + Integer.toString(i), boardSize - k);
				}
			}

			x = tasks.get("Left").get(i);
			for (int j = 0; j < x - 1; j++) {
				for (int k = 0; k < x - 1 - j; k++) {
					board.eliminate((char) (65 + i) + Integer.toString(j), boardSize - k);
				}
			}

			x = tasks.get("Right").get(i);
			for (int j = 0; j < x - 1; j++) {
				for (int k = 0; k < x - 1 - j; k++) {
					board.eliminate((char) (65 + i) + Integer.toString(boardSize - 1 - j), boardSize - k);
				}
			}
		}
		board.complete   = true;
		board.inProgress = false;

		if (board.isValid())
			board.solution = true;
		else
			board.error = true;
		board.checked = true;

//		Main.mainThread.interrupt();
		try {
			Main.imDone(this);
		}
		catch (RejectedExecutionException ignored) {}
	}
}