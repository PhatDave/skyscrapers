package com.company;

import static com.company.Main.boards;

public class ThreadMonitor extends Thread {
	static boolean run = true;
	int boardsInProgress = 0;
	int boardsCompleted  = 0;

	@Override
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
