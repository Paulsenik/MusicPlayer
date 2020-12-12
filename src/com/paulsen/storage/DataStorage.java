package com.paulsen.storage;

import java.util.ArrayList;

/**
 * Class for saving data of any type
 * 
 * @author Paul Seidel
 * @version 1.0.1
 * @since 2019-01-01
 * @see Last Updated 2020/10/05
 */

public class DataStorage {

	public static final char BLOCK_START = '[', BLOCK_END = ']';
	/**
	 * Replacement if start- or end-character is included in data (1)=> store data
	 * with replaced characters (2)=> read data with replacements back replaced to
	 * start or end
	 */
	public static final String START_REPLACEMENT = "^<<^", END_REPLACEMENT = "^>>^";

	private String path;

	public DataStorage(String path) {
		this.path = path;
	}

	public synchronized String[] read() {
		PFile pf = new PFile(path);
		if (pf.exists()) {
			ArrayList<String> out = new ArrayList<>();
			String in = pf.getFileAsString();
			String tempW = "";
			for (int i = 0; i < in.length(); i++) {
				if (in.charAt(i) == BLOCK_END) {

					/**
					 * replaces REPLACEMENTS with start/end and than stores the finished String as
					 * new DataBlock to the array
					 */
					out.add(tempW.replace(START_REPLACEMENT, Character.toString(BLOCK_START)).replace(END_REPLACEMENT,
							Character.toString(BLOCK_END)));
					tempW = "";
				} else if (in.charAt(i) != BLOCK_START)
					tempW += in.charAt(i);
			}
			if (tempW.length() != 0) {
				out.add(tempW);
			}
			String array[] = new String[out.size()];
			for (int i = 0; i < out.size(); i++)
				array[i] = out.get(i);
			return array;
		} else
			return null;
	}

	public synchronized boolean write(String s[]) {
		PFile f = new PFile(path);
		if (f.exists()) {
			String input = "";
			/**
			 * replaces START/END with REPLACEMENTS and than stores the finished String as
			 * new DataBlock
			 */
			s = replace(replace(s, Character.toString(BLOCK_START), START_REPLACEMENT), Character.toString(BLOCK_END),
					END_REPLACEMENT);
			for (String S : s)
				input += BLOCK_START + S + BLOCK_END;
			f.writeFile(input);
			return true;
		}
		return false;
	}

	private String[] replace(String s[], String target, String replacement) {
		String aNew[] = new String[s.length];
		for (int i = 0; i < s.length; i++) 
			aNew[i] = s[i].replace(target, replacement);
		return aNew;
	}
	
	public String getPath() {
		return path;
	}
}
