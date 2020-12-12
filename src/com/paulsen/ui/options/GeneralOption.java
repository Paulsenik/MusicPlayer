package com.paulsen.ui.options;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class GeneralOption {

	private JButton jb;
	private JComboBox<String> box;
	private String choices[];

	private OptionListener optionChange;

	private JFrame parentFrame;
	private String title;

	public GeneralOption(JFrame parentFrame, String title, String choices[], OptionListener optionChange) {
		this.title = title;
		this.parentFrame = parentFrame;
		this.choices = choices;
		this.optionChange = optionChange;

		box = new JComboBox<>(choices);
		jb = new JButton();
		jb.setContentAreaFilled(false);
		jb.setBorderPainted(false);
		jb.setFocusable(false);
		jb.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				optionChange.optionChanged(createComboBoxOptionPane(title));
			}
		});
		parentFrame.add(jb);
	}

	private int createComboBoxOptionPane(String title) {
		JOptionPane.showMessageDialog(parentFrame, box, title, JOptionPane.QUESTION_MESSAGE);
		return box.getSelectedIndex();
	}

	public JButton getJB() {
		return jb;
	}

	public String getTitle() {
		return title;
	}

	public String getSelected() {
		return optionChange.optionDispayed(box.getSelectedIndex());
	}

}
