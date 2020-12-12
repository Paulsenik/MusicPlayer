package com.paulsen.ui.frames;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JScrollBar;

import com.paulsen.MusicPlayerManager;
import com.paulsen.storage.PFile;
import com.paulsen.storage.PFolder;
import com.paulsen.ui.FrameManager;

import javafx.util.Duration;

public class MainFrame extends JLabel {
	private static final long serialVersionUID = 1L;
	private final String TITLE = "Paulsens Musicplayer [V0.9.8]";
	public static final String font = "Noto Emoji";
	public final int MW = 50; // Menu-width
	public final int BH = 150; // Bar-height
	public final int BS = 60; // Button-size
	public final int BDS = 10; // Button-Draw-Space => space between drawing and bounds
	public final int EH = 60; // Element-height
	public final int DB = 2; // Durationbar-height
	public final int OS = 18; // size of Oval for Durationdisplay
	private int space = 0;

	public int playlistFileDivider = 0; // separation btw PlaylistButtons & FileButtons
	public int playlistScrollOffset = 0, songScrollOffset = 0;
	public boolean hasToUpdateWindow = false;
	private boolean isDrag = false;

	public int w, h; // width, height
	int menuIndex = 0; // 0=Folders, 1=Playlists(custom)
	int dragValue = 0;

	long lastDragged = 0;
	boolean hasDragged = false;

	public MusicPlayerManager musicPlayer;
	public ArduinoFrame arduinoFrame;
	public GeneralFrame generalFrame;
	public JFrame jf;
	public JMenuBar mb;

	ArrayList<JButton> customPlaylists = new ArrayList<>();
	ArrayList<JButton> folders = new ArrayList<>();
	ArrayList<JButton> files = new ArrayList<>();
	public JButton playlistButton, folderButton, pausePlay, last, next, rand, loop;
	public JButton durationShifter;
	public JScrollBar volume;

	public MainFrame(MusicPlayerManager mpm) {

		// Main-Frame
		musicPlayer = mpm;
		jf = new JFrame(TITLE);
		jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jf.setMinimumSize(new Dimension(500, 500));

		if (mpm.presetMainFramePosition != null) {
			jf.setSize((int) mpm.presetMainFramePosition.getWidth(), (int) mpm.presetMainFramePosition.getHeight());
			jf.setLocation((int) mpm.presetMainFramePosition.x, (int) mpm.presetMainFramePosition.y);
		} else {
			jf.setSize(1300, 1000);
			jf.setLocationRelativeTo(null);
		}

		FrameManager wm = new FrameManager(this);
		jf.addKeyListener(wm);
		jf.addMouseWheelListener(wm);

		wm.initMenu();
		initComponents();
		updatePlaylistButtons(); // create playlistButtons

		// Arduino-Frame
		arduinoFrame = new ArduinoFrame(musicPlayer);

		// General-Frame
		generalFrame = new GeneralFrame(musicPlayer);

		jf.add(this);
		jf.setVisible(true);

		initThread();
		updateGraphics();
	}

	protected void paintComponent(Graphics g) { // GRAPHICS
		try {
			super.paintComponent(g);
			Graphics2D g2d = (Graphics2D) g;
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			Color sel = new Color(201, 201, 201); // selected-button
			Color notSel = new Color(81, 81, 81);

			// Background
			g.setColor(new Color(51, 51, 51));
			g.fillRect(0, 0, w, h);

			// Playlists
			if (menuIndex == 0) { // folder

				for (int i = 0; i < folders.size(); i++) {
					if (folders.get(i).isVisible()) {
						boolean isPlaylistSel = musicPlayer.selectedPlaylist != null
								&& musicPlayer.selectedPlaylist.getFolderPath() != null && musicPlayer.selectedPlaylist
										.getFolderPath().equals(musicPlayer.folders.get(i).getFolderPath());
						if (isPlaylistSel) {
							g.setColor(new Color(131, 131, 131));
							g.fillRect(folders.get(i).getX(), folders.get(i).getY(),
									folders.get(i).getWidth()
											+ ((getWidth() - MW) / 2 - folders.get(i).getWidth()) / 2/* getSpace */,
									folders.get(i).getHeight());
						} else {
							g.setColor(new Color(81, 81, 81));
							g.fillRect(folders.get(i).getX(), folders.get(i).getY(), folders.get(i).getWidth(),
									folders.get(i).getHeight());
						}
						g.setColor(Color.white);
						g.setFont(new Font(font, 0, EH));
						g.drawString(PFolder.getName(musicPlayer.folders.get(i).getFolderPath()),
								(int) (folders.get(i).getX() + folders.get(i).getHeight() * .1),
								(int) (folders.get(i).getY() + folders.get(i).getHeight() * .9));

						// cut off Strings
						g.setColor(new Color(51, 51, 51)); // backgroundcolor
						g.fillRect(folders.get(i).getX() + folders.get(i).getWidth(), folders.get(i).getY() - space,
								folders.get(i).getWidth(), folders.get(i).getHeight() + space * 2);

						if (isPlaylistSel) {
							g.setColor(new Color(131, 131, 131));
							g.fillRect(folders.get(i).getX() + folders.get(i).getWidth(), folders.get(i).getY(),
									((getWidth() - MW) / 2 - folders.get(i).getWidth()) / 2/* getSpace */,
									folders.get(i).getHeight());

						}
					}
				}

			} else if (menuIndex == 1) { // custom

				for (int i = 0; i < customPlaylists.size(); i++) {
					if (customPlaylists.get(i).isVisible()) {
						boolean isFolderSel = musicPlayer.selectedPlaylist != null
								&& musicPlayer.selectedPlaylist.name != null
								&& musicPlayer.customPlaylists.get(i).name.equals(musicPlayer.selectedPlaylist.name);
						if (isFolderSel) {
							g.setColor(new Color(131, 131, 131));
							g.fillRect(customPlaylists.get(i).getX(), customPlaylists.get(i).getY(), customPlaylists
									.get(i).getWidth()
									+ ((getWidth() - MW) / 2 - customPlaylists.get(i).getWidth()) / 2/* getSpace */,
									customPlaylists.get(i).getHeight());
						} else {
							g.setColor(new Color(81, 81, 81));
							g.fillRect(customPlaylists.get(i).getX(), customPlaylists.get(i).getY(),
									customPlaylists.get(i).getWidth(), customPlaylists.get(i).getHeight());
						}
						g.setColor(Color.white);
						g.setFont(new Font(font, 0, EH));
						g.drawString(musicPlayer.customPlaylists.get(i).name,
								(int) (customPlaylists.get(i).getX() + customPlaylists.get(i).getHeight() * .1),
								(int) (customPlaylists.get(i).getY() + customPlaylists.get(i).getHeight() * .9));

						// cut off Strings
						g.setColor(new Color(51, 51, 51)); // backgroundcolor
						g.fillRect(customPlaylists.get(i).getX() + customPlaylists.get(i).getWidth(),
								customPlaylists.get(i).getY() - space, customPlaylists.get(i).getWidth(),
								customPlaylists.get(i).getHeight() + space * 2);

						if (isFolderSel) {
							g.setColor(new Color(131, 131, 131));
							g.fillRect(customPlaylists.get(i).getX() + customPlaylists.get(i).getWidth(),
									customPlaylists.get(i).getY(),
									((getWidth() - MW) / 2 - customPlaylists.get(i).getWidth()) / 2/* getSpace */,
									customPlaylists.get(i).getHeight());

						}
					}
				}
			}

			// Songs
			g.setColor(new Color(131, 131, 131));
			g.fillRect(playlistFileDivider, 0, w - playlistFileDivider, h - BH);

			// set the number of songs that should be rendered
			for (int i = 0; i < files.size(); i++) {
				if (files.get(i).isVisible()) {
					if (musicPlayer.selectedPlaylist != null && musicPlayer.getCurrentPlaylist() != null
							&& musicPlayer.getCurrentPlaylist().equals(musicPlayer.selectedPlaylist)
							&& musicPlayer.getIndexOfCurrentFile() == i) {
						g.setColor(new Color(201, 201, 201)); // sel
						g.fillRect(files.get(i).getX(), files.get(i).getY(),
								files.get(i).getWidth() + files.get(i).getWidth() / 10, files.get(i).getHeight());
					} else {
						g.setColor(new Color(81, 81, 81)); // notSel
						g.fillRect(files.get(i).getX(), files.get(i).getY(), files.get(i).getWidth(),
								files.get(i).getHeight());
					}
					g.setColor(Color.white);
					g.setFont(new Font(font, 0, EH));
					g.drawString(PFile.getName(musicPlayer.selectedPlaylist.getFiles().get(i)),
							(int) (files.get(i).getX() + files.get(i).getHeight() * .1),
							(int) (files.get(i).getY() + files.get(i).getHeight() * .9));
				}
			}

			// Menu
			g.setColor(new Color(61, 61, 61));
			g.fillRect(0, MW, MW, h);

			Color menuCol = menuIndex == 0 ? sel : notSel;
			g.setColor(menuCol);
			g.fillRect(folderButton.getX(), folderButton.getY(), folderButton.getWidth(), folderButton.getHeight());
			g.setColor(Color.white);
			drawFolder(g);

			g.setColor(menuIndex == 1 ? sel : notSel);
			g.fillRect(playlistButton.getX(), playlistButton.getY(), playlistButton.getWidth(),
					playlistButton.getHeight());
			g.setColor(Color.white);
			drawPlaylist(g);

			// BAR
			g.setColor(new Color(61, 61, 61));
			g.fillRect(0, h - BH, w, BH);

			g.setColor(musicPlayer.getCurrentPlaylist() != null && musicPlayer.getCurrentPlaylist().isRandom() ? sel
					: notSel);
			g.fillRect(rand.getX(), rand.getY(), rand.getWidth(), rand.getHeight());
			g.setColor(Color.white);
			drawRand(g);

			g.setColor(musicPlayer.getCurrentPlaylist() != null && musicPlayer.getCurrentPlaylist().isLoop() ? sel
					: notSel);
			g.fillRect(loop.getX(), loop.getY(), loop.getWidth(), loop.getHeight());
			g.setColor(Color.white);
			drawLoop(g);

			g.setColor(musicPlayer.getCurrentPlaylist() != null ? sel : notSel);
			g.fillRect(last.getX(), last.getY(), last.getWidth(), last.getHeight());
			g.fillRect(next.getX(), next.getY(), next.getWidth(), next.getHeight());
			g.setColor(Color.white);
			drawNext(g);
			drawLast(g);

			g.setColor(musicPlayer.selectedPlaylist != null ? sel : notSel);
			g.fillRect(pausePlay.getX(), pausePlay.getY(), pausePlay.getWidth(), pausePlay.getHeight());
			g.setColor(Color.white);
			drawPausePlay(g);

			g.setColor(Color.white);
			g.fillRoundRect(rand.getX() + OS / 2, h - BH + BH / 4 - DB / 2, w - rand.getX() * 2 - OS, DB, DB, DB);

			if (dragValue != 0) {
				g.setColor(Color.white);
				g.fillRect(durationShifter.getX() + durationShifter.getWidth() / 2 - 2, durationShifter.getY(), 4,
						durationShifter.getHeight());
				if (musicPlayer.getCurrentPlaylist() != null) {
					int offset = (int) ((w - rand.getX() * 2 - OS)
							* musicPlayer.getCurrentPlaylist().getDurationFactor());
					g.setColor(Color.white);
					g.fillOval(rand.getX() + offset + dragValue - OS / 2, h - BH + BH / 4 - OS / 2, OS, OS);
					g.setColor(new Color(61, 61, 61));
					g.fillOval(rand.getX() + offset + 1 + dragValue - OS / 2, h - BH + BH / 4 - OS / 2 + 1, OS - 2,
							OS - 2);
					durationShifter.setBounds(rand.getX() + offset, h - BH + BH / 4 - OS / 2, OS, OS);
				}
			} else {
				g.setColor(Color.white);
				g.fillOval(durationShifter.getX(), durationShifter.getY(), durationShifter.getWidth(),
						durationShifter.getHeight());
				g.setColor(new Color(61, 61, 61));
				g.fillOval(durationShifter.getX() + 1, durationShifter.getY() + 1, durationShifter.getWidth() - 2,
						durationShifter.getHeight() - 2);
			}
			if (musicPlayer.getCurrentPlaylist() != null) {
				g.setColor(Color.white);
				g.setFont(new Font(font, 0, BH / 4));
				g.drawString(getTime(musicPlayer.getCurrentPlaylist().getCurrentDuration()) + "/"
						+ getTime(musicPlayer.getCurrentPlaylist().getTotalDuration()), OS, (int) (h - BH / 2));
			}

			// volume
			g.setColor(Color.white);
			g.setFont(new Font(font, 0, BH / 4));
			g.drawString(((double) (Math.round(musicPlayer.getVolume() * 200 * 10)) / 10) + "%",
					volume.getX() + volume.getWidth() / 2 - BH / 4, volume.getY());
		} catch (IndexOutOfBoundsException e) {
//			e.printStackTrace();
			repaint();
		}
	}

	private void drawFolder(Graphics g) {
		int BDS = this.BDS / 2;
		int th = (rand.getHeight() - BDS * 2) / 5; // Thickness
		g.drawRoundRect(folderButton.getX() + BDS, folderButton.getY() + BDS, (int) (th * 1.5),
				folderButton.getHeight() - BDS * 2 - th / 2, th, th);
		g.setColor(Color.white);
		g.fillRoundRect(folderButton.getX() + BDS, folderButton.getY() + BDS + th / 2,
				folderButton.getWidth() - BDS * 2, folderButton.getHeight() - BDS * 2 - th / 2, th, th);
	}

	private void drawPlaylist(Graphics g) {
		int BDS = this.BDS / 2;
		int th = (rand.getHeight() - BDS * 2) / 5; // Thickness
		g.fillOval(playlistButton.getX() + BDS, playlistButton.getY() + playlistButton.getHeight() / 2,
				playlistButton.getWidth() / 2 - BDS, playlistButton.getHeight() / 2 - BDS);
		g.fillRect(playlistButton.getX() + playlistButton.getWidth() / 2 - th, playlistButton.getY() + BDS, th,
				(int) ((playlistButton.getHeight() - BDS * 2) * 0.75));

		int[][] t1 = new int[2][3]; // [x+y][n]
		t1[0][0] = playlistButton.getX() + playlistButton.getWidth() / 2;
		t1[1][0] = playlistButton.getY() + BDS;
		t1[0][1] = playlistButton.getX() + playlistButton.getWidth() / 2;
		t1[1][1] = playlistButton.getY() + BDS + th;
		t1[0][2] = playlistButton.getX() + playlistButton.getWidth() - BDS;
		t1[1][2] = playlistButton.getY() + playlistButton.getHeight() / 2;
		g.fillPolygon(t1[0], t1[1], 3);
	}

	private void drawRand(Graphics g) {
		int th = (rand.getHeight() - BDS * 2) / 5; // Thickness
		g.fillRect(rand.getX() + BDS, rand.getY() + BDS, th, th);
		g.fillRect(rand.getX() + BDS, rand.getY() + rand.getHeight() - BDS - th, th, th);

		int[][] p1 = new int[2][4]; // [x+y][n]
		p1[0][0] = rand.getX() + BDS + th;
		p1[1][0] = rand.getY() + BDS;
		p1[0][1] = rand.getX() + rand.getWidth() - BDS - th;
		p1[1][1] = rand.getY() + rand.getHeight() - BDS - th;
		p1[0][2] = rand.getX() + rand.getWidth() - BDS - th;
		p1[1][2] = rand.getY() + rand.getHeight() - BDS;
		p1[0][3] = rand.getX() + BDS + th;
		p1[1][3] = rand.getY() + BDS + th;
		g.fillPolygon(p1[0], p1[1], 4);

		int[][] p2 = new int[2][4];
		p2[0][0] = rand.getX() + BDS + th;
		p2[1][0] = rand.getY() + rand.getHeight() - BDS;
		p2[0][1] = rand.getX() + rand.getWidth() - BDS - th;
		p2[1][1] = rand.getY() + BDS + th;
		p2[0][2] = rand.getX() + rand.getWidth() - BDS - th;
		p2[1][2] = rand.getY() + BDS;
		p2[0][3] = rand.getX() + BDS + th;
		p2[1][3] = rand.getY() + rand.getHeight() - BDS - th;
		g.fillPolygon(p2[0], p2[1], 4);

		int[][] t1 = new int[2][3]; // [x+y][n]
		t1[0][0] = rand.getX() + rand.getWidth() - BDS - th;
		t1[1][0] = (int) (rand.getY() + BDS - th * 0.5);
		t1[0][1] = rand.getX() + rand.getWidth() - BDS - th;
		t1[1][1] = (int) (rand.getY() + BDS + th * 1.5);
		t1[0][2] = rand.getX() + rand.getWidth() - BDS + th / 2;
		t1[1][2] = (int) (rand.getY() + BDS + 0.5 * th);
		g.fillPolygon(t1[0], t1[1], 3);

		int[][] t2 = new int[2][3]; // [x+y][n]
		t2[0][0] = rand.getX() + rand.getWidth() - BDS - th;
		t2[1][0] = (int) (rand.getY() + rand.getHeight() - BDS + th * 0.5);
		t2[0][1] = rand.getX() + rand.getWidth() - BDS - th;
		t2[1][1] = (int) (rand.getY() + rand.getHeight() - BDS - th * 1.5);
		t2[0][2] = rand.getX() + rand.getWidth() - BDS + th / 2;
		t2[1][2] = (int) (rand.getY() + rand.getHeight() - BDS - 0.5 * th);
		g.fillPolygon(t2[0], t2[1], 3);
	}

	private void drawLoop(Graphics g) {
		int th = (loop.getHeight() - BDS * 2) / 5; // Thickness
		g.fillRect(loop.getX() + BDS + th, loop.getY() + BDS, loop.getWidth() - BDS * 2 - th, th);
		g.fillRect(loop.getX() + loop.getWidth() - BDS - th, loop.getY() + BDS, th, loop.getHeight() - BDS * 2 - th);
		g.fillRect(loop.getX() + BDS, loop.getY() + loop.getHeight() - BDS - th, loop.getWidth() - BDS * 2, th);

		int[][] t = new int[2][3]; // [x+y][n]
		t[0][0] = loop.getX() + BDS - th / 2;
		t[1][0] = loop.getY() + BDS + th / 2;
		t[0][1] = loop.getX() + BDS + th;
		t[1][1] = loop.getY() + BDS - th / 2;
		t[0][2] = loop.getX() + BDS + th;
		t[1][2] = (int) (loop.getY() + BDS + th * 1.5);
		g.fillPolygon(t[0], t[1], 3);
	}

	private void drawNext(Graphics g) {
		int[][] p = new int[2][3]; // [x+y][n]
		p[0][0] = next.getX() + BDS;
		p[1][0] = next.getY() + BDS;
		p[0][1] = next.getX() + BDS;
		p[1][1] = next.getY() + next.getHeight() - BDS;
		p[0][2] = next.getX() + next.getWidth() - BDS - next.getWidth() / 7;
		p[1][2] = next.getY() + next.getHeight() / 2;
		g.fillPolygon(p[0], p[1], 3);

		g.fillRect(next.getX() + next.getWidth() - BDS - next.getWidth() / 7, next.getY() + BDS, next.getWidth() / 7,
				next.getHeight() - BDS * 2);
	}

	private void drawLast(Graphics g) {
		int[][] p = new int[2][3]; // [x+y][n]
		p[0][0] = last.getX() + last.getWidth() - BDS;
		p[1][0] = last.getY() + BDS;
		p[0][1] = last.getX() + last.getWidth() - BDS;
		p[1][1] = last.getY() + last.getHeight() - BDS;

		p[0][2] = last.getX() + BDS + last.getWidth() / 7;
		p[1][2] = last.getY() + last.getHeight() / 2;
		g.fillPolygon(p[0], p[1], 3);

		g.fillRect(last.getX() + BDS, last.getY() + BDS, last.getWidth() / 7, last.getHeight() - BDS * 2);
	}

	private void drawPausePlay(Graphics g) {
		int[][] p = new int[2][3]; // [x+y][n]
		p[0][0] = pausePlay.getX() + BDS;
		p[1][0] = pausePlay.getY() + BDS;
		p[0][1] = pausePlay.getX() + BDS;
		p[1][1] = pausePlay.getY() + pausePlay.getHeight() - BDS;
		p[0][2] = pausePlay.getX() + pausePlay.getWidth() - BDS;
		p[1][2] = pausePlay.getY() + pausePlay.getHeight() / 2;
		g.fillPolygon(p[0], p[1], 3);
	}

	private String getTime(Duration d) {
		if (d == null)
			return null;
		int h = (int) (d.toHours());
		int min = (int) (d.toMinutes() - ((int) d.toHours()) * 60);
		int sec = (int) (d.toSeconds() - ((int) d.toMinutes()) * 60);
		return h + ":" + (min < 10 ? "0" + min : min) + ":" + (sec < 10 ? "0" + sec : sec);
	}

	private synchronized void updateComponents() {
//			System.out.println("[GUI] :: updateComponents()");

		playlistFileDivider = w / 5 * 2;

		folderButton.setBounds(0, 0, MW, MW);
		playlistButton.setBounds(0, MW, MW, MW);
		pausePlay.setBounds((w - BS) / 2, h - BS - BS / 8, BS, BS);
		last.setBounds(pausePlay.getX() - BS - BS / 8, h - BS - BS / 8, BS, BS);
		next.setBounds(pausePlay.getX() + BS + BS / 8, h - BS - BS / 8, BS, BS);
		rand.setBounds(BS / 8, h - BS - BS / 8, BS, BS);
		loop.setBounds(rand.getX() + BS + BS / 8, h - BS - BS / 8, BS, BS);
		volume.setBounds(next.getX() + BS + BS / 4, next.getY() + BS / 2, w - next.getX() - BS - BS / 4 - BS / 8,
				BS / 2);
		volume.setValue((int) (musicPlayer.getVolume() * (volume.getMaximum() - 10)));

		space = EH / 6;

		if (menuIndex == 0) { // folders
			for (int i = 0; i < folders.size(); i++) {
				folders.get(i).setBounds(MW + space, i * (EH + space) + playlistScrollOffset + space,
						(playlistFileDivider - MW - space * 2), EH);
				folders.get(i).setVisible(folders.get(i).getBounds().intersects(new Rectangle(MW, 0, w - MW, h - BH)));
			}

			if (folders.size() != 0 && folders.get(0).isVisible())
				for (JButton j : folders)
					j.setVisible(true);
			if (customPlaylists.size() != 0 && customPlaylists.get(0).isVisible())
				for (JButton j : customPlaylists)
					j.setVisible(false);
		} else if (menuIndex == 1) { // custom Playlists

			for (int i = 0; i < customPlaylists.size(); i++) {
				customPlaylists.get(i).setBounds(MW + space, i * (EH + space) + playlistScrollOffset + space,
						(playlistFileDivider - MW - space * 2), EH);
				customPlaylists.get(i).setVisible(
						customPlaylists.get(i).getBounds().intersects(new Rectangle(MW, 0, w - MW, h - BH)));
			}

			if (folders.size() != 0 && folders.get(0).isVisible())
				for (JButton j : folders)
					j.setVisible(false);
			if (customPlaylists.size() != 0 && customPlaylists.get(0).isVisible())
				for (JButton j : customPlaylists)
					j.setVisible(true);
		}

		if (files != null) {
			for (int i = 0; i < files.size(); i++) {
				files.get(i).setBounds(playlistFileDivider + space, i * (EH + space) + songScrollOffset + space,
						w - playlistFileDivider - space * 2, EH);
				files.get(i).setVisible(files.get(i).getBounds().intersects(new Rectangle(MW, 0, w - MW, h - BH)));
			}
		} else
			for (int i = 0; i < files.size(); i++) {
				files.get(i).setVisible(false);
			}
		jf.remove(this);
		jf.add(this);
//		jf.requestFocus();
	}

	public synchronized void updateFileButtons() {
		for (JButton j : files)
			jf.remove(j);
		files.clear();
		if (musicPlayer.selectedPlaylist != null) {
			for (int i = 0; i < musicPlayer.selectedPlaylist.getFiles().size(); i++) {
				files.add(createFileButton(musicPlayer.selectedPlaylist.getFiles().get(i)));
			}
		}
	}

	private synchronized JButton createFileButton(String name) {
//		System.out.println("[GUI] :: createFileButton(" + name + ")");
		JButton jb = new JButton();
		jf.add(jb);
		jb.setContentAreaFilled(false);
		jb.setBorderPainted(false);
		jb.setFocusable(false);
		jb.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("[GUI] :: pressedFileButton=" + name);
				musicPlayer.setCurrentPlaylist(musicPlayer.selectedPlaylist);
				for (int i = 0; i < musicPlayer.getCurrentPlaylist().getFiles().size(); i++) {
					if (musicPlayer.getCurrentPlaylist().getFiles().get(i).equals(name)) {
						musicPlayer.getCurrentPlaylist().pause_play(i);
					}
				}
				updateGraphics();
			}
		});
		return jb;
	}

	// initiates New Buttons if needed and updates all others
	public synchronized void updatePlaylistButtons() {
		for (JButton j : folders)
			jf.remove(j);
		for (JButton j : customPlaylists)
			jf.remove(j);
		folders.clear();
		customPlaylists.clear();

		for (int i = 0; i < musicPlayer.folders.size(); i++)
			folders.add(createPlaylistButton(PFolder.getName(musicPlayer.folders.get(i).getFolderPath())));
		for (int i = 0; i < musicPlayer.customPlaylists.size(); i++)
			customPlaylists.add(createPlaylistButton(musicPlayer.customPlaylists.get(i).name));
	}

	private synchronized JButton createPlaylistButton(String name) {
//		System.out.println("[GUI] :: createPlaylistButton(" + name + ")");
		JButton jb = new JButton();
		jf.add(jb);
		jb.setContentAreaFilled(false);
		jb.setBorderPainted(false);
		jb.setFocusable(false);
		jb.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("[GUI] :: pressedPlaylistButton=" + name);
				if (menuIndex == 0) {
					musicPlayer.selectedPlaylist = musicPlayer.getFolderPlaylist(name);
				} else if (menuIndex == 1) {
					musicPlayer.selectedPlaylist = musicPlayer.getCustomPlaylist(name);
				}
				songScrollOffset = 0;
				updateFileButtons();
				updateWindow();
			}
		});
		return jb;
	}

	private void initComponents() {

		folderButton = new JButton();
		jf.add(folderButton);
		folderButton.setContentAreaFilled(false);
		folderButton.setBorderPainted(false);
		folderButton.setFocusable(false);
		folderButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (menuIndex != 0) {
					menuIndex = 0;
					playlistScrollOffset = 0;
					songScrollOffset = 0;
					updateFileButtons();
					updateWindow();
				}
			}
		});

		playlistButton = new JButton();
		jf.add(playlistButton);
		playlistButton.setContentAreaFilled(false);
		playlistButton.setBorderPainted(false);
		playlistButton.setFocusable(false);
		playlistButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (menuIndex != 1) {
					menuIndex = 1;
					playlistScrollOffset = 0;
					songScrollOffset = 0;
					updateFileButtons();
					updateWindow();
				}
			}
		});

		durationShifter = new JButton();
		jf.add(durationShifter);
		durationShifter.setContentAreaFilled(false);
		durationShifter.setBorderPainted(false);
		durationShifter.setFocusable(false);
		durationShifter.addMouseMotionListener(new MouseMotionListener() {
			@Override
			public void mouseMoved(MouseEvent e) {
			}

			@Override
			public void mouseDragged(MouseEvent e) {
				if (musicPlayer.getCurrentPlaylist() != null) {
					hasDragged = true;
					lastDragged = System.currentTimeMillis();
					dragValue = e.getX();
					System.out.println(dragValue);
				}
			}
		});

		pausePlay = new JButton();
		jf.add(pausePlay);
		pausePlay.setContentAreaFilled(false);
		pausePlay.setBorderPainted(false);
		pausePlay.setFocusable(false);
		pausePlay.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				pausePlay();
			}
		});

		last = new JButton();
		jf.add(last);
		last.setContentAreaFilled(false);
		last.setBorderPainted(false);
		last.setFocusable(false);
		last.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (musicPlayer.getCurrentPlaylist() != null)
					musicPlayer.getCurrentPlaylist().playLast();
				updateGraphics();
			}
		});

		next = new JButton();
		jf.add(next);
		next.setContentAreaFilled(false);
		next.setBorderPainted(false);
		next.setFocusable(false);
		next.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (musicPlayer.getCurrentPlaylist() != null)
					musicPlayer.getCurrentPlaylist().playNext();
				updateGraphics();
			}
		});

		rand = new JButton();
		jf.add(rand);
		rand.setContentAreaFilled(false);
		rand.setBorderPainted(false);
		rand.setFocusable(false);
		rand.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (musicPlayer.getCurrentPlaylist() != null)
					musicPlayer.getCurrentPlaylist().setRandom(!musicPlayer.getCurrentPlaylist().isRandom());
				updateGraphics();
			}
		});

		loop = new JButton();
		jf.add(loop);
		loop.setContentAreaFilled(false);
		loop.setBorderPainted(false);
		loop.setFocusable(false);
		loop.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (musicPlayer.getCurrentPlaylist() != null)
					musicPlayer.getCurrentPlaylist().setLoop(!musicPlayer.getCurrentPlaylist().isLoop());
				updateGraphics();
			}
		});

		volume = new JScrollBar(JScrollBar.HORIZONTAL);
		volume.setFocusable(false);
		jf.add(volume);
		setVolumeSteps(400);
	}

	/**
	 * is ONLY called by the pausePlay-Button-Event
	 */
	private synchronized void pausePlay() {
		if (musicPlayer.getCurrentPlaylist() == null) {
			if (musicPlayer.selectedPlaylist != null) {
				musicPlayer.setCurrentPlaylist(musicPlayer.selectedPlaylist);
				musicPlayer.getCurrentPlaylist().pause_play();
			}
		} else if (musicPlayer.selectedPlaylist != null
				&& !musicPlayer.getCurrentPlaylist().equals(musicPlayer.selectedPlaylist)) {
			if (musicPlayer.getCurrentPlaylist().isPlaying()) {
				musicPlayer.getCurrentPlaylist().pause_play();
			}
			musicPlayer.setCurrentPlaylist(musicPlayer.selectedPlaylist);
			musicPlayer.getCurrentPlaylist().pause_play();
		} else {
			musicPlayer.getCurrentPlaylist().pause_play();
		}
		updateGraphics();
	}

	private int lastVolume = 100;
	private long count = 0; // cooldown because of volume.updateUI() --> causes problems

	private int volumeW = 0, volumeH;
	private String lastSong = "";

	public void initThread() {
		new Timer().scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				if (w != getWidth() || h != getHeight()) {
					count = System.currentTimeMillis();
					updateWindow();
				} else if (lastVolume != volume.getValue()) {
					lastVolume = volume.getValue();
					musicPlayer.setVolume((float) lastVolume / (volume.getMaximum() - 10));
					updateGraphics();
				}
				if (count + 100 < System.currentTimeMillis()
						&& (volumeW != volume.getWidth() || volumeH != volume.getHeight())) {
					volumeW = volume.getWidth();
					volumeH = volume.getHeight();
					count = System.currentTimeMillis();

//					volume.updateUI();
				}

				// update duration
				if (lastDragged + 100 < System.currentTimeMillis() && hasDragged) {
					hasDragged = false;
					System.out.println("set duration " + dragValue);
					seekInCurrentSong();
					dragValue = 0;
				}

				// update durationShifter
				if (jf.isFocused()) {
					try {
						if (musicPlayer.getCurrentPlaylist() != null) {
							int offset = (int) ((w - rand.getX() * 2 - OS)
									* musicPlayer.getCurrentPlaylist().getDurationFactor());
							durationShifter.setBounds(rand.getX() + offset, h - BH + BH / 4 - OS / 2, OS, OS);
						} else {
							durationShifter.setBounds(rand.getX(), h - BH + BH / 4 - OS / 2, OS, OS);
						}

						if (hasToUpdateWindow) {
							hasToUpdateWindow = false;
							updateWindow();
						}
					} catch (NullPointerException e) {
					}
					updateGraphics();
				}
			}
		}, 0, 15);
		// Title & durationshifter
		new Timer().scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				// Title
				updateTitle();
			}
		}, 1000, 900);
	}

	private void updateGraphics() {
//		System.out.println("[GUI] :: updateGraphics()");
		repaint();
	}

	public void updateWindow() {
		w = getWidth();
		h = getHeight();
		updateComponents();
		updateGraphics();
	}

	public void updateTitle() {
		if (musicPlayer.getCurrentPlaylist() != null) {
			if (!lastSong.equals(musicPlayer.getCurrentPlaylist().getCurrentFileName())
					&& musicPlayer.getCurrentPlaylist().getCurrentFileName() != null) {
				jf.setTitle(TITLE + " >> " + musicPlayer.getCurrentPlaylist().getCurrentFileName());
			}
		} else if (!jf.getTitle().equals(TITLE)) {
			jf.setTitle(TITLE);
		}
	}

	public void setVolumeSteps(int steps) {
		volume.setMaximum(steps + 10); // steps/2 = Original Volume
		volume.setValue((volume.getMaximum() - 10) / 2);
	}

	public void seekInCurrentSong() {
		double seekBarWidth = w - rand.getX() * 2 - OS;
		double factor = (dragValue + durationShifter.getX() - OS * 0.75) / seekBarWidth;
		musicPlayer.getCurrentPlaylist().seek(factor);
	}
}
