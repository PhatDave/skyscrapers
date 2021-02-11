package com.company;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Generates the puzzle by querying site and retrieving puzzle tasks and field, all
 * values are static
 * Creating multiple objects is strongly discouraged and may cause strange behavior
 * <p>
 * Static variables are
 *  <ul>
 *     <li>String puzzleID<pre>    ID of the puzzle generated</pre>
 *     <li>String link<pre>    link to the puzzle</pre>
 *     <li>int difficulty<pre>    puzzle difficulty (sizei in constructor)</pre>
 *     <li>Map field<pre>    board, see Board for more info</pre>
 *     <li>Map tasks<pre>    puzzle tasks, see Board for more info</pre>
 *  </ul>
 * @see com.company.Board
 */
public class PuzzleGenerator {
	static String                          puzzleID   = "";
	static ArrayList<Integer>              task       = new ArrayList<>();
	static Map<String, ArrayList<Integer>> tasks      = new HashMap<>();
	static String                          link       = "";
	static boolean                         hasField   = false;
	static int                             difficulty = 0;
	static Map<String, ArrayList<Integer>> field      = new HashMap<>();

	/**
	 * Assigns difficulty to 0 by default
	 * @deprecated use {@link #PuzzleGenerator(int)} method instead
	 * @see PuzzleGenerator Static variables available
	 */
	public PuzzleGenerator() {
		this.generate(0, "https://www.puzzle-skyscrapers.com/", false);
	}

	/**
	 * Generate a puzzle seeded by link
	 * @param sizei difficulty of puzzle generated, 0 - 2 for 4x4, 3 - 5 for 5x5, 6 - 8 for 6x6, >8 is not supported
	 * @param link link to seed puzzle by
	 * @see PuzzleGenerator Static variables available
	 */
	public PuzzleGenerator(int sizei, String link) {
		this.generate(sizei, link, true);
	}

	/**
	 * Generate a random puzzle
	 * @param sizei difficulty of puzzle generated, 0 - 2 for 4x4, 3 - 5 for 5x5, 6 - 8 for 6x6, >8 is not supported
	 * @see PuzzleGenerator Static variables available
	 */
	public PuzzleGenerator(int sizei) {
		this.generate(sizei, "https://www.puzzle-skyscrapers.com/", false);
	}

	/**
	 * Used by class' constructor
	 * @param sizei difficulty of puzzle generated, 0 - 2 for 4x4, 3 - 5 for 5x5, 6 - 8 for 6x6, >8 is not supported
	 * @param link link to seed puzzle by
	 */
	private void generate(int sizei, String link, boolean linkSet) {
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
			String  temp;
			System.out.println("Formatting html into board...");
			while ((temp = reader.readLine()) != null) {
				Matcher m = puzzleIDRegex.matcher(temp);
				if (m.find()) {
					puzzleID = m.group(1);
				}

				m = taskRegex.matcher(temp);
				if (m.find()) {
					String match = m.group(1);

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

					match = m.group(3);
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

		int                boardSize = task.size() / 4;
		ArrayList<Integer> Top       = new ArrayList<>();
		ArrayList<Integer> Bottom    = new ArrayList<>();
		ArrayList<Integer> Left      = new ArrayList<>();
		ArrayList<Integer> Right     = new ArrayList<>();
		for (int i = 0; i < boardSize; i++) {
			Top.add(task.get(i));
			Bottom.add(task.get(i + boardSize));
			Left.add(task.get(i + (2 * boardSize)));
			Right.add(task.get(i + (3 * boardSize)));
		}
		tasks.put("Top", Top);
		tasks.put("Bottom", Bottom);
		tasks.put("Left", Left);
		tasks.put("Right", Right);
		System.out.println("Board done");
		// System.out.println("Seed = " + this.puzzleID + "\nTask = " + this.task + "\nLink " + this.link);
	}

	/**
	 * Used internally by {@link #generate}
	 */
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
