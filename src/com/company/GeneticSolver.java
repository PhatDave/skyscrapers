package com.company;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.*;

public class GeneticSolver {
	//	static final int    population     = (int) Math.pow(20, 2);
	//	static final int    numSplices     = (int) Math.sqrt(population) - 1;
	static int    population     = 200;
	static int    numSplices     = 3;
	static int    iterations     = 1000;
	static double mutationChance = 0.3;
	static int    iteration      = 0;
	static Board  bestBoard;
	static Board  startingBoard;
	static Random rand           = new Random();
	ArrayList<Integer>  fitness           = new ArrayList<>();
	Map<Board, Integer> currentPopulation = new HashMap<>();
	JFrame              inputFrame        = new JFrame();
	BufferedWriter      fileWriter        = new BufferedWriter(new FileWriter("output.txt"));

	public GeneticSolver(Board input) throws IOException {
		GridBagConstraints gbc    = new GridBagConstraints();
		GridBagLayout      layout = new GridBagLayout();
		inputFrame.setResizable(false);
		inputFrame.setLayout(layout);

		gbc.gridx = 0;
		gbc.gridy = 0;

		inputFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JButton    confirm             = new JButton();
		JLabel     popInputLabel       = new JLabel("Population");
		JTextField popInput            = new JTextField("200");
		JLabel     spliceInputLabel    = new JLabel("Splices");
		JTextField spliceInput         = new JTextField("3");
		JLabel     iterationInputLabel = new JLabel("Iterations");
		JTextField iterationInput      = new JTextField("2000");
		JSlider    mutationInput       = new JSlider(0, 100);
		mutationInput.setValue(20);
		JLabel mutationInputLabel = new JLabel("Mutation chance");

		mutationInput.addChangeListener(e -> {
			mutationInputLabel.setText(Double.toString((double) mutationInput.getValue() / 100));
		});

		popInput.setColumns(8);
		spliceInput.setColumns(8);
		iterationInput.setColumns(8);

		inputFrame.add(popInputLabel, gbc);
		gbc.gridy = 1;
		inputFrame.add(spliceInputLabel, gbc);
		gbc.gridy = 2;
		inputFrame.add(iterationInputLabel, gbc);
		gbc.gridy = 3;
		inputFrame.add(mutationInputLabel, gbc);

		gbc.gridx     = 1;
		gbc.gridy     = 0;
		gbc.gridwidth = 2;
		inputFrame.add(popInput, gbc);
		gbc.gridy = 1;
		inputFrame.add(spliceInput, gbc);
		gbc.gridy = 2;
		inputFrame.add(iterationInput, gbc);
		gbc.gridy = 3;
		inputFrame.add(mutationInput, gbc);
		gbc.gridy = 4;
		inputFrame.add(confirm, gbc);

		inputFrame.setSize(new Dimension(640, 480));

		confirm.setText("Confirm");
		confirm.addActionListener(e -> {
			popInput.setEditable(false);
			spliceInput.setEditable(false);
			iterationInput.setEditable(false);
			mutationInput.setEnabled(false);

			population     = Integer.parseInt(popInput.getText());
			numSplices     = Integer.parseInt(spliceInput.getText());
			iterations     = Integer.parseInt(iterationInput.getText());
			mutationChance = (double) mutationInput.getValue() / 100;

			startingBoard = input;
			while (currentPopulation.size() < population) {
				currentPopulation.put(generateRandomBoard(), 1);
			}
			try { startLoop(); } catch (IOException ignored) {}
			System.out.println(bestBoard.printBoard());
			DrawGraph.createAndShowGui(fitness);
		});
		inputFrame.pack();
		inputFrame.setVisible(true);
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

	public void startLoop() throws IOException {
		System.out.println(population + " " + numSplices);
		fileWriter.write("Population: " + population + "; numSplices: " + numSplices + "; mutationChance: " + mutationChance + "\n");
		for (iteration = 0; iteration < iterations; iteration++) {
//			System.out.println("Iteration " + iteration);
			fileWriter.write("Iteration " + iteration + "\n");
			for (Map.Entry<Board, Integer> iteratedBoard : currentPopulation.entrySet()) {
				iteratedBoard.setValue(getFitness(iteratedBoard.getKey()));
			}
			currentPopulation = sortByValue(currentPopulation);

			Map<Board, Integer> toRemove = new HashMap<>();
			int                 i        = 0;
			for (Map.Entry<Board, Integer> iteratedBoard : currentPopulation.entrySet()) {
				if (i < (population / (numSplices + 1))) {
					if (i == 0) {
						fileWriter.write("Best board " + iteratedBoard.getValue() + "\n");
//						System.out.println("Best board " + iteratedBoard.getValue());
						fitness.add(iteratedBoard.getValue());
						bestBoard = iteratedBoard.getKey();
					}
					fileWriter.write("Best boards (" + (i + 1) + ") " + iteratedBoard.getValue() + "\n" +
					                 iteratedBoard.getKey().printBoard() + "\n");
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
//				System.out.println(currentPopulation);
//				System.out.println(splicingPositions);

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
							while (!startingBoard.field.get(pos)
									.contains(tempRandInt = rand.nextInt(PuzzleGenerator.boardSize)))
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
//				tempBoard.field.get(mem).removeIf(e -> true);
//				tempBoard.field.get(mem).add(tempRandInt);
				tempBoard.assign(mem, tempRandInt);
			}
		}
		return tempBoard;
	}
}
