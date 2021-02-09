package com.company;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Board {
	static ArrayList<String>               boardMembers   = new ArrayList<>();
	static Map<String, ArrayList<Integer>> tasks          = new HashMap<>();
	static Map<String, ArrayList<String>>  peers          = new HashMap<>();
	static Map<Integer, ArrayList<String>> rows           = new HashMap<>();
	static Map<Integer, ArrayList<String>> columns        = new HashMap<>();
	static Map<Integer, ArrayList<String>> reverseRows    = new HashMap<>();
	static Map<Integer, ArrayList<String>> reverseColumns = new HashMap<>();
	static int                             boardSize;

	Map<String, ArrayList<Integer>> field         = new HashMap<>();
	Map<String, Boolean>            assignedField = new HashMap<>();
	boolean                         inProgress    = false;
	boolean                         complete      = false;
	boolean                         checked       = false;
	boolean                         solution      = false;
	boolean                         error         = false;

	public Board(Map<String, ArrayList<Integer>> tasks) {
		Board.tasks = tasks;
		boardSize   = tasks.get("Top").size();
		ArrayList<Integer> combinations = new ArrayList<>();
		for (int i = 1; i < boardSize + 1; i++)
		     combinations.add(i);
		// Generate board
		for (int i = 0; i < boardSize; i++) {
			for (int j = 0; j < boardSize; j++) {
				field.put(((char) (65 + i)) + Integer.toString(j), new ArrayList<>(combinations));
				assignedField.put(((char) (65 + i)) + Integer.toString(j), false);
				if (boardMembers.size() < boardSize * boardSize)
					boardMembers.add(((char) (65 + i)) + Integer.toString(j));
			}
		}
		// Generate rows and columns
		for (int i = 0; i < boardSize; i++) {
			ArrayList<String> row    = new ArrayList<>();
			ArrayList<String> column = new ArrayList<>();
			for (int j = 0; j < boardSize; j++) {
				row.add((char) (65 + i) + Integer.toString(j));
				column.add((char) (65 + j) + Integer.toString(i));
			}
			rows.put(i, row);
			columns.put(i, column);

			ArrayList<String> reversedRow    = new ArrayList<>();
			ArrayList<String> reversedColumn = new ArrayList<>();
			for (int j = row.size() - 1; j >= 0; j--) {
				reversedRow.add(row.get(j));
				reversedColumn.add(column.get(j));
			}
			reverseRows.put(i, reversedRow);
			reverseColumns.put(i, reversedColumn);
		}
		// Generate peers
		for (String member : boardMembers) {
			ArrayList<String> memberPeers = new ArrayList<>();
			char              letter      = member.charAt(0);
			char              digit       = member.charAt(1);

			for (int i = 0; i < boardSize; i++) {
				String currentMember = letter + Integer.toString(i);
				if (!currentMember.equals(member))
					memberPeers.add(currentMember);

				currentMember = Character.toString((char) (65 + i)) + digit;
				if (!currentMember.equals(member))
					memberPeers.add(currentMember);
			}
			peers.put(member, memberPeers);
		}
	}

	public Map<String, ArrayList<Integer>> exportBetter() {
		Map<String, ArrayList<Integer>> export = new HashMap<>();

		for (String member : boardMembers) {
			ArrayList<Integer> currentMember = new ArrayList<>();
			for (Integer entry : field.get(member)) {
				int temp = entry;
				currentMember.add(temp);
			}
			export.put(member, currentMember);
		}

		return export;
	}

	public String export() {
		StringBuilder output = new StringBuilder();

		for (String member : boardMembers)
			output.append(member).append(field.get(member));

		return output.toString();
	}

	// Does not work
	public void importBetter(Map<String, ArrayList<Integer>> input, boolean initial) {
		for (String member : boardMembers) {
			try {
				input.get(member);
				ArrayList<Integer> currentMember = new ArrayList<>();
				for (Integer entry : input.get(member)) {
					int temp = entry;
					currentMember.add(temp);
					if (initial && input.get(member).size() == 1)
						assign(member, temp);
				}
				field.put(member, currentMember);
			}
			catch (Exception ignored) {}
		}
	}

	public void importField(String input) {
		String[] fieldInput = input.split("]");
		for (String forString : fieldInput) {
			String             pos     = forString.substring(0, 2);
			String             val     = forString.substring(3);
			String[]           vals    = val.split(", ");
			ArrayList<Integer> newList = new ArrayList<>();
			for (String forVal : vals)
				if (!forVal.equals(""))
					newList.add(Integer.parseInt(forVal));
			this.field.put(pos, newList);
		}
	}

	protected String printBoard() {
		System.out.println(tasks);
		StringBuilder output      = new StringBuilder();
		StringBuilder outputSmall = new StringBuilder();
		for (int i = 0; i < boardSize; i++) {
			for (int j = 0; j < boardSize; j++) {
				outputSmall.delete(0, outputSmall.length());
				for (Integer I : field.get((char) (65 + i) + Integer.toString(j))) {
					outputSmall.append(I);
				}
				output.append(String.format("%8s  ", outputSmall));
			}
			output.append("\n");
		}
		return output.toString();
	}

	protected void propagate(String pos, int i) {
		for (String peer : peers.get(pos)) {
			this.eliminate(peer, i);
		}
	}

	protected void eliminate(String pos, int i) {
		field.get(pos).removeIf(e -> e.equals(i));
		if (field.get(pos).size() == 1 && !assignedField.get(pos)) {
			assign(pos, field.get(pos).get(0));
		}
	}

	protected void assign(String pos, int i) {
		if (!assignedField.get(pos)) {
			field.get(pos).removeIf(e -> !e.equals(i));
			assignedField.put(pos, true);
			propagate(pos, i);
		}
	}

	protected boolean checkRow(int task, ArrayList<String> test) {
		if (task == 0)
			return false;
		int top     = 0;
		int visible = 0;
		for (String pos : test) {
			if (field.get(pos).size() == 1) {
				int c = field.get(pos).get(0);
				if (c > top) {
					visible += 1;
					top = c;
					if (visible > task) {
						return true;
					}
				}
			} else {
				return true;
			}
		}
		if (visible == task)
			return false;
		else
			return true;
	}

	protected boolean isValid() {
		for (int i = 0; i < boardSize; i++) {
			if (checkRow(tasks.get("Top").get(i), columns.get(i)))
				return false;
			if (checkRow(tasks.get("Bottom").get(i), reverseColumns.get(i)))
				return false;
			if (checkRow(tasks.get("Left").get(i), rows.get(i)))
				return false;
			if (checkRow(tasks.get("Right").get(i), reverseRows.get(i)))
				return false;
		}
		checked = true;
		return true;
	}

	protected String bestGuess() {
		String pos = "";
		int    min = boardSize;
		for (String mem : boardMembers) {
			if (field.get(mem).size() > 1 && field.get(mem).size() < min) {
				min = field.get(mem).size();
				pos = mem;
			}
		}
		return pos;
	}
}
