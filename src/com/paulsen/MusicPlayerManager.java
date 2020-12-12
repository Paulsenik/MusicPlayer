package com.paulsen;

import java.awt.Rectangle;
import java.io.File;
import java.util.ArrayList;

import com.paulsen.arduino.ArduinoAction;
import com.paulsen.arduino.ArduinoManager;
import com.paulsen.arduino.PlayerAction.TYPE;
import com.paulsen.playlist.Playlist;
import com.paulsen.storage.DataStorage;
import com.paulsen.storage.PFile;
import com.paulsen.storage.PFolder;
import com.paulsen.ui.frames.MainFrame;

/**
 * Musicplayer which can also connect and recieve instructions from an Arduino
 * and other small Microprocessors connected to the computer
 * 
 * @author Paulsen
 * @version 0.9.8
 * @since 2020-07
 * @see Last Updated 2020/10/07
 */

public class MusicPlayerManager {

	public ArrayList<Playlist> folders = new ArrayList<Playlist>();
	public ArrayList<Playlist> customPlaylists = new ArrayList<Playlist>();

	private Playlist currentPlaylist;
	private DataStorage folderLocations, options;

	public Playlist selectedPlaylist;

	public boolean autoConnect = false, autoPlay_ArduinoPlaylist = false;
	public int initConnectIndex = 0;
	public Rectangle presetMainFramePosition = null, presetArduinoFramePosition = null,
			presetGeneralFramePosition = null;

	private float mainVolume = 0.5f;

	public static void main(String[] args) {
		new MusicPlayerManager();
	}

	public MainFrame ui;

	public MusicPlayerManager() {
		initFoldersAndFiles();
		ui = new MainFrame(this);
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			@Override
			public void run() {
				saveFiles();
			}
		}));

		loadPlayerActions();
		ui.arduinoFrame.updateActionButtons();
		ui.arduinoFrame.updateWindow();
	}

	private void initFoldersAndFiles() {
		System.out.println("[MusicPlayer] :: initFolderAndFiles()"); // Folders
		PFolder.createFolder("MusicPlayer");
		PFolder.createFolder("MusicPlayer/playlists");
		PFolder.createFolder("MusicPlayer/arduino");

		// folder-Playlists
		if (new File("MusicPlayer/folderlocations.storage").exists()) {
			folderLocations = new DataStorage("MusicPlayer/folderlocations.storage");

			String paths[] = folderLocations.read();

			for (String s : paths) {
				addFolder(s);
			}
		} else
			folderLocations = new DataStorage("MusicPlayer/folderlocations.storage");

		// custom-Playlists
		if (PFolder.isFolder("MusicPlayer/playlists")) {
			String playlists[] = PFolder.getFiles("MusicPlayer/playlists", ".storage");
			if (playlists != null)
				for (String s : playlists) {
					System.out.println("[MusicPlayer] :: loading playlist " + PFile.getName(s));
					DataStorage ds = new DataStorage(s);
					String files[] = ds.read();

					Playlist p = new Playlist();
					p.setName(PFile.getName(s));
					for (String path : files) {
						p.addFile(path);
//						System.out.println(path);
					}
					addPlaylist(p);
				}
		}

		// Options
		options = new DataStorage("MusicPlayer/options.storage");
		
		String data[] = options.read();
		// TODO --> change when adding options
		// index: 0 = arduinoPort
		// index: 1 = autoconnect
		// index: 2-5 = presetMainFramePosition
		// index: 6-9 = presetMainFramePosition
		// index: 10-13 = presetMainFramePosition
		// index: 14 = autoPlay_Arduino

		// connect-index
		try {
			initConnectIndex = Integer.parseInt(data[0]);
		} catch (NumberFormatException e) {
			System.err.println("[MusicPlayerManager] :: error with converting arduinoPort!");
		}
		// is autoconnect
		try {
			autoConnect = Boolean.parseBoolean(data[1]);
		} catch (Exception e) {
			System.err.println("[MusicPlayerManager] :: error with converting autoConnect!");
		}

		// MainFrame-Position (x,y,widht, height)
		try {
			int x = Integer.parseInt(data[2]);
			int y = Integer.parseInt(data[3]);
			int w = Integer.parseInt(data[4]);
			int h = Integer.parseInt(data[5]);
			presetMainFramePosition = new Rectangle(x, y, w, h);
		} catch (NumberFormatException e) {
			System.err.println("[MusicPlayerManager] :: error with converting MainFrame-Pos!");
			presetMainFramePosition = null;
		}

		// ArduinoFrame-Position (x,y,widht, height)
		try {
			int x = Integer.parseInt(data[6]);
			int y = Integer.parseInt(data[7]);
			int w = Integer.parseInt(data[8]);
			int h = Integer.parseInt(data[9]);
			presetArduinoFramePosition = new Rectangle(x, y, w, h);
		} catch (NumberFormatException e) {
			System.err.println("[MusicPlayerManager] :: error with converting ArduinoFrame-Pos!");
			presetArduinoFramePosition = null;
		}

		// GeneralFrame-Position (x,y,widht, height)
		try {
			int x = Integer.parseInt(data[10]);
			int y = Integer.parseInt(data[11]);
			int w = Integer.parseInt(data[12]);
			int h = Integer.parseInt(data[13]);
			presetGeneralFramePosition = new Rectangle(x, y, w, h);
		} catch (NumberFormatException e) {
			System.err.println("[MusicPlayerManager] :: error with converting GeneralFrame-Pos!");
			presetGeneralFramePosition = null;
		}

		// autoPlay_Arduino
		try {
			autoPlay_ArduinoPlaylist = Boolean.parseBoolean(data[14]);
		} catch (Exception e) {
			System.err.println("[MusicPlayerManager] :: error with converting autoPlay_Arduino!");
		}
	}

	// arduino
	public void loadPlayerActions() {
		ArrayList<ArduinoAction> actions = new ArrayList<ArduinoAction>();

		String s[] = PFolder.getFiles("MusicPlayer/arduino", ".storage");
		if (s != null) {
			for (int i = 0; i < s.length; i++) {
				DataStorage ds = new DataStorage(s[i]);
				String data[] = ds.read();
				if (data != null && data.length != 0) {
					ArduinoAction a = new ArduinoAction();
					a.setName(data[0]);
					try {
						a.setType(TYPE.values()[Integer.parseInt(data[1])]);
					} catch (NumberFormatException e) {
						System.err.println("[MusicPlayerManager] :: could not set type for " + ds.getPath());
					}
					a.setKey(data[2]);
					if (a.getAction() != null) {
						if (data.length - 3 > 0) { // is args available
							String args[] = new String[data.length - 3];
							for (int z = 0; z < args.length; z++) {
								args[z] = data[z + 3];
//								System.out.println("args" + z + "   " + args[z]);
							}
							a.setArgs(args);
						}
					}
					actions.add(a);
				}
			}

			ui.arduinoFrame.arduinoManager.setActions(actions);
		}
	}

	private void saveFiles() {
		System.out.println("[MusicPlayer] :: saving...");

		// Folders
		String s[] = new String[folders.size()];
		for (int i = 0; i < s.length; i++) {
			s[i] = folders.get(i).getFolderPath();
		}
		folderLocations.write(s);

		// custom-Playlists
		for (int i = 0; i < customPlaylists.size(); i++) {
			String files[] = new String[customPlaylists.get(i).getFiles().size()];
			for (int j = 0; j < files.length; j++) {
				files[j] = customPlaylists.get(i).getFiles().get(j);
			}
			DataStorage ds = new DataStorage("MusicPlayer/playlists/" + customPlaylists.get(i).name + ".storage");
			ds.write(files);
		}

		// arduino
		ArduinoManager am = ui.arduinoFrame.arduinoManager;
		for (int i = 0; i < am.getActions().size(); i++) {
			ArduinoAction a = am.getActions().get(i);
			String name = a.getName();
			String actionIndex = am.getTYPEindex(a) + ""; // index
			String key = a.getKey();

			String args[];
			if (a.getAction() != null) {
				args = a.getArgs();

				String data[] = new String[(args != null ? args.length : 0) + 3];
				data[0] = name;
				data[1] = actionIndex;
				data[2] = key;
				if (args != null && args.length != 0)
					for (int j = 0; j < args.length; j++)
						data[3 + j] = args[j];

				DataStorage ds = new DataStorage("MusicPlayer/arduino/action_" + i + ".storage");
				ds.write(data);
			} else {
				System.err.println("[MusicPlayer] :: could not save action with index=" + i);
			}
		}

		// Options
		String data[] = new String[15]; // TODO change when settings are added
		data[0] = Integer.toString(initConnectIndex);
		data[1] = Boolean.toString(autoConnect);
		data[2] = Integer.toString(ui.jf.getX());
		data[3] = Integer.toString(ui.jf.getY());
		data[4] = Integer.toString(ui.jf.getWidth());
		data[5] = Integer.toString(ui.jf.getHeight());
		data[6] = Integer.toString(ui.arduinoFrame.jf.getX());
		data[7] = Integer.toString(ui.arduinoFrame.jf.getY());
		data[8] = Integer.toString(ui.arduinoFrame.jf.getWidth());
		data[9] = Integer.toString(ui.arduinoFrame.jf.getHeight());
		data[10] = Integer.toString(ui.generalFrame.jf.getX());
		data[11] = Integer.toString(ui.generalFrame.jf.getY());
		data[12] = Integer.toString(ui.generalFrame.jf.getWidth());
		data[13] = Integer.toString(ui.generalFrame.jf.getHeight());
		data[14] = Boolean.toString(autoPlay_ArduinoPlaylist);
		options.write(data);

		System.out.println("[MusicPlayer] :: ...saving complete");
	}

	public Playlist getFolderPlaylist(String foldername) {
		for (int i = 0; i < folders.size(); i++) {
			if (foldername.equals(PFolder.getName(folders.get(i).getFolderPath())))
				return folders.get(i);
		}
		return null;
	}

	public Playlist getCustomPlaylist(String name) {
		for (int i = 0; i < customPlaylists.size(); i++) {
			if (name.equals(customPlaylists.get(i).name))
				return customPlaylists.get(i);
		}
		return null;
	}

	public synchronized void setCurrentPlaylist(Playlist p) {
		if (currentPlaylist != null && currentPlaylist.isPlaying()) {
			currentPlaylist.pause_play();
			currentPlaylist = p;
			currentPlaylist.setVolume(mainVolume);
		} else if (p != null) {
			currentPlaylist = p;
			currentPlaylist.setVolume(mainVolume);
		}
	}

	public int getIndexOfCurrentFile() {
		if (currentPlaylist != null)
			for (int i = 0; i < currentPlaylist.getFiles().size(); i++)
				if (currentPlaylist.getCurrentFileName() != null
						&& currentPlaylist.getCurrentFileName().equals(currentPlaylist.getFileName(i)))
					return i;
		return -1;
	}

	public void addFolder(String path) {
		if (path != null && PFolder.isFolder(path)) {
			Playlist p = new Playlist(path);
			p.setName(PFolder.getName(path));
			folders.add(p);
			System.out.println("[MusicPlayer] :: createNewFolder=" + path);
			if (ui != null) {
				ui.updatePlaylistButtons();
				ui.updateFileButtons();
				ui.updateWindow();
			}
		}
	}

	public void addPlaylist(Playlist p) {
		if (p != null) {
			customPlaylists.add(p);
			System.out.println("[MusicPlayer] :: added Playlist " + p);
			if (ui != null) {
				ui.updatePlaylistButtons();
				ui.updateFileButtons();
				ui.updateWindow();
			}
		}
	}

	public void addPlaylist(String name) {
		if (name != null) {
			Playlist p = new Playlist();
			p.setName(name);
			customPlaylists.add(p);
			System.out.println("[MusicPlayer] :: createNewPlaylist=" + name);
			if (ui != null) {
				ui.updatePlaylistButtons();
				ui.updateFileButtons();
				ui.updateWindow();
			}
		}
	}

	public void deleteArduinoAction(int index) {
		PFile file = new PFile("MusicPlayer/arduino/action_" + index + ".storage");
		file.delete();
	}

	public void deleteFolder(int index) {
		System.out.println("[MusicPlayer] :: deleteFolder=" + folders.get(index));
		folders.remove(index);
		ui.updatePlaylistButtons();
		ui.updateFileButtons();
		ui.updateWindow();
	}

	public void deletePlaylist(int index) {
		System.out.println("[MusicPlayer] :: deletePlaylist=" + customPlaylists.get(index));
		new PFile("MusicPlayer/playlists/" + customPlaylists.get(index).name + ".storage").delete();
		customPlaylists.remove(index);
		ui.updatePlaylistButtons();
		ui.updateFileButtons();
		ui.updateWindow();
	}

	public Playlist getCurrentPlaylist() {
		return currentPlaylist;
	}

	public synchronized void setVolume(float f) {
		System.out.print("[MusicPlayerManager] :: setVolume from " + mainVolume);
		mainVolume = (f > 1 ? 1 : (f < 0 ? 0 : f));
		if (currentPlaylist != null)
			currentPlaylist.setVolume(mainVolume);
		System.out.println(" to " + mainVolume);
	}

	public float getVolume() {
		return mainVolume;
	}

}
