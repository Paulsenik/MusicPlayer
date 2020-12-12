package com.paulsen.playlist;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;

import com.paulsen.storage.PFile;
import com.sun.javafx.application.PlatformImpl;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaPlayer.Status;
import javafx.util.Duration;

@SuppressWarnings("restriction")
public class Player {

	/*
	 * All filetypes the player allows
	 */
	public final static String ALLOWEDFILETYPES[] = { "wav", "mp3", "aac", "pcm", "aiff" };

	public static boolean isFilePlayable(String path) {
		String fileType = getFileType(path);
		if (fileType == null) // no fileType
			return false;
		for (int i = 0; i < ALLOWEDFILETYPES.length; i++) {
			if (fileType.equals(ALLOWEDFILETYPES[i]))
				return true;
		}
		return false;
	}

	public static String getFileType(String file) {
		String fileType = "";
		for (int i = file.length() - 1; i >= 0; i--) {
			if (file.charAt(i) == '.') {
				return fileType;
			} else {
				fileType = file.charAt(i) + fileType;
			}
		}
		return null;
	}

	private MediaPlayer mp;
	private Media m;
	String path;

	public Player(String path) throws FileNotFoundException, UnsupportedEncodingException {
		if (new File(path).exists()) {
			if (isFilePlayable(path)) {
				this.path = path;
				init();
			} else
				throw new UnsupportedEncodingException("File is not playable!");
		} else
			throw new FileNotFoundException("No playable file found!");
		
	}

	private void init() {
		PlatformImpl.startup(() -> {
		});
		m = new Media(new File(path).toURI().toString());
		mp = new MediaPlayer(m);
	}

	public String getFileName() {
		return PFile.getName(path);
	}

	/*
	 * pauses if isPlaying, plays if paused
	 */
	public void pause_play() {
		if (mp.getStatus() != Status.DISPOSED)
			if (mp.getStatus() == Status.PAUSED) {
				play();
			} else {
				pause();
			}
	}

	public synchronized boolean isPlaying() {
		if (mp != null)
			return mp.getStatus() == Status.PLAYING;
		return false;
	}

	public void runOnEndOfFile(Runnable r) {
		mp.setOnEndOfMedia(r);
	}

	public void pause() {
		if (mp.getStatus() != Status.DISPOSED) {
			mp.pause();
			System.out.println("[Player] :: pausing --> " + m.getSource());
		}
	}

	public void play() {
		if (mp.getStatus() != Status.DISPOSED) {
			mp.play();
			System.out.println("[Player] :: playing --> " + m.getSource());
		}
	}

	public void setVolume(float f) {
		if (mp.getStatus() != Status.DISPOSED) {
			mp.setVolume(f);
			System.out.println("[Player] :: setVolume=" + f + " --> " + m.getSource());
		}
	}

	public void setBalance(float f) { // -1=left 1=right
		if (mp.getStatus() != Status.DISPOSED)
			mp.setBalance(f);
	}

	public void setRate(float f) {
		if (mp.getStatus() != Status.DISPOSED)
			mp.setRate(f);
	}

	public void seek(double factor) { // 0f-1f
		Duration d = m.getDuration().multiply(factor);
		mp.seek(d);
		System.out.println("[Player] :: skipped to " + d.toMillis() + "ms");
	}

	public Duration getTotalDuration() {
		return mp.getTotalDuration();
	}

	public Duration getCurrentDuration() {
		return mp.getCurrentTime();
	}

	public double getDurationFactor() { // 0f-1f
		return mp.getCurrentTime().toMillis() / m.getDuration().toMillis();
	}

	public void end() {
		mp.dispose();
	}
}
