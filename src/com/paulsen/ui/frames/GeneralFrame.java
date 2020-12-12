package com.paulsen.ui.frames;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;

import com.paulsen.MusicPlayerManager;
import com.paulsen.ui.options.GeneralOption;
import com.paulsen.ui.options.OptionListener;

public class GeneralFrame extends JLabel {
	private static final long serialVersionUID = 1L;
	public static final String font = "Noto Emoji"; // Agency FB
	public final int EH = 60; // Element-height
	public final int ES = 6; // Element-Space
	public final int space = 15; // Space to borders
	public final double scrollmultiplier = 2;

	private MusicPlayerManager musicPlayer;
	public JFrame jf;
	private int w, h;

	private int scrollOffset = 0;

	// Options
	ArrayList<GeneralOption> options = new ArrayList<>();

	private boolean isUpdating = false;

	public GeneralFrame(MusicPlayerManager musicPlayer) {
		this.musicPlayer = musicPlayer;

		jf = new JFrame("General-Options");
		jf.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		jf.setMinimumSize(new Dimension(700, 700));

		if (musicPlayer.presetGeneralFramePosition != null) {
			jf.setSize((int) musicPlayer.presetGeneralFramePosition.getWidth(), (int) musicPlayer.presetGeneralFramePosition.getHeight());
			jf.setLocation((int) musicPlayer.presetGeneralFramePosition.x, (int) musicPlayer.presetGeneralFramePosition.y);
		} else {
			jf.setSize(600, 600);
			jf.setLocationRelativeTo(null);
		}

		initComponents();
		initThread();

		updateComponents();
		updateGraphics();

		jf.addMouseWheelListener(new MouseWheelListener() {

			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				int scroll = (int) (e.getWheelRotation() * e.getScrollAmount() * scrollmultiplier);
				if (scrollOffset - scroll <= 0) {
					if (scroll > 0) {
						if (options.size() > 0 && options.get(options.size() - 1).getJB().getY() + ES + EH >= h)
							scrollOffset -= scroll;
					} else {
						scrollOffset -= scroll;
					}
				} else {
					scrollOffset = 0;
				}
				updateWindow();
			}
		});

		jf.add(this);
	}

	private void initComponents() {
		initOptions();
	}

	/**
	 * Add GeneralOptions here to the options-list
	 */
	private void initOptions() {
		String[] choice_ON_OFF = { "ON", "OFF" };
		options.add(new GeneralOption(jf, "Auto Connect", choice_ON_OFF, new OptionListener() {
			@Override
			public String optionDispayed(int index) {
				if (musicPlayer.autoConnect) {
					return "ON";
				} else {
					return "OFF";
				}
			}
			@Override
			public void optionChanged(int index) {
				musicPlayer.autoConnect = index == 0;
				updateGraphics();
			}

		}));
		//TODO
		options.add(new GeneralOption(jf, "AutoPlay Arduino-Playlist", choice_ON_OFF, new OptionListener() {
			@Override
			public String optionDispayed(int index) {
				if (musicPlayer.autoPlay_ArduinoPlaylist) {
					return "ON";
				} else {
					return "OFF";
				}
			}
			@Override
			public void optionChanged(int index) {
				musicPlayer.autoPlay_ArduinoPlaylist = index == 0;
				updateGraphics();
			}
		}));
	}

	protected void paintComponent(Graphics g) { // GRAPHICS
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		w = getWidth();
		h = getHeight();
		Color sel = new Color(201, 201, 201); // selected-button
//		Color notSel = new Color(81, 81, 81);

		// Background
		g.setColor(new Color(51, 51, 51));
		g.fillRect(0, 0, w, h);

		// options
		for (int i = 0; i < options.size(); i++) {
			JButton jb = options.get(i).getJB();
			g.setColor(sel);
			g.fillRoundRect(jb.getX(), jb.getY(), jb.getWidth(), jb.getHeight(), space, space);
			g.setColor(Color.white);
			g.setFont(new Font(font, 0, jb.getHeight()));
			g.drawString(options.get(i).getTitle() + " : " + options.get(i).getSelected(), jb.getX() + ES,
					(int) (jb.getY() + jb.getHeight() * 0.9));
		}
	}

	private void updateComponents() {
		isUpdating = true;

		w = getWidth();
		h = getHeight();
		// START

		// Options
		for (int i = 0; i < options.size(); i++) {
			JButton jb = options.get(i).getJB();
			jb.setBounds(space, scrollOffset + ES + (ES + EH) * i, w - space * 2, EH);
		}

		// END
		jf.remove(this);
		jf.add(this);
		jf.requestFocus();

		isUpdating = false;
	}

	public void updateWindow() {
		w = getWidth();
		h = getHeight();
		updateComponents();
		updateGraphics();
	}

	public void setVisible(boolean b) {
		super.setVisible(b);
		jf.setVisible(b);
	}

	public void requestFocus() {
		jf.requestFocus();
	}

	private void initThread() {

		new Timer().scheduleAtFixedRate(new TimerTask() {

			@Override
			public void run() {
				if (w != getWidth() || h != getHeight()) {
					updateWindow();
				}
			}
		}, 0, 15);
	}

	private void updateGraphics() {
		repaint();
	}

}
