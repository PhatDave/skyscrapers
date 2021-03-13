package com.company;

import java.util.*;

public class GeneticSolver {
	static final int    population = (int) Math.pow(2, 2);
	static final int    numSplices = (int) Math.sqrt(population) - 1;
	static final int    iterations = 2000;
	static final double mutationChance = 0.1;
	ArrayList<Integer> fitness = new ArrayList<>();
	static       Board  bestBoard;
	static       Board  startingBoard;
	static       Random rand       = new Random();
	Map<Board, Integer> currentPopulation = new HashMap<>();

	public GeneticSolver(Board input) {
		startingBoard = input;
		while (currentPopulation.size() < population) {
			currentPopulation.put(generateRandomBoard(), 1);
		}
		startLoop();
		System.out.println(bestBoard.printBoard());
		DrawGraph.createAndShowGui(fitness);
	}

	private String offsetToPos(int offset) {
		char digit  = '0';
		char letter = 'A';
		while (offset > PuzzleGenerator.boardSize - 1) {
			offset -= PuzzleGenerator.boardSize;
			letter += 1;
		}
		while (offset > 0) {
			offset -= 1;
			digit += 1;
		}
		return String.valueOf(letter) + digit;
	}

	private Map<Integer, ArrayList<String>> getSpliceArrays(ArrayList<Integer> splicingPositions) {
		Map<Integer, ArrayList<String>> spliceArrays = new HashMap<>();
		ArrayList<String>               offsets      = new ArrayList<>();
		for (Integer i : splicingPositions) {
			offsets.add(offsetToPos(i));
		}
		offsets.add(offsetToPos(PuzzleGenerator.boardSize * PuzzleGenerator.boardSize - 1));

		int               i           = 0;
		boolean           changed     = true;
		ArrayList<String> offsetArray = null;
		for (String mem : PuzzleGenerator.boardMembers) {
			if (changed) {
				offsetArray = new ArrayList<>();
				changed     = false;
			}
			if (mem.equals(offsets.get(i))) {
				spliceArrays.put(i, offsetArray);
				i++;
				changed = true;
			}
			offsetArray.add(mem);
		}

		return spliceArrays;
	}

	public void startLoop() {
		System.out.println(population + " " + numSplices);
		for (int iteration = 0; iteration < iterations; iteration++) {
//			System.out.println("Iteration " + iteration);
			for (Map.Entry<Board, Integer> iteratedBoard : currentPopulation.entrySet()) {
				iteratedBoard.setValue(getFitness(iteratedBoard.getKey()));
			}
			currentPopulation = sortByValue(currentPopulation);

			Map<Board, Integer> toRemove = new HashMap<>();
			int                 i        = 0;
			for (Map.Entry<Board, Integer> iteratedBoard : currentPopulation.entrySet()) {
				if (i < (numSplices + 1)) {
					if (i == 0) {
//						System.out.println("Best board " + iteratedBoard.getValue());
						fitness.add(iteratedBoard.getValue());
						bestBoard = iteratedBoard.getKey();
					}
//					System.out.println("Board " + (i + 1) + "  " + iteratedBoard.getValue());
//					System.out.println(iteratedBoard.getKey().printBoard());
					i += 1;
				} else {
					toRemove.put(iteratedBoard.getKey(), iteratedBoard.getValue());
				}
			}
			currentPopulation.entrySet().removeAll(toRemove.entrySet());

			// Splicing
			Map<Board, Integer> newPopulation = new HashMap<>();
			for (Map.Entry<Board, Integer> ignored : currentPopulation.entrySet()) {
				ArrayList<Integer> splicingPositions = new ArrayList<>();
				for (int j = 0; j < numSplices; j++) {
					int tempRandInt;
					while (splicingPositions.contains(
							tempRandInt = rand.nextInt((PuzzleGenerator.boardSize * PuzzleGenerator.boardSize) - 1))) {}
					splicingPositions.add(tempRandInt);
				}
				Collections.sort(splicingPositions);

				Map<Integer, ArrayList<String>> spliceArrays  = getSpliceArrays(splicingPositions);
				Map<String, ArrayList<Integer>> newBoardField = new HashMap<>();
				for (Map.Entry<Integer, ArrayList<String>> splice : spliceArrays.entrySet()) {
					Board spliceBoard = (Board) currentPopulation.keySet().toArray()[splice.getKey()];
					for (String pos : splice.getValue()) {
						if (rand.nextDouble() < mutationChance) {
							ArrayList<Integer> tempList = new ArrayList<>();
							if (startingBoard.field.get(pos).size() == 1) {
								tempList.add(startingBoard.field.get(pos).get(0));
								newBoardField.put(pos, tempList);
								continue;
							}
							int tempRandInt;
							while (!startingBoard.field.get(pos).contains(tempRandInt =
									                                            rand.nextInt(PuzzleGenerator.boardSize)))
								continue;
							tempList.add(tempRandInt);
							newBoardField.put(pos, tempList);
							continue;
						}
						newBoardField.put(pos, spliceBoard.field.get(pos));
					}
				}
				newPopulation.put(new Board(newBoardField), 1);
			}
			currentPopulation = newPopulation;
		}
	}

	// Ty stackoverflow
	public <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
		List<Map.Entry<K, V>> list = new ArrayList<>(map.entrySet());
		list.sort(Map.Entry.comparingByValue());

		Map<K, V> result = new LinkedHashMap<>();
		for (Map.Entry<K, V> entry : list) {
			result.put(entry.getKey(), entry.getValue());
		}

		return result;
	}

	public int getFitness(Board input) {
		int fitness = 0;
		for (Map.Entry<Integer, ArrayList<String>> temp : PuzzleGenerator.taskRows.entrySet()) {
			ArrayList<Integer> allAssignedValues = new ArrayList<>();
			for (String pos : temp.getValue()) {
				if (allAssignedValues.contains(input.field.get(pos).get(0))) fitness += 2;
				allAssignedValues.add(input.field.get(pos).get(0));
			}


			int top     = 0;
			int visible = 0;
			for (String pos : temp.getValue()) {
				if (input.field.get(pos).size() == 1) {
					int c = input.field.get(pos).get(0);
					if (c > top) {
						visible += 1;
						top = c;
					}
				}
			}
			if (visible != temp.getKey()) fitness += 1;
		}
		return fitness;
	}

	public Board generateRandomBoard() {
		Board tempBoard = new Board(startingBoard);
		for (String mem : PuzzleGenerator.boardMembers) {
			if (tempBoard.field.get(mem).size() > 1) {
				int tempRandInt;
				while (!tempBoard.field.get(mem).contains(tempRandInt = rand.nextInt(PuzzleGenerator.boardSize)))
					continue;
				tempBoard.field.get(mem).removeIf(e -> true);
				tempBoard.field.get(mem).add(tempRandInt);
			}
		}
		return tempBoard;
	}
}
