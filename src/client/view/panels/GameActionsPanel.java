package client.view.panels;

import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import client.controller.ClientInput;
import client.view.renderers.CountryRenderer;
import model.Card;
import model.Country;
import model.Game;
import model.Player;
import model.enums.TurnPhase;

import javax.swing.JLabel;
import java.awt.GridLayout;
import java.awt.GridBagLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.security.spec.ECGenParameterSpec;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.JTextArea;
import javax.swing.BoxLayout;
import javax.swing.JButton;

import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Component;
import javax.swing.Box;
import javax.swing.SwingConstants;

public class GameActionsPanel extends JPanel {
	
	private JPanel generalActions;
	private JPanel gameActions;
	
	private JButton cards;
	private JButton nextPhase;
	
	private JButton surrender;
	private JButton leave;
	private JButton leaveSpectator;

	public GameActionsPanel() {
		
//		setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		setBorder(new TitledBorder(null, "actions", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		this.gameActions = new JPanel();
		TitledBorder gameBorder = new TitledBorder(null, "[game]", TitledBorder.LEADING, TitledBorder.TOP, null, null);
		this.gameActions.setBorder(gameBorder);
		gameBorder.setTitleFont( gameBorder.getTitleFont().deriveFont(Font.BOLD) );
		
		
		this.cards = new JButton("Cards");
		this.nextPhase = new JButton("Next Phase");
		
		this.gameActions.add(cards);
		this.gameActions.add(nextPhase);
		add(gameActions);
		
		Component verticalStrut_1 = Box.createVerticalStrut(20);
		add(verticalStrut_1);
		
		this.generalActions = new JPanel();
		TitledBorder generalBorder = new TitledBorder(null, "[general]", TitledBorder.LEADING, TitledBorder.TOP, null, null);
		this.generalActions.setBorder(generalBorder);
		generalBorder.setTitleFont( generalBorder.getTitleFont().deriveFont(Font.BOLD) );
		
		this.surrender = new JButton("Surrender");
		this.surrender.setVisible(false);
		
		this.generalActions.add(surrender);
		add(generalActions);
		this.leave = new JButton("Leave");
		this.generalActions.add(leave);
		
		leaveSpectator = new JButton("Leave");
		generalActions.add(leaveSpectator);
		this.leaveSpectator.setVisible(false);
		
	}
	
	public JButton getCardsButton() {
		return this.cards;
	}
	
	public JButton getNextPhaseButton() {
		return this.nextPhase;
	}
	
	public JButton getSurrenderButton() {
		return this.surrender;
	}
	
	public JButton getLeaveButton() {
		return this.leave;
	}
	
	public JButton getLeaveSpectatorButton() {
		return this.leaveSpectator;
	}
	
	public void setSpectatorMode(boolean value) {
		this.gameActions.setVisible(!value);
		this.surrender.setVisible(!value);
		this.leave.setVisible(!value);
		this.leaveSpectator.setVisible(value);
	}

}

