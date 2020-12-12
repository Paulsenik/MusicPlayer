package com.paulsen.ui.options;

public interface OptionListener {
	
	public void optionChanged(int index);
	
	public String optionDispayed(int index); // returns what is Displayed as selected

}
