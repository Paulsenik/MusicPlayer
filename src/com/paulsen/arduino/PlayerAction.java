package com.paulsen.arduino;

public class PlayerAction {

	public static enum TYPE {
		PAUSEPLAY, LAST, NEXT, RANDOM, LOOP, LESSVOLUME, MOREVOLUME, MASTERVOLUME, PLAYLIST
	};

	private TYPE type;
	private ArduinoListener action;

	public PlayerAction(TYPE type, ArduinoListener action) {
		this.type = type;
		this.action = action;
	}

	public TYPE getType() {
		return type;
	}

	public void run(String input, ArduinoAction a) {
//		System.out.println("[PlayerAction] :: run()");
		if (action != null)
			action.onInputEnter(input, a);
		else
			System.err.println("[PlayerAction] :: nothing to play!");
	}

}
