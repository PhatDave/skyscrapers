package com.company;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PuzzleGenerator {
	static String                          puzzleID       = "";
	static String                          link           = "";
	static ArrayList<Integer>              task           = new ArrayList<>();
	static Map<String, ArrayList<Integer>> tasks          = new HashMap<>();
	static ArrayList<String>               boardMembers   = new ArrayList<>();
	static Map<String, ArrayList<String>>  peers          = new HashMap<>();
	static Map<Integer, ArrayList<String>> rows           = new HashMap<>();
	static Map<Integer, ArrayList<String>> columns        = new HashMap<>();
	static Map<Integer, ArrayList<String>> reverseRows    = new HashMap<>();
	static Map<Integer, ArrayList<String>> reverseColumns = new HashMap<>();
	static Map<Integer, ArrayList<String>> taskRows       = new HashMap<>();
	static ArrayList<Integer>              combinations   = new ArrayList<>();
	static int                             difficulty     = 0;
	static int                             boardSize;
	boolean                         hasField = false;
	Map<String, ArrayList<Integer>> field    = new HashMap<>();

	public PuzzleGenerator() {
		this.getBoard(0, "https://www.puzzle-skyscrapers.com/", false);
	}

	public PuzzleGenerator(int sizei, String link) {
		this.getBoard(sizei, link, true);
	}

	public PuzzleGenerator(int sizei) {
		this.getBoard(sizei, "https://www.puzzle-skyscrapers.com/", false);
	}

	public PuzzleGenerator(boolean ok) {
		if (ok) {
			Integer[] temp = {
					2, 3, 3, 1, 4, 2,
					2, 1, 3, 3, 2, 2,
					2, 4, 1, 2, 5, 2,
					3, 1, 4, 3, 2, 2,
			};
			Map<String, Integer> entryField = new HashMap<>();
			task.addAll(Arrays.asList(temp));

			entryField.put("C2", 2);
			entryField.put("C3", 4);
			entryField.put("D5", 4);
			entryField.put("F3", 1);

			for (Map.Entry<String, Integer> entry : entryField.entrySet()) {
				ArrayList<Integer> tempList = new ArrayList<>();
				tempList.add(entry.getValue());
				field.put(entry.getKey(), tempList);
			}

			generateBoard();
		}
	}

	private void generateBoard() {
		boardSize = task.size() / 4;
		ArrayList<Integer> Top    = new ArrayList<>();
		ArrayList<Integer> Bottom = new ArrayList<>();
		ArrayList<Integer> Left   = new ArrayList<>();
		ArrayList<Integer> Right  = new ArrayList<>();
		for (int i = 0; i < boardSize; i++) {
			Top.add(task.get(i));
			Bottom.add(task.get(i + boardSize));
			Left.add(task.get(i + (2 * boardSize)));
			Right.add(task.get(i + (3 * boardSize)));

			combinations.add(i + 1);
		}
		tasks.put("Top", Top);
		tasks.put("Bottom", Bottom);
		tasks.put("Left", Left);
		tasks.put("Right", Right);

		// Generate board
		for (int i = 0; i < boardSize; i++) {
			for (int j = 0; j < boardSize; j++) {
				String pos = ((char) (65 + i)) + Integer.toString(j);
				field.computeIfAbsent(pos, k -> new ArrayList<>(combinations));
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

		for (int i = 0; i < boardSize; i++) {
			ArrayList<String> temp = new ArrayList<>();
			for (int j = 0; j < boardSize; j++)
			     temp.add((char) (65 + j) + Integer.toString(i));
			taskRows.put(i, temp);
		}
		for (int i = 0; i < boardSize; i++) {
			ArrayList<String> temp = new ArrayList<>();
			for (int j = 0; j < boardSize; j++)
			     temp.add((char) ((65 + boardSize) - 1 - j) + Integer.toString(i));
			taskRows.put(boardSize + i, temp);
		}
		for (int i = 0; i < boardSize; i++) {
			ArrayList<String> temp = new ArrayList<>();
			for (int j = 0; j < boardSize; j++)
			     temp.add((char) (65 + i) + Integer.toString(j));
			taskRows.put(boardSize * 2 + i, temp);
		}
		for (int i = 0; i < boardSize; i++) {
			ArrayList<String> temp = new ArrayList<>();
			for (int j = 0; j < boardSize; j++)
			     temp.add((char) (65 + i) + Integer.toString(boardSize - 1 - j));
			taskRows.put(boardSize * 3 + i, temp);
		}
	}

	private void getBoard(int sizei, String link, boolean linkSet) {
		String rootLink = "https://www.puzzle-skyscrapers.com/";
		difficulty = sizei;
		if (!linkSet) {
			PuzzleGenerator.link = rootLink;
			PuzzleGenerator.link += "/?size=" + sizei;
		} else {
			PuzzleGenerator.link = link;
		}

		try {
			System.out.println("Grabbing puzzle from site...");
			URL            url    = new URL(PuzzleGenerator.link);
			URLConnection  con    = url.openConnection();
			BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));

			Pattern taskRegex = Pattern.compile("var task = '((\\d?/?)+)(.+); \\$\\(document\\)");
//			Pattern taskRegex     = Pattern.compile("var task = '(.+)';");
			Pattern puzzleIDRegex = Pattern.compile("span id=\"puzzleID\">(.+)</span>");
			String  currentLine;
			System.out.println("Formatting html into board...");
			while ((currentLine = reader.readLine()) != null) {
				Matcher matcher = puzzleIDRegex.matcher(currentLine);
				if (matcher.find()) {
					puzzleID = matcher.group(1);
				}

				matcher = taskRegex.matcher(currentLine);
				if (matcher.find()) {
					String match = matcher.group(1);

					for (int i = 0; i < match.length(); i++) {
						if (match.charAt(i) == '/') {
							task.add(0);
						} else {
							task.add(Integer.parseInt(String.valueOf(match.charAt(i))));
							i++;
						}
						if (i == match.length() - 1 && match.charAt(i) == '/')
							task.add(0);
					}

					match = matcher.group(3);
					if (!match.equals("'")) {
						hasField = true;
						StringBuilder offset    = new StringBuilder();
						int           offsetInt = 0;
						try {
							int i = 0;
							while (true) {
								i++;
								offset.delete(0, offset.length());
								while (match.charAt(i) >= 97 && match.charAt(i) <= 122) {
									offset.append(match.charAt(i));
									i++;

									if (match.charAt(i) == '\'')
										throw new StringIndexOutOfBoundsException();
								}
								if (offset.length() == 1) {
									offsetInt += offset.charAt(0) - 96;
								} else {
									offsetInt += offset.charAt(0) - 96;
									offsetInt += offset.charAt(1) - 96;
								}

								ArrayList<Integer> iveGotToDoThis = new ArrayList<>();
								iveGotToDoThis.add((int) match.charAt(i) - 48);

								field.put(offsetToPos(offsetInt), iveGotToDoThis);
								offsetInt++;
							}
						}
						catch (StringIndexOutOfBoundsException ignored) {}
//						System.exit(0);
					}
				}
			}
			if (!linkSet)
				PuzzleGenerator.link = "https://www.puzzle-skyscrapers.com/?e=" +
				                       Base64.getEncoder().encodeToString((sizei + ":" + puzzleID).getBytes());
		}
		catch (IOException e) {
			System.out.println("oopsie");
			e.printStackTrace();
		}

		generateBoard();
		System.out.println("Board done");
		System.out.println(PuzzleGenerator.link);
		// System.out.println("Seed = " + this.puzzleID + "\nTask = " + this.task + "\nLink " + this.link);
	}

	private String offsetToPos(int offset) {
		int           n      = 0;
		StringBuilder output = new StringBuilder();
		while (offset > task.size() / 4) {
			offset -= task.size() / 4;
			n++;
		}

		output.append((char) (65 + n));
		output.append(offset);
		return output.toString();
	}
}
