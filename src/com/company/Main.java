package com.company;

import javax.swing.*;
import java.awt.*;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

// TODO: Enable looking through set fields for complex puzzles and maybe add option for directly importing puzzle
//  without downloading it from page, seems to take a long time (url get) ALSO rework export/import to use arraylist
//  instead of string and then just manually copy the thing instead of using string like a big DUMDUM
// Rudi kaze, za easy puzzle je ok, ne treba teze i da e ne komplicira vise nista!

public class Main {
	public static final String ANSI_RESET  = "\u001B[0m";
	public static final String ANSI_GREEN  = "\u001B[32m";

	static int              threadsStarted = 0;
	static Thread           mainThread     = Thread.currentThread();
	static boolean          firstBoard     = true;
	static ExecutorService  executor       = Executors.newFixedThreadPool(1000);
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
					System.out.println(newBoard.printBoard());
					threadsStarted++;
				}
				currentSolver.board = null;
			}
		}
	}

	public static void main(String[] args) {
		fullStart = System.nanoTime();

		JFrame    root  = new JFrame();
		JTextArea field = new JTextArea();
		field.setPreferredSize(new Dimension(400, 200));
		root.add(field);
		root.pack();
		root.setVisible(true);
		new PuzzleGenerator(0);

		System.out.println(PuzzleGenerator.link);

		Board first;
		if (PuzzleGenerator.hasField) {
			first = new Board(PuzzleGenerator.tasks);
			first.importBetter(PuzzleGenerator.field);
		} else
			first = new Board(PuzzleGenerator.tasks);
		boards.add(first);
		System.out.println(first.printBoard());
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

			new Scanner(System.in).nextLine();
			System.exit(0);
		}
	}
}
