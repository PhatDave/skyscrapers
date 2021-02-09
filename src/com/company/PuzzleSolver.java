package com.company;

import java.util.ArrayList;
import java.util.concurrent.RejectedExecutionException;

import static com.company.Board.boardSize;
import static com.company.Board.tasks;

//public class PuzzleSolver extends Thread {
public class PuzzleSolver implements Runnable {
	Board                           board;
	String                          pos   = "";
	int                             guess = 0;

	public PuzzleSolver(Board board) {
		this.board = board;
	}

	public PuzzleSolver(Board board, String pos, int guess, Board importBoard) {
		this.board = board;
		this.pos   = pos;
		this.guess = guess;
		this.board.importBetter(importBoard.exportBetter(), false);
	}

	private static ArrayList<Integer> getAllIndices(ArrayList<Integer> list, int lookup) {
		ArrayList<Integer> temp = new ArrayList<>();
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i) == lookup) {
				temp.add(i);
			}
		}
		return temp;
	}

	@Override
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
					board.assign((char)(65 + boardSize - j) + Integer.toString(i), j);
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
		board.complete = true;
		board.inProgress = false;

		if (board.isValid())
			board.solution = true;
		else
			board.error = true;
		board.checked = true;

//		Main.mainThread.interrupt();
		try {
			Main.imDone(this);
		} catch (RejectedExecutionException ignored) {}
	}
}