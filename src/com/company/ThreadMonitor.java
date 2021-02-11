package com.company;

import static com.company.Main.boards;

/**
 * Intended to output the number of threads/boards in progress, boards completed and total number of boards
 * Strongly discouraged from being used, does not work well with other threads working
 * @deprecated
 */
public class ThreadMonitor extends Thread {
	static boolean run = true;
	int boardsInProgress = 0;
	int boardsCompleted  = 0;

	public void run() {
		while (run) {
			try {
				boardsInProgress = boardsCompleted = 0;
				for (Board i : boards) {
					if (i.inProgress)
						++boardsInProgress;
					if (i.complete)
						++boardsCompleted;
				}

				System.out.println(boardsInProgress + "/" + boardsCompleted + "/" + boards.size());
				Thread.sleep(100);
			}
			catch (Exception ignored) {}
		}
	}
}
