package com.paulsen.arduino;

import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Scanner;

import com.fazecast.jSerialComm.SerialPort;
import com.paulsen.arduino.PlayerAction.TYPE;

public class ArduinoManager {

	public boolean isLookingForInput = false;

	private SerialPort chosenPort;
	private volatile ArrayList<String> portList = new ArrayList<>();
	private volatile ArrayList<ArduinoAction> arduinoActions = new ArrayList<>();

	private ArduinoListener listener;
	private Runnable disconnectAction;
	private Thread t;
	private boolean isConnected = false;

	private int lastConnectedIndex;

	int selectedIndex = 0;

	public ArduinoManager() {
		updatePorts();
	}

	public void setActions(ArrayList<ArduinoAction> actions) {
		arduinoActions = actions;
	}

	public void setDisconnectListener(Runnable r) {
		disconnectAction = r;
	}

	public void setArduinoListener(ArduinoListener a) {
		listener = a;
	}

	public void createAction() {
		arduinoActions.add(new ArduinoAction());
	}

	public void removeAction(int index) {
		arduinoActions.remove(index);
	}

	public int getActionIndex(ArduinoAction action) {
		for (int i = 0; i < arduinoActions.size(); i++)
			if (action == arduinoActions.get(i))
				return i;
		return -1;
	}

	public int getTYPEindex(ArduinoAction action) {
		for (int i = 0; i < TYPE.values().length; i++) {
			if (action.getType() == TYPE.values()[i])
				return i;
		}
		return -1;
	}

	private void initThread() {
		t = new Thread(new Runnable() {
			@Override
			public void run() {
				Scanner scn = new Scanner(chosenPort.getInputStream());
				while (isConnected) {
					try {
						String s = scn.nextLine();

						if (!isLookingForInput) {
							// Actions
							for (ArduinoAction a : arduinoActions)
								if (a != null && s != null && !s.isEmpty()) {
									String extraKey = extractKey(s);
									if (a.getAction() != null && a.getAction().getType() == TYPE.MASTERVOLUME
											&& extraKey != null && extraKey.equals(a.getKey()))
										a.run(s, a);
									else if (s.equals(a.getKey()))
										a.run(s, a);
								}
						} else {
							// listener
							listener.onInputEnter(s, null);
						}

					} catch (NumberFormatException e) {
						System.err.println("[ArduinoManager] :: Error with communikation!");
					} catch (NoSuchElementException e) {
						scn.close();
						isConnected = false;
						System.out.println("[ArduinoManager] :: Connection closed");
						try {
							disconnectAction.run();
						} catch (NullPointerException e2) {
							System.err.println("[ArduinoManager] :: Something went wrong!");
						}
						return;
					}
				}
				scn.close();
			}
		});
	}

	private synchronized String extractKey(String input) { // only for MASTERVOLUME!
		try {
			String nInput = "";
			boolean hasBeenSpace = false;
			for (int i = input.length() - 1; i >= 0; i--) {
				if (hasBeenSpace)
					nInput = input.charAt(i) + nInput;
				else if (input.charAt(i) == ' ')
					hasBeenSpace = true;
			}
			if (nInput.isEmpty())
				return null;
			return nInput;
		} catch (ArrayIndexOutOfBoundsException e) {
			return null;
		}
	}

	public void updatePorts() {
		portList.clear();
		SerialPort[] portNames = SerialPort.getCommPorts();
		for (int i = 0; i < portNames.length; i++) {
			portList.add(portNames[i].getSystemPortName().toString());
		}
	}

	public String getCurrentPortName() {
		if (chosenPort != null)
			return getPortList().get(lastConnectedIndex);
		return "";
	}

	public synchronized boolean connect(int portIndex) {
		lastConnectedIndex = portIndex;
		if (!isConnected) {
			chosenPort = SerialPort.getCommPort(portList.get(portIndex));
			chosenPort.setComPortTimeouts(SerialPort.TIMEOUT_SCANNER, 0, 0);
			chosenPort.openPort();
			initThread();
			t.start();
			isConnected = true;
			System.out.println("[ArduinoManager] :: connected");
			return true;
		} else if (disConnect())
			return connect(portIndex);
		return false;
	}

	public synchronized boolean disConnect() {
		if (isConnected) {
			t.interrupt();
			chosenPort.closePort();
			isConnected = false;
			System.out.println("[ArduinoManager] :: disconnected!");
			return true;
		}
		return false;
	}

	public boolean isConnected() {
		return isConnected;
	}

	public ArrayList<ArduinoAction> getActions() {
		return arduinoActions;
	}

	public ArrayList<String> getPortList() {
		return portList;
	}

}
