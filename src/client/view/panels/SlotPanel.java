package client.view.panels;

import javax.swing.JPanel;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import javax.swing.JLabel;
import java.awt.Insets;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.border.EtchedBorder;
import javax.swing.border.SoftBevelBorder;

import model.SlotStatus;

import javax.swing.border.BevelBorder;
import javax.swing.border.CompoundBorder;

public class SlotPanel extends JPanel {
	
	private static Color TRANSPARENT_COLOR = new Color(0,0,0,0);
	
	private JLabel nameLabel;
	private JLabel readyLabel;
	private RoundPanel colorPanel;

	public SlotPanel() {
		setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
		
		this.colorPanel = new RoundPanel(15, TRANSPARENT_COLOR); // transparent color
		this.colorPanel.setPreferredSize(new Dimension(20,20));
		add(colorPanel);
		
//		Component horizontalStrut = Box.createHorizontalStrut(5);
//		add(horizontalStrut);
		
		this.nameLabel = new JLabel("DEF_PLAYER");
		add(nameLabel);
		
		Component horizontalStrut_1 = Box.createHorizontalStrut(30);
		add(horizontalStrut_1);
		
		this.readyLabel = new JLabel("DEF_STATUS");
		add(readyLabel);
		
		setPreferredSize(getPreferredSize());
		nameLabel.setText("");
		readyLabel.setText("");

	}
	
	public void setPlayerColor(Color color) {
		this.colorPanel.setColor(color);
	}
	
	public void setPlayerName(String name) {
		this.nameLabel.setText(name);
	}
	
	public void setReadyStatus(SlotStatus status) {
		switch(status) {
		case READY -> this.readyLabel.setText("READY");
		case CONNECTED -> this.readyLabel.setText("");
		case EMPTY -> this.readyLabel.setText("");
		}
	}

	public void setDefaultColor() {
		setPlayerColor(new Color(155, 155, 155));
	}
	
	public void setDefaultName() {
		setPlayerName("DEFAULT_NAME");
	}
	
	public void setDefaultStatus() {
		setReadyStatus(SlotStatus.CONNECTED);
	}
	
	public void reset() {
		setReadyStatus(SlotStatus.EMPTY);
		setPlayerName("");
		setPlayerColor(TRANSPARENT_COLOR);
	}
	

}
