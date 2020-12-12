package com.paulsen.playlist;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import com.paulsen.storage.PFile;
import com.paulsen.storage.PFolder;

import javafx.util.Duration;

public class Playlist {
	
	public static boolean loop = false, random = false;

	// details
	public String name;
	private String folderPath;

	// managing
	private ArrayList<String> files = new ArrayList<String>();
	private ArrayList<String> playedFiles = new ArrayList<String>();

	Player currentPlayer;
	int playlistIndex = 0;

	float volume = 1f;

	private boolean reachedEndOfPlaylist = false; // only for loop=false & random=true

	public Playlist() { // empty
	}

	public Playlist(String folderPath) {
		this.folderPath = folderPath;
		addFolder(folderPath);
	}

	public synchronized boolean addFile(String path) {
		if (path != null && new File(path).exists() && Player.isFilePlayable(path)) {
			files.add(path);
			return true;
		}
		System.err.println("Couldn't add " + path + " to Playlist");
		return false;
	}

	public synchronized void removeFile(int index) {
		System.out.println("[Playlist] :: " + (folderPath == null ? name : folderPath) + " :: remove " + index);
		files.remove(index);
	}

	public synchronized boolean addFolder(String path) {

		ArrayList<String> files = new ArrayList<>();

		for (String filetype : Player.ALLOWEDFILETYPES) {
			String array[] = PFolder.getFiles(path, filetype);
			for (String s : array) {
				files.add(s);
//				if (folderPath != null && !folderPath.isEmpty())
//					System.out.println("[Playlist] :: " + folderPath + " :: add(" + s + ")");
//				else
//					System.out.println("[Playlist] :: " + name + " :: add(" + s + ")");
			}
		}
		if (folderPath != null && !folderPath.isEmpty())
			System.out.println("[Playlist] :: add " + folderPath + " :: " + files.size() + " files");
		else
			System.out.println("[Playlist] :: add " + name + " :: " + files.size() + " files");
		this.files.addAll(files);

		return files.size() > 0;
	}

	public synchronized void setVolume(float f) {
		volume = f;
		if (isPlayable() && currentPlayer != null) {
			currentPlayer.setVolume(volume);
		}
	}

	public synchronized boolean isPlaying() {
		if (currentPlayer != null) {
			return currentPlayer.isPlaying();
		}
		return false;
	}

	public synchronized void pause_play() {
		if (currentPlayer != null) {
			currentPlayer.pause_play();
		} else {
			play();
		}
	}

	public synchronized void pause_play(int playlistIndex) {
		if (this.playlistIndex != playlistIndex) {
			this.playlistIndex = playlistIndex;
			play(playlistIndex);
		} else {
			pause_play();
		}
	}

	public synchronized void play() {
		if (isPlayable()) {
			initSong(files.get(playlistIndex));
			if (!loop && random)
				playedFiles.add(files.get(playlistIndex));
			currentPlayer.play();
			currentPlayer.setVolume(volume);
		}
	}

	public synchronized void play(int playlistIndex) {
		if (isPlayable()) {
			this.playlistIndex = playlistIndex;
			initSong(files.get(playlistIndex));
			if (!loop && random)
				playedFiles.add(files.get(playlistIndex));
			currentPlayer.play();
			currentPlayer.setVolume(volume);
		}
	}

	public synchronized void playLast() {
		if (isPlayable()) {
			initLast();
			play();
		}
	}

	public synchronized void playNext() {
		if (isPlayable()) {
			if (initNext())
				play();
		}
	}

	private synchronized void initLast() {
		if (currentPlayer != null)
			currentPlayer.end();

		if (files.size() > 0)
			if (loop) {
				if (random) { // loop && random
					int nIndex = (int) (Math.random() * files.size());
					playlistIndex = nIndex;
					initSong(files.get(playlistIndex));
				} else { // loop && !random
					playlistIndex--;
					if (playlistIndex < 0)
						playlistIndex = files.size() - 1;
					initSong(files.get(playlistIndex));
				}
			} else if (random) { // !loop && random
				int lastIndex = 0;
				if (playedFiles.size() == 0) {

				} else if (playedFiles.size() == 1) {
					lastIndex = getPlaylistIndex(playedFiles.get(0));
					playedFiles.remove(playedFiles.size() - 1);
				} else if (playedFiles.size() == files.size() && reachedEndOfPlaylist) {
					reachedEndOfPlaylist = false;
					lastIndex = getPlaylistIndex(playedFiles.get(playedFiles.size() - 1));
					playedFiles.remove(playedFiles.size() - 1);
				} else {
					lastIndex = getPlaylistIndex(playedFiles.get(playedFiles.size() - 2));
					playedFiles.remove(playedFiles.size() - 1);
					playedFiles.remove(playedFiles.size() - 1);
				}
				playlistIndex = lastIndex;
			} else {// !loop && !random
				if (playlistIndex > 0) {
					playlistIndex--;
					initSong(files.get(playlistIndex));
				}
			}
	}

	private synchronized boolean initNext() {
		if (currentPlayer != null)
			currentPlayer.end();

		if (files.size() > 0)
			if (loop) {
				if (random) { // loop && random
					int nIndex = (int) (Math.random() * files.size());
					playlistIndex = nIndex;
					initSong(files.get(playlistIndex));
					return true;
				} else { // loop && !random
					playlistIndex++;
					if (playlistIndex >= files.size())
						playlistIndex = 0;
					initSong(files.get(playlistIndex));
					return true;
				}
			} else if (random) { // !loop && random
				if (playedFiles.size() == files.size()) {
					reachedEndOfPlaylist = true;
					return false;
				} else if (playedFiles.size() < files.size()) {
					int nIndex;
					do {
						nIndex = (int) (Math.random() * files.size());
					} while (hasSongBeenPlayed(files.get(nIndex)));
					playlistIndex = nIndex;
					initSong(files.get(playlistIndex));
					return true;
				}
			} else {// !loop && !random
				if (playlistIndex + 1 < files.size()) {
					playlistIndex++;
					initSong(files.get(playlistIndex));
					return true;
				}
			}
		return false;
	}

	public void seek(double factor) {
		currentPlayer.seek(factor);
	}

	public double getDurationFactor() {
		return currentPlayer.getDurationFactor();
	}

	public Duration getTotalDuration() {
		if (currentPlayer != null)
			return currentPlayer.getTotalDuration();
		return null;
	}

	public Duration getCurrentDuration() {
		if (currentPlayer != null)
			return currentPlayer.getCurrentDuration();
		return null;
	}

	public String getFolderPath() {
		return folderPath;
	}

	public boolean isLoop() {
		return loop;
	}

	public boolean isRandom() {
		return random;
	}

	public String getCurrentFileName() {
		if (currentPlayer != null) {
			return currentPlayer.getFileName();
		}
		return null;
	}

	public String getFileName(int index) {
		return PFile.getName(files.get(index));
	}

	public void setLoop(boolean b) {
		loop = b;
		if (!loop && random) {
			playedFiles.clear();
		}
	}

	public void setRandom(boolean b) {
		random = b;
		if (!loop && random) {
			playedFiles.clear();
		}
	}

	public boolean isPlayable() {
		if (files.size() > 0)
			return true;
		return false;
	}

	private boolean hasSongBeenPlayed(String path) {
		for (int i = 0; i < playedFiles.size(); i++)
			if (playedFiles.get(i).equals(path))
				return true;
		return false;
	}

	public int getPlaylistIndex(String file) {
		for (int i = 0; i < files.size(); i++)
			if (file.equals(files.get(i)))
				return i;
		return -1;
	}

	private void initSong(String path) {
		// init
		try {
			currentPlayer = new Player(path);
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		currentPlayer.runOnEndOfFile(new Runnable() {
			@Override
			public void run() {
				System.out.println("[Playlist] :: EndOfFile");
				// play next according to loop and random variables
				playNext();
			}
		});
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isFolder() {
		return !(folderPath == null || folderPath.isEmpty());
	}

	@Override
	public String toString() {
		if (folderPath == null || folderPath.isEmpty()) {
			return "Playlist: " + name; // custom
		}
		return "Folder: " + name; // folder
	}

	public ArrayList<String> getFiles() {
		return files;
	}

}
