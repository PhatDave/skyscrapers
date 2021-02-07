package com.company;

import java.util.ArrayList;

// https://www.puzzle-skyscrapers.com/?e=MjoxMCw3MDcsNDYz 3412 2134 1243 4321
//

public class Main {
	public static final String ANSI_RESET  = "\u001B[0m";
	public static final String ANSI_BLACK  = "\u001B[30m";
	public static final String ANSI_RED    = "\u001B[31m";
	public static final String ANSI_GREEN  = "\u001B[32m";
	public static final String ANSI_YELLOW = "\u001B[33m";
	public static final String ANSI_BLUE   = "\u001B[34m";
	public static final String ANSI_PURPLE = "\u001B[35m";
	public static final String ANSI_CYAN   = "\u001B[36m";
	public static final String ANSI_WHITE  = "\u001B[37m";


	public static void main(String[] args) {
		// Dual - solved
//		PuzzleGenerator puzzle = new PuzzleGenerator("https://www.puzzle-skyscrapers.com/?e=MDo2NTcsNzQ2");
		// Very fucked - solved
//		PuzzleGenerator puzzle = new PuzzleGenerator("https://www.puzzle-skyscrapers.com/?e=MDo1NDIsNzEw");
		// Breaks program - solved
//		PuzzleGenerator puzzle = new PuzzleGenerator("https://www.puzzle-skyscrapers.com/?e=MDoxOCwzNDg=");
		// Deadly!
//		PuzzleGenerator puzzle = new PuzzleGenerator("https://www.puzzle-skyscrapers.com/?e=MDoxNzAsMTA4");
		// Breaks program, 5x5
//		PuzzleGenerator puzzle = new PuzzleGenerator(3, "https://www.puzzle-skyscrapers.com/?e=MzoxNCw4ODYsODgz");
		PuzzleGenerator puzzle = new PuzzleGenerator();
		System.out.println(puzzle.link);
//		System.out.println(puzzle.puzzleID);
//		System.out.println(puzzle.task);
//		System.out.println(puzzle.tasks);
		System.out.println("\n");

		boolean          solved       = false;
		Board            first        = new Board(puzzle.tasks);
		ArrayList<Board> boards       = new ArrayList<>();
		ArrayList<Board> boardsBuffer = new ArrayList<>();
		boards.add(first);

		// Starts thread for each incomplete - idle board in boards until a solution is found
		while (!solved) {
			for (Board forBoard : boards) {
				if (!forBoard.inProgress && !forBoard.complete) {
					PuzzleSolver solver = new PuzzleSolver(forBoard);
					forBoard.inProgress    = true;
					forBoard.currentSolver = solver;
					solver.start();
				} else if (forBoard.complete) {
					forBoard.inProgress = false;
					if (forBoard.isValid()) {
						solved            = true;
						forBoard.solution = true;
					} else if (!forBoard.bestGuess().equals("") && !forBoard.error) {
						String guess = forBoard.bestGuess();
						for (int i = 0; i < forBoard.field.get(guess).size(); i++) {
							Board newBoard = new Board(puzzle.tasks);
							newBoard.importField(forBoard.export());
							boardsBuffer.add(newBoard);

							PuzzleSolver solver = new PuzzleSolver(newBoard, guess, forBoard.field.get(guess).get(i));
							solver.start();
						}
					} else {
						forBoard.error = true;
					}

					if (forBoard.solution) {
						System.out.println(ANSI_GREEN);
						forBoard.printBoard();
						System.out.println(ANSI_RESET);
					} else if (forBoard.error) {
//						System.out.println(ANSI_RED);
//						forBoard.printBoard();
//						System.out.println(ANSI_RESET);
					} else {
//						forBoard.printBoard();
					}
				}
			}
			boards.addAll(boardsBuffer);
			boardsBuffer.clear();
		}
	}
}
