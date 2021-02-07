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

public class PuzzleGenerator {
	String                          puzzleID = "";
	ArrayList<Integer>              task     = new ArrayList<>();
	Map<String, ArrayList<Integer>> tasks    = new HashMap<>();
	String                          link     = "";

	public PuzzleGenerator() {
		this.deder(0, "https://www.puzzle-skyscrapers.com/", false);
	}

	public PuzzleGenerator(int sizei, String link) {
		this.deder(sizei, link, true);
	}

	public PuzzleGenerator(int sizei) {
		this.deder(sizei, "https://www.puzzle-skyscrapers.com/", false);
	}

	public void deder(int sizei, String link, boolean linkSet) {
		String rootLink = "https://www.puzzle-skyscrapers.com/";
		if (!linkSet) {
			this.link = rootLink;
		} else {
			this.link = link;
		}
		URL url;

		String size = Integer.toString(sizei);
		if (sizei != 0 && sizei != 3 && sizei != 6) {
			System.out.println("Invalid size");
			return;
		} else if ((sizei == 3 || sizei == 6) && !linkSet) {
			this.link += "/?size=" + size;
		}

		try {
			url = new URL(this.link);
			URLConnection  con    = url.openConnection();
			BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));

			Pattern taskRegex     = Pattern.compile("var task = '(.+)';");
			Pattern puzzleIDRegex = Pattern.compile("span id=\"puzzleID\">(.+)</span>");
			String  temp;
			while ((temp = reader.readLine()) != null) {
				Matcher m = puzzleIDRegex.matcher(temp);
				if (m.find()) {
					this.puzzleID = m.group(1);
				}

				m = taskRegex.matcher(temp);
				if (m.find()) {
					for (String c : m.group(1).split("/")) {
						this.task.add(Integer.parseInt(c));
					}
				}
			}
			this.link = "https://www.puzzle-skyscrapers.com/?e=" +
			            Base64.getEncoder().encodeToString((size + ":" + this.puzzleID).getBytes());
		}
		catch (IOException e) {
			System.out.println("oopsie");
			e.printStackTrace();
		}

		int                boardSize = this.task.size() / 4;
		ArrayList<Integer> Top       = new ArrayList<>();
		ArrayList<Integer> Bottom    = new ArrayList<>();
		ArrayList<Integer> Left      = new ArrayList<>();
		ArrayList<Integer> Right     = new ArrayList<>();
		for (int i = 0; i < boardSize; i++) {
			Top.add(this.task.get(i));
			Bottom.add(this.task.get(i + boardSize));
			Left.add(this.task.get(i + (2 * boardSize)));
			Right.add(this.task.get(i + (3 * boardSize)));
		}
		this.tasks.put("Top", Top);
		this.tasks.put("Bottom", Bottom);
		this.tasks.put("Left", Left);
		this.tasks.put("Right", Right);
		// System.out.println("Seed = " + this.puzzleID + "\nTask = " + this.task + "\nLink " + this.link);
	}
}
