package com.paulsen.ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.File;

import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.paulsen.MusicPlayerManager;
import com.paulsen.playlist.Player;
import com.paulsen.storage.PFolder;
import com.paulsen.ui.frames.MainFrame;

public class FrameManager implements MouseWheelListener, KeyListener {

	private static final int scrollmultiplier = 2;

	MainFrame ui;
	private static JFileChooser targetChooser, folderChooser;

	private boolean isControlPressed = false;

	public FrameManager(MainFrame ui) {
		folderChooser = new JFileChooser();
		targetChooser = new JFileChooser();
		this.ui = ui;
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		if (e.getX() < ui.playlistFileDivider && e.getX() > ui.MW) { // Playlists
			ui.playlistScrollOffset -= e.getWheelRotation() * e.getScrollAmount() * scrollmultiplier;
		} else { // Songs
			if (isControlPressed) {
				ui.songScrollOffset -= e.getWheelRotation() * (ui.h - ui.BH) / 2;
			} else {
				ui.songScrollOffset -= e.getWheelRotation() * e.getScrollAmount() * scrollmultiplier;
			}
		}
		ui.updateWindow();
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
			isControlPressed = true;
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
			isControlPressed = false;
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {
	}

	public void initMenu() {
		String font = ui.font;
		MusicPlayerManager musicPlayer = ui.musicPlayer;

		ui.mb = new JMenuBar();
		ui.mb.setBackground(Color.white);

		// Options
		JMenu options = new JMenu("Options");
		options.setFont(new Font(font, 0, 20));
		ui.mb.add(options);

		// General
		JMenuItem general = new JMenuItem("General");
		general.setFont(new Font(font, 0, 20));
		general.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("[FrameManager] :: open generalOptions");
				ui.generalFrame.setVisible(true);
				ui.generalFrame.requestFocus();
			}
		});
		options.add(general);

		// Arduino
		JMenuItem arduino = new JMenuItem("Arduino");
		arduino.setFont(new Font(font, 0, 20));
		arduino.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("[FrameManager] :: open arduinoOptions");
				ui.arduinoFrame.setVisible(true);
				ui.arduinoFrame.requestFocus();
			}
		});
		options.add(arduino);

		// equalizer
		JMenuItem eq = new JMenuItem("Equalizer");
		eq.setFont(new Font(font, 0, 20));
		eq.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("[FrameManager] :: open EQ-Window [TODO]");
				// TODO for 1.1.0
				JOptionPane.showMessageDialog(ui.jf, "Equalizer is not integrated yet. Comming soon in V1.1.0",
						"Equalizer", JOptionPane.INFORMATION_MESSAGE);
			}
		});
		options.add(eq);

		// Edit
		JMenu edit = new JMenu("Edit");
		edit.setFont(new Font(font, 0, 20));
		ui.mb.add(edit);

		JMenu add = new JMenu("Add");
		add.setFont(new Font(font, 0, 20));
		edit.add(add);

		JMenu rem = new JMenu("Remove");
		rem.setFont(new Font(font, 0, 20));
		edit.add(rem);

		JMenuItem addFolder = new JMenuItem("-Folder");
		addFolder.setFont(new Font(font, 0, 20));
		addFolder.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("[FrameManager] :: addfolder");
				musicPlayer.addFolder(getFolder(ui.jf));
			}
		});
		add.add(addFolder);

		JMenuItem addPlaylist = new JMenuItem("-Playlist");
		addPlaylist.setFont(new Font(font, 0, 20));
		addPlaylist.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("[FrameManager] :: addPlaylist");
				musicPlayer.addPlaylist(createInputDialog(ui.jf, "Creating a new Playlist", "Input name of Playlist"));
			}
		});
		add.add(addPlaylist);

		JMenuItem addFile = new JMenuItem("-Song");
		addFile.setFont(new Font(font, 0, 20));
		addFile.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("[FrameManager] :: addFile");
				String a[] = new String[musicPlayer.customPlaylists.size()];
				for (int i = 0; i < musicPlayer.customPlaylists.size(); i++)
					a[i] = PFolder.getName(musicPlayer.customPlaylists.get(i).name);
				int index = createButtonBoxOptionPane(ui.jf, "Add Song","Select Playlist", a);
				if (index != -1) {
					musicPlayer.customPlaylists.get(index).addFile(getFilePath(ui.jf));
				}
			}
		});
		add.add(addFile);

		JMenuItem remFolder = new JMenuItem("-Folder");
		remFolder.setFont(new Font(font, 0, 20));
		remFolder.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("[FrameManager] :: remfolder");
				String a[] = new String[musicPlayer.folders.size()];
				for (int i = 0; i < musicPlayer.folders.size(); i++)
					a[i] = PFolder.getName(musicPlayer.folders.get(i).getFolderPath());

				int index = createButtonBoxOptionPane(ui.jf, "Delete Folder", "Select the Folder you want to delete", a);
				if (index != -1 && 0 == createConfirmDialog(ui.jf, "Delete Folder",
						"Do you really want to delete " + a[index].toUpperCase() + " ?"))
					musicPlayer.deleteFolder(index);
			}
		});
		rem.add(remFolder);

		JMenuItem remPlaylist = new JMenuItem("-Playlist");
		remPlaylist.setFont(new Font(font, 0, 20));
		remPlaylist.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("[FrameManager] :: remPlaylist");
				String a[] = new String[musicPlayer.customPlaylists.size()];
				for (int i = 0; i < musicPlayer.customPlaylists.size(); i++)
					a[i] = musicPlayer.customPlaylists.get(i).name;

				int index = createButtonBoxOptionPane(ui.jf,  "Delete Folder", "Select the Playlist you want to delete", a);
				if (index != -1 && 0 == createConfirmDialog(ui.jf, "Delete Playlist",
						"Do you really want to delete " + a[index].toUpperCase() + " ?"))
					musicPlayer.deletePlaylist(index);
			}
		});
		rem.add(remPlaylist);

		JMenuItem remSong = new JMenuItem("-Song");
		remSong.setFont(new Font(font, 0, 20));
		remSong.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("[FrameManager] :: remSong");
				String a[] = new String[musicPlayer.customPlaylists.size()];
				for (int i = 0; i < musicPlayer.customPlaylists.size(); i++)
					a[i] = musicPlayer.customPlaylists.get(i).name;

				int index = createButtonBoxOptionPane(ui.jf, "Select a Playlist ", "Select the Playlist of the song that should be deleted", a);
				if (index != -1) {
					String b[] = new String[musicPlayer.customPlaylists.get(index).getFiles().size()];
					for (int i = 0; i < musicPlayer.customPlaylists.get(index).getFiles().size(); i++) {
						b[i] = PFolder.getName(musicPlayer.customPlaylists.get(index).getFiles().get(i));
					}
					int song = createComboBoxOptionPane(ui.jf, "Title", b);
					if (song != -1 && 0 == createConfirmDialog(ui.jf,
							"Delete Song in >> " + (musicPlayer.customPlaylists.get(index).getFolderPath() == null
									? musicPlayer.customPlaylists.get(index).name
									: musicPlayer.customPlaylists.get(index).getFolderPath()),
							"Do you really want to delete "
									+ PFolder.getName(musicPlayer.customPlaylists.get(index).getFiles().get(song))
									+ " ?")) {
						musicPlayer.customPlaylists.get(index).removeFile(song);
						musicPlayer.ui.updateFileButtons();
						musicPlayer.ui.updateWindow();
					}
				}
			}
		});
		rem.add(remSong);

		ui.jf.setJMenuBar(ui.mb);
		ui.mb.setVisible(true);
	}

	public static String getFolder(JFrame parent) {
		folderChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		folderChooser.setAcceptAllFileFilterUsed(false);
		folderChooser.setFileFilter(new FileFilter() {
			@Override
			public String getDescription() {
				return "Folders";
			}

			@Override
			public boolean accept(File f) {
				return !f.isFile();
			}
		});
		int rueckgabeWert = folderChooser.showDialog(parent, "Choose a FOLDER");
		if (rueckgabeWert == JFileChooser.APPROVE_OPTION)
			return folderChooser.getSelectedFile().getAbsolutePath();
		return null;
	}

	public static String getFilePath(JFrame parent) {
		targetChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		targetChooser.setAcceptAllFileFilterUsed(false);

		String s = "";
		for (int i = 0; i < Player.ALLOWEDFILETYPES.length; i++)
			s += "." + Player.ALLOWEDFILETYPES[i] + " ";
		targetChooser.setFileFilter(new FileNameExtensionFilter(s, Player.ALLOWEDFILETYPES));

		int rueckgabeWert = targetChooser.showDialog(parent, "Choose a Audio-File");
		if (rueckgabeWert == JFileChooser.APPROVE_OPTION)
			return targetChooser.getSelectedFile().getAbsolutePath();
		return null;
	}

	public static int createButtonBoxOptionPane(JFrame parent, String title, String message, Object comboBoxInput[]) {
		return JOptionPane.showOptionDialog(parent, message, title, JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE, null, comboBoxInput, 0);
	}

	/**
	 * Creates a popup-window which lets u choose one of the Options
	 * 
	 * @param parent
	 * @param title
	 * @param comboBoxInput String-Array of Options
	 * @return index from 0 to comboBoxInput.length
	 */
	public static int createComboBoxOptionPane(JFrame parent, String title, String comboBoxInput[]) {
		JComboBox<String> box = new JComboBox<>(comboBoxInput);
		JOptionPane.showMessageDialog(parent, box, title, JOptionPane.QUESTION_MESSAGE);
		return box.getSelectedIndex();
	}

	// returns 0=yes, 1=no, 2=exit
	public static int createConfirmDialog(JFrame parent, String title, String message) {
		return JOptionPane.showConfirmDialog(parent, message, title, JOptionPane.YES_NO_OPTION);
	}

	public static String createInputDialog(JFrame parent, String title, String message) {
		return JOptionPane.showInputDialog(parent, message, title, JOptionPane.PLAIN_MESSAGE);
	}
}
