package com.company;

import javax.swing.*;
import java.awt.*;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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

	static int              threadsStarted = 0;
	static Thread           mainThread     = Thread.currentThread();
	static boolean          firstBoard     = true;
	static ExecutorService  executor       = Executors.newFixedThreadPool(100);
	static ArrayList<Board> boards         = new ArrayList<>();
	static long             fullStart      = 0;
	static long             solveStart     = 0;
	static Board            solutionBoard;
	static boolean          solved         = false;

	public static void imDone(PuzzleSolver currentSolver) {
		if (currentSolver.board.solution) {
			solved        = true;
			solutionBoard = currentSolver.board;
			mainThread.interrupt();
//			executor.shutdownNow();
			try {
				executor.awaitTermination(30, TimeUnit.SECONDS);
			}
			catch (Exception ignored) {}
		} else {
			String guess = currentSolver.board.bestGuess();
			if (!guess.equals("")) {
				for (int i = 0; i < currentSolver.board.field.get(guess).size(); i++) {
					Board newBoard = new Board(PuzzleGenerator.tasks);
					executor.execute(new Thread(
							new PuzzleSolver(newBoard, guess, currentSolver.board.field.get(guess).get(i),
							                 currentSolver.board)));
					boards.add(newBoard);
					threadsStarted++;
				}
			}
		}
	}

	// TODO: introduce new variable to board named parent board to track the hierarchy to the winning one
	// TODO: maybe make array list too to track every move per board on assign
	// TODO: also make benchmark class to run same board x times, for this get a board for first time and reuse it
	// TODO: output results of benchmark to txt or serialize and display with js
	// TODO: also rework main algorithm to be more generic // using row column rowreversed and columnreversed
	// TODO: try making constants final after assignment
	// TODO: maybe rework propagation to add all to-eliminate entries to a queue and remove them from field all at onc
	// TODO: export is actually a waste of time and returns a shallow copy where import makes a deep copy

	public static void main(String[] args) throws InterruptedException, IOException {
		fullStart = System.nanoTime();

		JFrame    root  = new JFrame();
		JTextArea field = new JTextArea();
		field.setPreferredSize(new Dimension(400, 200));
		root.add(field);
		root.pack();
		root.setVisible(true);
//		new PuzzleGenerator(7, "https://www.puzzle-skyscrapers.com/?e=Nzo1LDM0MCw3MTY=");
//		new PuzzleGenerator(7, "https://www.puzzle-skyscrapers.com/?e=Nzo5LDE5OCw2ODE=");
//		new PuzzleGenerator(0,"https://www.puzzle-skyscrapers.com/?e=MDoxNjAsMDMx");
//		new PuzzleGenerator(6);
		new PuzzleGenerator(6, "https://www.puzzle-skyscrapers.com/?e=Njo4LDY5MiwyNTc=");

//		ThreadMonitor monitor = new ThreadMonitor();

		System.out.println(PuzzleGenerator.link);

		Board first;
		if (PuzzleGenerator.hasField) {
			first = new Board(PuzzleGenerator.tasks);
			first.importBetter(PuzzleGenerator.field, true);
		} else
			first = new Board(PuzzleGenerator.tasks);
		boards.add(first);
		System.out.println(first.printBoard());

		// Starts thread for each incomplete - idle board in boards until a solution is found
//		System.in.read();
//		monitor.start();
		solveStart = System.nanoTime();
		try {
			if (firstBoard) {
				firstBoard = false;
				executor.execute(new Thread(new PuzzleSolver(first)));
			}
			Thread.sleep((long) (60 * 1e18));
			System.exit(-1);
		}
		catch (InterruptedException e) {
			long end = System.nanoTime();

			StringBuilder temp = new StringBuilder();
			temp.append("Top:  ");
			for (Integer i : Board.tasks.get("Top"))
				temp.append(i);
			temp.append("    Left:  ");
			for (Integer i : Board.tasks.get("Left"))
				temp.append(i);
			temp.append("    Bottom:  ");
			for (Integer i : Board.tasks.get("Bottom"))
				temp.append(i);
			temp.append("    Right:  ");
			for (Integer i : Board.tasks.get("Right"))
				temp.append(i);

//			ThreadMonitor.run = false;
			System.out.println(ANSI_GREEN);
			System.out.println(solutionBoard.printBoard());
			System.out.println(ANSI_RESET);
			System.out.println(threadsStarted);
			System.out.println((end - solveStart) / 1e6 + "ms");

			field.setText(PuzzleGenerator.link + "\n" + solutionBoard.printBoard() + "\n" + threadsStarted + "\n" +
			              (end - solveStart) / 1e6 +
			              "ms");

			try {
				String     fileName = "log" + PuzzleGenerator.difficulty + ".txt";
				FileWriter myWriter = new FileWriter(fileName, true);
				myWriter.write(PuzzleGenerator.link);
				myWriter.write("\n");
				myWriter.write(temp.toString());
				myWriter.write("\n");
				myWriter.write(solutionBoard.printBoard());
				myWriter.write(Integer.toString(threadsStarted));

				myWriter.write("\n");
				myWriter.write(Long.toString(end - fullStart));
				myWriter.write("ns ");
				myWriter.write(Long.toString((end - fullStart) / 1000));
				myWriter.write("us ");
				myWriter.write(Long.toString((end - fullStart) / 1000000));
				myWriter.write("ms ");
				myWriter.write(Long.toString((end - fullStart) / 1000000000));
				myWriter.write("s ");

				myWriter.write("\n");
				myWriter.write(Long.toString(end - solveStart));
				myWriter.write("ns ");
				myWriter.write(Long.toString((end - solveStart) / 1000));
				myWriter.write("us ");
				myWriter.write(Long.toString((end - solveStart) / 1000000));
				myWriter.write("ms ");
				myWriter.write(Long.toString((end - solveStart) / 1000000000));
				myWriter.write("s ");

				myWriter.write("\n\n\n");
				myWriter.close();
			}
			catch (IOException ignored) {}

//			System.exit(0);
		}
	}
}
