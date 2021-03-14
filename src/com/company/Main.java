package com.company;

import java.io.IOException;
import java.util.ArrayList;

// TODO: Enable looking through set fields for complex puzzles and maybe add option for directly importing puzzle
//  without downloading it from page, seems to take a long time (url get) ALSO rework export/import to use arraylist
//  instead of string and then just manually copy the thing instead of using string like a big DUMDUM
// Rudi kaze, za easy puzzle je ok, ne treba teze i da e ne komplicira vise nista!

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

	static Thread           mainThread     = Thread.currentThread();
	static ArrayList<Board> boards         = new ArrayList<>();
	static Board            solutionBoard;

	static public void stuffsDone(PuzzleSolver currentSolver) {
		solutionBoard = currentSolver.board;
		mainThread.interrupt();
//		System.exit(0);
	}

	public static void main(String[] args) {
		long execStart = System.nanoTime();
//		new PuzzleSolver(new Board(new PuzzleGenerator(true)));
//		new PuzzleSolver(new Board(new PuzzleGenerator(0, "https://www.puzzle-skyscrapers.com/?e=MDozNTMsNDEw")));
		new PuzzleSolver(new Board(new PuzzleGenerator(5)));
		try {
			Thread.sleep((long) (60 * 1e18));
			System.exit(-1);
		}
		catch (InterruptedException e) {
			System.out.println(solutionBoard.printBoard());
			try {
				new GeneticSolver(solutionBoard);
			} catch (IOException ignored) {}
		}
	}
}
