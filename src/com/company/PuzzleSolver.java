package com.company;

import static com.company.Main.stuffsDone;

public class PuzzleSolver implements Runnable {
	Board board;

	public PuzzleSolver(Board board) {
		this.board = board;
		this.run();
	}

	private void checkThings() {
		for (int i = 0; i < PuzzleGenerator.boardSize; i++) {
			String   pos                = (char) (65 + i) + Integer.toString(i);
			String[] favorablePositions = new String[PuzzleGenerator.boardSize];
			int[]    combinations       = new int[PuzzleGenerator.boardSize];

			if (board.field.get(pos).size() > 1) {
				for (Integer I : board.field.get(pos)) {
					++combinations[I - 1];
					favorablePositions[I - 1] = pos;
				}
			}

			for (String peer : PuzzleGenerator.peers.get(pos)) {
				if (board.field.get(peer).size() > 1) {
					for (Integer I : board.field.get(peer)) {
						if (combinations[I - 1] > 1)
							continue;
						++combinations[I - 1];
						favorablePositions[I - 1] = peer;
					}
				}
			}

			for (int j = 0; j < combinations.length; j++) {
				if (combinations[j] == 1) {
//					System.out.println("Assign " + (j + 1) + " to " + favorablePositions[j]);
					board.assign(favorablePositions[j], j + 1);
				}
			}
		}
	}

	@Override
	public void run() {
		for (int i = 0; i < PuzzleGenerator.task.size(); i++) {
			if (PuzzleGenerator.task.get(i) == 1) {
				board.assign(PuzzleGenerator.taskRows.get(i).get(0), PuzzleGenerator.boardSize);
			} else if (PuzzleGenerator.task.get(i) == PuzzleGenerator.boardSize) {
				for (int j = 0; j < PuzzleGenerator.boardSize; j++) {
					board.assign(PuzzleGenerator.taskRows.get(i).get(j), j + 1);
				}
			} else {
				int x     = PuzzleGenerator.task.get(i);
				int count = 0;
				for (String mem : PuzzleGenerator.taskRows.get(i)) {
					for (int k = 0; k < x - 1 - count; k++)
					     board.eliminate(mem, PuzzleGenerator.boardSize - k);
					++count;
					if (count >= x - 1)
						break;
				}
			}
		}

		checkThings();
		stuffsDone(this);
	}
}
