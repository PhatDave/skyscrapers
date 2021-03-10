package com.company;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Contains all necessary info for solving boards such as
 *  <ul>
 *      <li>List boardMembers (static)<pre>    all cells by index string</pre>
 *      <li>Map tasks (static)<pre>    tasks that dictate the puzzle indexed by
 *      "Top", "Bottom", "Right" and "Left"</pre>
 *      <li>Map peers (static)<pre>    peers of all cells indexed by cell string,
 *      see boardMembers</pre>
 *      <li>Map rows (static)<pre>    all rows indexed by integer</pre>
 *      <li>Map columns (static)<pre>    all columns indexed by integer</pre>
 *      <li>Map reverseRows (static)<pre>    all rows indexed by integer in reverse</pre>
 *      <li>Map reverseColumns (static)<pre>    all columns indexed by integer in reverse</pre>
 *      <li>int boardSize (static)<pre>    size of the board, also size of tasks</pre>
 *
 *      <li>Map field<pre>    Board field, indexed by string and contains 
 *      a List of all possible moves in given cell</pre></li>
 *      <li>Map assignedField<pre>    Indexed by string, every cell is given a boolean
 *      that tells whether the cell has been assigned 
 *      to by the assign method</pre></li>
 *      <li>boolean inProgress<pre>    True if the board is being worked on
 *      by an active thread or not</pre></li>
 *      <li>boolean complete<pre>     True if the board has been processed
 *      by a thread using PuzzleSolver</pre></li>
 *      <li>boolean checked<pre>     True if the board has been checked for errors</pre></li>
 *      <li>boolean solution<pre>     True if the board is the solution to given tasks</pre></li>
 *      <li>boolean error<pre>     True if board contains an error,
 *      boards with this flag will be ignored by
 *      Threads</pre></li>
 *  </ul>
 */
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
	/**
	 * Generates a board given by tasks, assigns all static values for use by other classes
	 * @param tasks tasks for board to obey
	 */
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

	/**
	 * Creates a deep copy of the given board by copying it's field
	 */
	public void importBetter(Map<String, ArrayList<Integer>> input) {
		for (Map.Entry<String, ArrayList<Integer>> entry : input.entrySet()) {
			field.put(entry.getKey(), new ArrayList<>(entry.getValue()));
		}
	}

	/**
	 * Formats the board to a string
	 * @return String
	 */
	protected synchronized String printBoard() {
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

	/**
	 * For use by {@link PuzzleSolver}
	 * Propagates constraints from position pos by iterating through all it's peers ({@link Board}) and eliminating (
	 * {@link #eliminate}) every number but i
	 * @param pos cell that is being propagated from
	 * @param i the number assigned to pos, all numbers but i are removed from peers
	 */
	protected void propagate(String pos, int i) {
		for (String peer : peers.get(pos)) {
			this.eliminate(peer, i);
		}
	}

	/**
	 * Eliminates i from pos, if pos has only one choice remaining it also assigns the choice to pos ({@link #assign})
	 * @param pos cell from which i is being removed
	 * @param i number which is removed from valid options in cell pos
	 */
	protected void eliminate(String pos, int i) {
		field.get(pos).removeIf(e -> e.equals(i));
		if (field.get(pos).size() == 1 && !assignedField.get(pos))
			assign(pos, field.get(pos).get(0));
	}

	/**
	 * Assigns i to pos by eliminating all choices but i in pos
	 * @param pos cell to which i is being assigned
	 * @param i number which is being assigned
	 */
	protected void assign(String pos, int i) {
		if (!assignedField.get(pos)) {
			field.get(pos).removeIf(e -> !e.equals(i));
			assignedField.put(pos, true);
			propagate(pos, i);
		}
	}

	/**
	 * Called by {@link #isValid}, checks the current row for errors
	 * <p>
	 * Method:
	 * Initially no towers are seen so the highest tower seen is 0, iterating through cRow every tower is compared
	 * (by referencing it from field) with the highest and if our current tower is higher then the current highest tower is our current tower and we
	 * can see +1 tower
	 * <p>
	 * Additionally, if any cell has more than one option the method returns true
	 *
	 * @param task task to check row against
	 * @param cRow row to check against task
	 * @return true if there is an error in the row, false otherwise
	 */
	protected boolean checkRow(int task, ArrayList<String> cRow) {
		if (task == 0)
			return false;
		int top     = 0;
		int visible = 0;
		for (String pos : cRow) {
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
		return visible != task;
	}

	/**
	 * Runs over every task in tasks and calls {@link #checkRow} against them
	 * <p>Also sets the checked boolean to true</p>
	 * @return true if none of the rows contain an error (solution) else false
	 */
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

	/**
	 * Used when starting new boards from an existing one which has run into a dead end
	 * @return the position which has the least choices (String)
	 */
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
