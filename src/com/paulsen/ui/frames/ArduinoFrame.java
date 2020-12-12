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
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollBar;

import com.paulsen.MusicPlayerManager;
import com.paulsen.arduino.ArduinoAction;
import com.paulsen.arduino.ArduinoListener;
import com.paulsen.arduino.ArduinoManager;
import com.paulsen.arduino.PlayerAction;
import com.paulsen.arduino.PlayerAction.TYPE;
import com.paulsen.playlist.Playlist;
import com.paulsen.ui.FrameManager;

public class ArduinoFrame extends JLabel {
	private static final long serialVersionUID = 1L;
	public static final String font = "Noto Emoji"; // Agency FB
	public final int BH = 75; // Bar-height
	public final int BS = 60; // Button-size
	public final int EH = 60; // Element-height

	private MusicPlayerManager musicPlayer;
	public JFrame jf;
	private int w, h;
	private int actionsScrollOffset = 0;
	private int scrollmultiplier = 2;
	private int space = 0;
	private double optionEH = 0; // optionsElementHeight
	public boolean isUpdating = false, isComponentsFinish = false; // isComponentsFinish => returns false if components
																	// havent been initialized

	private ArduinoAction currentAction;
	ArrayList<JButton> arduinoActionButtons = new ArrayList<JButton>();

	public ArduinoManager arduinoManager;
	JButton connect, addAction, remAction;

	// Options
	JButton name, key, playlists;
	JComboBox<String> actions, amount;
	JScrollBar minValue, maxValue;

	static final float[] amountList = { 0.5f, 1f, 5f, 10f, 25f, 50f, 100f };

	public ArduinoFrame(MusicPlayerManager musicPlayer) {
		this.musicPlayer = musicPlayer;

		initArduino();

		jf = new JFrame("Arduino-Options");
		jf.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		jf.setMinimumSize(new Dimension(700, 700));

		if (musicPlayer.presetMainFramePosition != null) {
			jf.setSize((int) musicPlayer.presetArduinoFramePosition.getWidth(),
					(int) musicPlayer.presetArduinoFramePosition.getHeight());
			jf.setLocation((int) musicPlayer.presetArduinoFramePosition.x,
					(int) musicPlayer.presetArduinoFramePosition.y);
		} else {
			jf.setSize(600, 600);
			jf.setLocationRelativeTo(null);
		}

		jf.addMouseWheelListener(new MouseWheelListener() {
			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				actionsScrollOffset -= e.getWheelRotation() * e.getScrollAmount() * scrollmultiplier;
				updateWindow();
			}
		});

		initComponents();
		initThread();

		updateComponents();
		updateGraphics();

		jf.add(this);
	}

	protected void paintComponent(Graphics g) { // GRAPHICS
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		w = getWidth();
		h = getHeight();
		Color sel = new Color(201, 201, 201); // selected-button
		Color notSel = new Color(81, 81, 81);

		// Background
		g.setColor(new Color(51, 51, 51));
		g.fillRect(0, 0, w, h);

		// Actions
		g.setFont(new Font(font, 0, EH));
		for (int i = 0; i < arduinoActionButtons.size(); i++) {
			if (arduinoManager.getActionIndex(currentAction) == i) {
				g.setColor(new Color(131, 131, 131));
				g.fillRect(arduinoActionButtons.get(i).getX(), arduinoActionButtons.get(i).getY(),
						arduinoActionButtons.get(i).getWidth() + space * 2, arduinoActionButtons.get(i).getHeight());
			} else {
				g.setColor(notSel);
				g.fillRect(arduinoActionButtons.get(i).getX(), arduinoActionButtons.get(i).getY(),
						arduinoActionButtons.get(i).getWidth(), arduinoActionButtons.get(i).getHeight());
			}
			g.setColor(Color.white);
			g.drawString("" + arduinoManager.getActions().get(i).getName(), arduinoActionButtons.get(i).getX() + space,
					(int) (arduinoActionButtons.get(i).getY() + arduinoActionButtons.get(i).getHeight() * 0.9));
		}

		// options
		g.setColor(new Color(131, 131, 131));
		g.fillRect(w / 2, 0, w / 2, h);

		g.setFont(new Font(font, 0, (int) optionEH));
		if (name.isVisible()) {
			g.setColor(Color.white);
			g.drawString("Name:", (int) (w / 2 + space), (int) (BH + (optionEH + space)));
			g.setColor(notSel);
			g.fillRect(name.getX(), name.getY(), name.getWidth(), name.getHeight());
			g.setColor(Color.white);
			g.drawString(currentAction.getName(), name.getX() + space, (int) (name.getY() + name.getHeight() * 0.9));

			g.drawString("Action:", (int) (w / 2 + space), (int) (BH + (optionEH) * 3 + space * 2));

			g.drawString("Key:", (int) (w / 2 + space), (int) (BH + (optionEH) * 5 + space * 4));
			if (arduinoManager.isLookingForInput) {
				g.setColor(sel);
				g.fillRect(key.getX(), key.getY(), key.getWidth(), key.getHeight());
				g.setColor(Color.white);
				g.drawString("...", key.getX() + space, (int) (key.getY() + key.getHeight() * 0.9));
			} else {
				g.setColor(notSel);
				g.fillRect(key.getX(), key.getY(), key.getWidth(), key.getHeight());
				g.setColor(Color.white);
				if (currentAction.getKey() != null)
					g.drawString(currentAction.getKey(), key.getX() + space,
							(int) (key.getY() + key.getHeight() * 0.9));
			}
		}
		if (minValue.isVisible()) {
			String min = "", max = "";
			if (currentAction.getArgs() != null) {
				String s[] = currentAction.getArgs();
				min = s[0];
				max = s[1];
			}
			g.drawString("MIN: " + min, (int) (w / 2 + space), (int) (BH + (optionEH) * 7 + space * 6));
			g.drawString("MAX: " + max, (int) (w / 2 + space), (int) (BH + (optionEH) * 9 + space * 8));
		}
		if (playlists.isVisible()) {
			g.drawString("List:", (int) (w / 2 + space), (int) (BH + (optionEH) * 7 + space * 6));
			g.setColor(notSel);
			g.fillRect(playlists.getX(), playlists.getY(), playlists.getWidth(), playlists.getHeight());

			g.setColor(Color.white);
			if (currentAction.getKey() != null && currentAction.getType() == TYPE.PLAYLIST
					&& currentAction.getArgs() != null) {
				g.drawString(currentAction.getArgs()[1], playlists.getX() + space,
						(int) (playlists.getY() + playlists.getHeight() * 0.9));
				System.out.println(currentAction.getArgs()[1] + " " + currentAction.getAction());
			}
		}
		if (amount.isVisible()) {
			g.drawString("Amount:", (int) (w / 2 + space), (int) (BH + (optionEH) * 7 + space * 6));
		}

		// Bar
		g.setColor(new Color(61, 61, 61));
		g.fillRect(0, 0, w, BH);

		g.setColor(sel);
		g.fillRect(addAction.getX(), addAction.getY(), addAction.getWidth(), addAction.getHeight());
		g.fillRect(remAction.getX(), remAction.getY(), remAction.getWidth(), remAction.getHeight());
		g.setColor(arduinoManager.isConnected() ? sel : notSel);
		g.fillRect(connect.getX(), connect.getY(), connect.getWidth(), connect.getHeight());
		g.setFont(new Font(font, 0, connect.getHeight()));
		g.setColor(Color.white);
		g.drawString((arduinoManager.isConnected() ? (arduinoManager.getCurrentPortName()) : "-"),
				(int) (connect.getX() + space), (int) (connect.getY() + connect.getHeight() * 0.95));

	}

	public void updateComponents() {
		if (isComponentsFinish) {
			isUpdating = true;

			w = getWidth();
			h = getHeight();

			// Options
			updateOptions();

			space = (BH - BS) / 2;

			connect.setBounds(w / 4 * 3 + space, space, w / 4 - space * 2, BS);
			addAction.setBounds(space, space, BS, BS);
			remAction.setBounds(addAction.getX() + addAction.getWidth() + space, space, BS, BS);

			for (int i = 0; i < arduinoActionButtons.size(); i++) {
				arduinoActionButtons.get(i).setBounds(space, actionsScrollOffset + BH + space + (EH + space) * i,
						w / 2 - space * 2, EH);
				arduinoActionButtons.get(i).setVisible(
						arduinoActionButtons.get(i).getBounds().intersects(new Rectangle(0, BH, w / 2, h - BH)));
			}

			jf.remove(this);
			jf.add(this);
			jf.requestFocus();

			isUpdating = false;
		} else {
			System.out.println("[ArduinoFrame] :: Components are not yet initialized");
		}
	}

	public void updateWindow() {
		w = getWidth();
		h = getHeight();
		updateComponents();
		updateGraphics();
	}

	public void updateActionButtons() {
		for (JButton j : arduinoActionButtons)
			jf.remove(j);
		arduinoActionButtons.clear();

		for (int i = 0; i < arduinoManager.getActions().size(); i++) {
			JButton j = new JButton();
			j.setContentAreaFilled(false);
			j.setBorderPainted(false);
			j.setFocusable(false);
			j.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					for (int i = 0; i < arduinoActionButtons.size(); i++)
						if (e.getSource().equals(arduinoActionButtons.get(i))) {
							if (arduinoManager.getActionIndex(currentAction) == i) {
								currentAction = null;
							} else {
								System.out.println("Action selected : " + arduinoManager.getActions().get(i).getName()
										+ ", index: " + i);
								currentAction = arduinoManager.getActions().get(i);

								// load actionsettings to JFrame-components
								int index = arduinoManager.getTYPEindex(currentAction);
								if (index != -1) {
									actions.setSelectedIndex(index);
								} else {
									actions.setSelectedIndex(0);
								}

								try {
									if (currentAction.getAction() != null) {
										String args[] = currentAction.getArgs();
										if (args != null)
											switch (currentAction.getType()) {
											case MASTERVOLUME:
												if (args.length == 2) {
													try {
														minValue.setValue(Integer.parseInt(args[0]));
														maxValue.setValue(Integer.parseInt(args[1]));
														System.out.println("[ArduinoFrame] :: set min to " + args[0]
																+ " & max to " + args[1]);
													} catch (NumberFormatException exept) {
														System.err.println(
																"[ArduinoFrame] :: could not set min and max to UI!");
													}
												}
												break;
											case LESSVOLUME:
											case MOREVOLUME:
												if (args.length == 1) {
													try {
														amount.setSelectedIndex(Integer.parseInt(args[0]));
														System.out.println(
																"[ArduinoFrame] :: set amount-selection to " + args[0]);
													} catch (NumberFormatException exept) {
														System.err.println(
																"[ArduinoFrame] :: could not set amount-selection to "
																		+ args[0]);
													}
												}
												break;
											case PLAYLIST:
												break;
											default:
												break;
											}
									} else {
										System.err.println("Some error!");
									}
								} catch (NumberFormatException execpt) {
									System.err.println("[ArduinoFrame] :: error with formating!");
									execpt.printStackTrace();
								}
							}
							updateOptions();
							updateWindow();
						}
				}

			});
			j.setVisible(true);

			jf.add(j);
			arduinoActionButtons.add(j);
		}
	}

	private void initComponents() {
		connect = new JButton();
		connect.setContentAreaFilled(false);
		connect.setBorderPainted(false);
		connect.setFocusable(false);
		jf.add(connect);
		connect.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("[ArduinoFrame] :: pressed connect/disconnect");

				if (!arduinoManager.isConnected()) {
					arduinoManager.updatePorts();

					String list[] = new String[arduinoManager.getPortList().size()];
					for (int i = 0; i < list.length; i++)
						list[i] = arduinoManager.getPortList().get(i);

					int portIndex = FrameManager.createComboBoxOptionPane(jf, "Choose USB-Port", list);
					if (arduinoManager.connect(portIndex)) {
						musicPlayer.initConnectIndex = portIndex;
					}
				} else {
					arduinoManager.disConnect();
				}
			}
		});

		addAction = new JButton("+");
		addAction.setContentAreaFilled(false);
		addAction.setBorderPainted(false);
		addAction.setFocusable(false);
		jf.add(addAction);
		addAction.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("[ArduinoFrame] :: pressed addAction");
				arduinoManager.createAction();
				updateActionButtons();
				updateWindow();
			}
		});

		remAction = new JButton("-");
		remAction.setContentAreaFilled(false);
		remAction.setBorderPainted(false);
		remAction.setFocusable(false);
		jf.add(remAction);
		remAction.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("[ArduinoFrame] :: pressed remAction");
				String s[] = new String[arduinoManager.getActions().size()];
				for (int i = 0; i < s.length; i++) {
					if (arduinoManager.getActions().get(i).getName() != null)
						s[i] = arduinoManager.getActions().get(i).getName();
					else
						s[i] = "NO NAME " + (i + 1);
				}
				int index = FrameManager.createComboBoxOptionPane(jf, "Delete Arduinoaction:", s);
				if (index != -1) {
					int confirm = FrameManager.createConfirmDialog(jf, "Delete Arduinoaction",
							"Do you really want to delete " + arduinoManager.getActions().get(index).getName() + "?");
					if (confirm == 0) {
						musicPlayer.deleteArduinoAction(index);
						arduinoManager.removeAction(index);
						updateActionButtons();
						updateWindow();
					}
				}
			}
		});

		initOptions();
		isComponentsFinish = true;
	}

	private void updateOptions() {
		if (currentAction != null) { // Display options
			optionEH = (h - BH - space * 10) / 10;

			name.setBounds(w / 2 + space, (int) (BH + optionEH + space * 2), w / 2 - space * 2, (int) (optionEH));
			actions.setBounds(w / 2 + space, (int) (BH + optionEH * 3 + space * 4), w / 2 - space * 2,
					(int) (optionEH));
			key.setBounds(w / 2 + space, (int) (BH + optionEH * 5 + space * 5), w / 2 - space * 2, (int) (optionEH));

			// extra
			if (currentAction.getType() == PlayerAction.TYPE.MASTERVOLUME) { // active volumemanipulition
				minValue.setBounds(w / 2 + space, (int) (BH + (optionEH + space) * 7), w / 2 - space * 2,
						(int) (optionEH));
				maxValue.setBounds(w / 2 + space, (int) (BH + (optionEH + space) * 9), w / 2 - space * 2,
						(int) (optionEH));

				amount.setVisible(false);
				playlists.setVisible(false);
				minValue.setVisible(true);
				maxValue.setVisible(true);

			} else if (currentAction.getType() == PlayerAction.TYPE.LESSVOLUME
					|| currentAction.getType() == PlayerAction.TYPE.MOREVOLUME) { // add/remove Volume
				amount.setBounds(w / 2 + space, (int) (BH + (optionEH + space) * 7), w / 2 - space * 2,
						(int) (optionEH));

				amount.setVisible(true);
				playlists.setVisible(false);
				minValue.setVisible(false);
				maxValue.setVisible(false);

			} else if (currentAction.getType() == PlayerAction.TYPE.PLAYLIST) {
				playlists.setBounds(w / 2 + space, (int) (BH + (optionEH + space) * 7), w / 2 - space * 2,
						(int) (optionEH));

				amount.setVisible(false);
				playlists.setVisible(true);
				minValue.setVisible(false);
				maxValue.setVisible(false);
			} else {
				amount.setVisible(false);
				playlists.setVisible(false);
				minValue.setVisible(false);
				maxValue.setVisible(false);
			}
			name.setVisible(true);
			actions.setVisible(true);
			key.setVisible(true);
		} else {
			if (name != null) {
				name.setVisible(false);
				actions.setVisible(false);
				key.setVisible(false);
				minValue.setVisible(false);
				maxValue.setVisible(false);
				playlists.setVisible(false);
				amount.setVisible(false);
			}
		}
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

	private void initArduino() {
		initPlayerActions();
		arduinoManager = new ArduinoManager();
		arduinoManager.setDisconnectListener(new Runnable() {
			@Override
			public void run() {
				updateWindow();
			}
		});
		arduinoManager.setArduinoListener(new ArduinoListener() {
			@Override
			public void onInputEnter(String input, ArduinoAction a) {
				System.out.println("[ArduinoListener] :: detectedInput = " + input);
				if (arduinoManager.isLookingForInput) {
					if (currentAction.getType() == TYPE.MASTERVOLUME) { // NAMEOFPOT + SPACE + VALUE
						String nInput = "";
						boolean hasBeenSpace = false;
						for (int i = input.length() - 1; i >= 0; i--) {
							if (hasBeenSpace)
								nInput = input.charAt(i) + nInput;
							else if (input.charAt(i) == ' ')
								hasBeenSpace = true;
						}
						if (!nInput.isEmpty())
							currentAction.setKey(nInput);
					} else {
						currentAction.setKey(input);
					}
					arduinoManager.isLookingForInput = false;
					updateWindow();
				}
			}
		});

		if (musicPlayer.autoConnect && musicPlayer.initConnectIndex < arduinoManager.getPortList().size())
			arduinoManager.connect(musicPlayer.initConnectIndex);
	}

	private void initOptions() {
		name = new JButton();
		name.setContentAreaFilled(false);
		name.setBorderPainted(false);
		name.setFocusable(false);
		name.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (currentAction != null) {
					String s = FrameManager.createInputDialog(jf, "Choose a Action-Name:", null);
					if (s != null && !s.isEmpty()) {
						currentAction.setName(s);
						updateWindow();
					}
				}
			}
		});
		jf.add(name);

		key = new JButton();
		key.setContentAreaFilled(false);
		key.setBorderPainted(false);
		key.setFocusable(false);
		key.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (currentAction != null) {
					if (!arduinoManager.isLookingForInput) {
						TYPE t = PlayerAction.TYPE.values()[actions.getSelectedIndex()];
						currentAction.setType(t);
						arduinoManager.isLookingForInput = true;
					} else {
						arduinoManager.isLookingForInput = false;
					}
				}
			}
		});
		jf.add(key);

		String s[] = new String[ArduinoAction.playerActions.size()];
		for (int i = 0; i < s.length; i++) {
			s[i] = ArduinoAction.playerActions.get(i).getType().name();
		}
		actions = new JComboBox<>(s);
		actions.setFocusable(false);
		actions.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {

					if (currentAction != null) {
						TYPE t = PlayerAction.TYPE.values()[actions.getSelectedIndex()];
						currentAction.setType(t);
						updateOptions();
						updateWindow();
					}
				}
			}
		});
		jf.add(actions);

		String tempList[] = new String[amountList.length];
		for (int i = 0; i < tempList.length; i++)
			tempList[i] = amountList[i] + "%";

		amount = new JComboBox<String>(tempList);
		amount.setFocusable(false);
		amount.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					setAmountToAction(currentAction, amount.getSelectedIndex());
				}
			}
		});
		jf.add(amount);

		playlists = new JButton();
		playlists.setContentAreaFilled(false);
		playlists.setBorderPainted(false);
		playlists.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {

				ArrayList<Playlist> aList = new ArrayList<Playlist>();
				for (Playlist p : musicPlayer.folders)
					aList.add(p);
				for (Playlist p : musicPlayer.customPlaylists)
					aList.add(p);

				Playlist nList[] = new Playlist[aList.size()];
				for (int i = 0; i < nList.length; i++)
					nList[i] = aList.get(i);

				int output = FrameManager.createButtonBoxOptionPane(jf, "Select a Playlist:", "", nList);

				if (output != -1) {

					Playlist p = nList[output];
					String playlistType = p.isFolder() ? "F" : "C";
					String arg1[] = { playlistType, p.name };
					currentAction.setArgs(arg1);
				}
			}
		});
		playlists.setFocusable(false);
		jf.add(playlists);

		minValue = new JScrollBar(JScrollBar.HORIZONTAL);
		minValue.setFocusable(false);
		minValue.setMaximum(410); // steps/2 = Original Volume
		minValue.addAdjustmentListener(new AdjustmentListener() {
			@Override
			public void adjustmentValueChanged(AdjustmentEvent e) {
				if (!isUpdating) {
					setValuesToAction(currentAction);
					updateGraphics();
				} else {
					System.err.println("nope");
				}
			}
		});
		jf.add(minValue);

		maxValue = new JScrollBar(JScrollBar.HORIZONTAL);
		maxValue.setFocusable(false);
		maxValue.setMaximum(410); // steps/2 = Original Volume
		maxValue.setValue(maxValue.getMaximum() - 10);
		maxValue.addAdjustmentListener(new AdjustmentListener() {
			@Override
			public void adjustmentValueChanged(AdjustmentEvent e) {
				if (!isUpdating) {
					setValuesToAction(currentAction);
					updateGraphics();
				} else {
					System.err.println("nope");
				}
			}
		});
		jf.add(maxValue);

		// set old values for MIN- & MAX-VALUE
	}

	public void setAmountToAction(ArduinoAction action, int index) {
		String s[] = { "" + index };
		action.setArgs(s);
	}

	public void setValuesToAction(ArduinoAction action) {
		String s[] = { Integer.toString(minValue.getValue()), Integer.toString(maxValue.getValue()) };
		action.setArgs(s);
		System.out.println("set values to " + s[0] + " " + s[1]);
	}

	public void initPlayerActions() {
		ArrayList<PlayerAction> playerActions = new ArrayList<PlayerAction>();
		playerActions.add(new PlayerAction(PlayerAction.TYPE.PAUSEPLAY, new ArduinoListener() {
			@Override
			public void onInputEnter(String input, ArduinoAction a) {
				System.out.println("[PlayerAction] :: PAUSEPLAY-Action()");
				if (musicPlayer.ui.pausePlay.isVisible())
					musicPlayer.ui.pausePlay.doClick();
			}
		}));

		playerActions.add(new PlayerAction(PlayerAction.TYPE.LAST, new ArduinoListener() {
			@Override
			public void onInputEnter(String input, ArduinoAction a) {
				System.out.println("[PlayerAction] :: LAST-Action()");
				if (musicPlayer.ui.last.isVisible())
					musicPlayer.ui.last.doClick();
			}
		}));
		playerActions.add(new PlayerAction(PlayerAction.TYPE.NEXT, new ArduinoListener() {
			@Override
			public void onInputEnter(String input, ArduinoAction a) {
				System.out.println("[PlayerAction] :: NEXT-Action()");
				if (musicPlayer.ui.next.isVisible())
					musicPlayer.ui.next.doClick();
			}
		}));
		playerActions.add(new PlayerAction(PlayerAction.TYPE.RANDOM, new ArduinoListener() {
			@Override
			public void onInputEnter(String input, ArduinoAction a) {
				System.out.println("[PlayerAction] :: RANDOM-Action()");
				if (musicPlayer.ui.rand.isVisible())
					musicPlayer.ui.rand.doClick();
			}
		}));
		playerActions.add(new PlayerAction(PlayerAction.TYPE.LOOP, new ArduinoListener() {
			@Override
			public void onInputEnter(String input, ArduinoAction a) {
				System.out.println("[PlayerAction] :: LOOP-Action()");
				if (musicPlayer.ui.loop.isVisible())
					musicPlayer.ui.loop.doClick();
			}
		}));
		playerActions.add(new PlayerAction(PlayerAction.TYPE.LESSVOLUME, new ArduinoListener() {
			@Override
			public void onInputEnter(String input, ArduinoAction a) {
				System.out.println("[PlayerAction] :: LESSVOLUME-Action()");
				try {
					String s[] = a.getArgs();
					if (s != null && s.length == 1) {
						float value = amountList[Integer.parseInt(s[0])];
						musicPlayer.setVolume(musicPlayer.getVolume() - value / 200f);
						musicPlayer.ui.updateWindow();
					} else
						System.err.println("No correct args!");
				} catch (NumberFormatException | IndexOutOfBoundsException e) {
					System.err.println("Could not convert to double!");
					e.printStackTrace();
				}
			}
		}));
		playerActions.add(new PlayerAction(PlayerAction.TYPE.MOREVOLUME, new ArduinoListener() {
			@Override
			public void onInputEnter(String input, ArduinoAction a) {
				System.out.println("[PlayerAction] :: MOREVOLUME-Action()");
				try {
					String s[] = a.getArgs();
					if (s != null && s.length == 1) {
						float value = amountList[Integer.parseInt(s[0])];
						musicPlayer.setVolume(musicPlayer.getVolume() + value / 200f);
						musicPlayer.ui.updateWindow();
					} else
						System.err.println("No correct args!");
				} catch (NumberFormatException e) {
					System.err.println("Could not convert to double!");
					e.printStackTrace();
				}
			}
		}));
		playerActions.add(new PlayerAction(PlayerAction.TYPE.MASTERVOLUME, new ArduinoListener() {
			@Override
			public void onInputEnter(String input, ArduinoAction z) {
				try {
					System.out.println("[PlayerAction] :: MASTERVOLUME-Action()");
					int value = getValueOfInput(input);

					String args[] = z.getArgs();
					if (value != -1 && args.length == 2) {

						int a = Integer.parseInt(args[0]), b = Integer.parseInt(args[1]) - a,
								c = musicPlayer.ui.volume.getMaximum() - b - a - 10;
						if (a < (a + b)) {

							float inputFactor = (float) (value + 1) / 1024;
							float nVolume = (a + (b * inputFactor)) / (a + b + c);
							musicPlayer.setVolume(nVolume);
							musicPlayer.ui.hasToUpdateWindow = true;
						}
					}
				} catch (NumberFormatException e) {
					System.err.println("[ArduinoFrame] :: boxing form args to values went wrong!");
					e.printStackTrace();
				}
			}
		}));
		playerActions.add(new PlayerAction(PlayerAction.TYPE.PLAYLIST, new ArduinoListener() {
			@Override
			public void onInputEnter(String input, ArduinoAction a) {

				System.out.println("[PlayerAction] :: PLAYLIST-Action()");

				Playlist p = null;

				String[] s = a.getArgs();

				if (s[0].equals("F")) {// Folder
					p = musicPlayer.getFolderPlaylist(s[1]);
				} else if (s[0].equals("C")) { // Custom
					p = musicPlayer.getCustomPlaylist(s[1]);
				} else {
					System.err.println("[ArduinoFrame] :: No Playlist or Folder Found in Action " + a.getName());
					return;
				}

				if (p != null) {
					musicPlayer.selectedPlaylist = p;
					musicPlayer.ui.updateUI();

					if (musicPlayer.autoPlay_ArduinoPlaylist)
						musicPlayer.ui.pausePlay.doClick();
				}
			}
		}));
		ArduinoAction.playerActions = playerActions;
	}

	// arduino
	public int getValueOfInput(String input) {
		if (input == null || input.isEmpty())
			return -1;
		try {
			String s = "";
			for (int i = input.length() - 1; i >= 0; i--) {
				if (input.charAt(i) == ' ')
					break;
				s = input.charAt(i) + s;
			}
			int a = Integer.parseInt(s);
			return a;
		} catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
			return -1;
		}
	}

	public void updateGraphics() {
		repaint();
	}

	public void setVisible(boolean b) {
		super.setVisible(b);
		jf.setVisible(b);
	}

	public void requestFocus() {
		jf.requestFocus();
	}
}
