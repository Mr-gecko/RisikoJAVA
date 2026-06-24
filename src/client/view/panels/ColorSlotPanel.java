package client.view.panels;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;

import model.PlayerColor;

import javax.swing.JButton;

public class ColorSlotPanel extends JPanel {

	private JLabel nameLabel;
	private RoundPanel colorPanel;
	private PlayerColor pColor;
	private JButton pickButton;
	private boolean picked;
	
	public ColorSlotPanel(PlayerColor pColor) {
		this.pColor = pColor;
		setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
		
		this.colorPanel = new RoundPanel(15, pColor.getColor()); // transparent color
		this.colorPanel.setPreferredSize(new Dimension(20,20));
		add(colorPanel);
		
//		Component horizontalStrut = Box.createHorizontalStrut(5);
//		add(horizontalStrut);
		
		this.nameLabel = new JLabel("DEF_PLAYER");
		add(nameLabel);
		
		Component horizontalStrut_1 = Box.createHorizontalStrut(15);
		add(horizontalStrut_1);
		
		this.picked = false;
		pickButton = new JButton("pick");
		add(pickButton);
		
		setPreferredSize(getPreferredSize());
		nameLabel.setText("");
		
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		this.pickButton.setVisible(!picked);
		repaint();
	}
	
	public JButton getButton() {
		return this.pickButton;
	}
	
	public PlayerColor getColor() {
		return this.pColor;
	}
	
	public void togglePicked() {
		this.picked = !picked;
	}
	
	public void setPlayerLabel(String name) {
		this.nameLabel.setText(name);
	}
	
	// . change picked variable value
	// . edit name label to who picked the color
	// . repaint the component to hide/show pick button
	// 
//	public PlayerColor pickAction(String name) {
//		togglePicked();
//		setPlayerLabel(name);
//		repaint();
//		return this.pColor;
//	}
	
	public void pick(String name) {
	    togglePicked();
	    setPlayerLabel(name);
	    repaint();
	}
	
	// . reset the component
	public void reset() {
		this.picked = false;
		setPlayerLabel("");
		repaint();
	}
	

}
