package com.paulsen.storage;

import java.io.File;
import java.util.ArrayList;

public class PFolder {

	public static boolean createFolder(String path) {
		if (isFile(path))
			return false;
		new File(path).mkdirs();
		return true;
	}

	public static boolean isFolder(String path) {
		return ((!isFile(path)) && new File(path).exists());
	}

	private static boolean isFile(String path) {
//		System.out.println("   asdfasdf " + ((char) 92));
		for (int i = path.length() - 1; i >= 0; i--) {
			if (path.charAt(i) == '/' || path.charAt(i) == ((char) 92))
				return false;
			if (path.charAt(i) == '.')
				return true;
		}
		if (new File(path).isFile())
			return true;
		return false;
	}

	public static String getName(String path) {
		String s = "";
		for (int i = path.length() - 1; i >= 0; i--) {
			if (path.charAt(i) == '/' || path.charAt(i) == ((char) 92))
				break;
			s = path.charAt(i) + s;
		}
		return s;
	}

	public static String[] getFiles(String path, String fileType) {
		String[] s = new File(path).list();
		if (s == null || s.length == 0)
			return null;

		ArrayList<String> list = new ArrayList<String>();
		for (int i = 0; i < s.length; i++)
			if (isFile(s[i]) && s[i].endsWith(fileType))
				list.add(path + "/" + s[i]);

		String sN[] = new String[list.size()];
		for (int i = 0; i < sN.length; i++)
			sN[i] = list.get(i);
		return sN;
	}

	public static String[] getSubFolders(String path) {
		String s[] = new File(path).list();
		if (s == null || s.length == 0)
			return null;

		ArrayList<String> list = new ArrayList<String>();
		for (int i = 0; i < s.length; i++)
			if (!isFile(s[i]))
				list.add(path + "/" + s[i]);

		String sN[] = new String[list.size()];
		for (int i = 0; i < sN.length; i++) {
			sN[i] = list.get(i);
		}
		return sN;
	}
}
