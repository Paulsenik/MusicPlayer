package com.paulsen.arduino;

import java.util.ArrayList;

import com.paulsen.arduino.PlayerAction.TYPE;

public class ArduinoAction {
	// executables contained in actions
	public static volatile ArrayList<PlayerAction> playerActions = new ArrayList<PlayerAction>();

	private PlayerAction action;
	private String key;
	private String name;
	private String[] args;

	public ArduinoAction() {
	}

	public PlayerAction getAction() {
		return action;
	}

	public TYPE getType() {
		if (action != null)
			return action.getType();
		return null;
	}

	public void setType(TYPE type) {
		for (int i = 0; i < playerActions.size(); i++)
			if (playerActions.get(i).getType() == type) {
				this.action = playerActions.get(i);
				return;
			}
		System.err.println("no action found");
	}

	public String getKey() {
		return key == null ? "" : key;
	}

	public void setKey(String s) {
		key = s;
	}

	public void run(String input, ArduinoAction a) {
		action.run(input, a);
	}

	public String getName() {
		return (name == null ? (key == null ? "new Action" : key) : name);
	}

	public void setName(String s) {
		this.name = s;
	}

	public void setArgs(String[] args) {
		this.args = args;
	}
	
	public String[] getArgs() {
		return args;
	}
}
