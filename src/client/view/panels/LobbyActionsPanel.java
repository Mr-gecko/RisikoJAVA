package client.view.panels;

import javax.swing.JPanel;
import javax.swing.JButton;
import java.awt.GridLayout;
import java.awt.Component;
import javax.swing.Box;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.border.TitledBorder;
import javax.swing.JCheckBox;

public class LobbyActionsPanel extends JPanel {
	
	public JButton leaveButton;
	public JButton readyButton;
	public JButton startButton;
	public JButton leaveSpectatorButton;
	public JButton addBotButton;
	public JButton removeBotButton;
	private Component verticalStrut_1;
	private JButton btnRemoveBot;

	public LobbyActionsPanel() {
		setBorder(new TitledBorder(null, "Actions", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0, 12, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		this.startButton = new JButton("start");
		this.startButton.setEnabled(false);
//        this.startButton.setVisible(true); // hidden by default
        GridBagConstraints gbc_startButton = new GridBagConstraints();
        gbc_startButton.fill = GridBagConstraints.BOTH;
        gbc_startButton.insets = new Insets(0, 0, 5, 5);
        gbc_startButton.gridx = 1;
        gbc_startButton.gridy = 1; // above ready button
        add(startButton, gbc_startButton);
		
		this.readyButton = new JButton("ready");
		GridBagConstraints gbc_readyButton = new GridBagConstraints();
		gbc_readyButton.fill = GridBagConstraints.BOTH;
		gbc_readyButton.insets = new Insets(0, 0, 5, 5);
		gbc_readyButton.gridx = 1;
		gbc_readyButton.gridy = 2;
		add(readyButton, gbc_readyButton);
//		readyButton.addActionListener();
		
		Component verticalStrut = Box.createVerticalStrut(5);
		GridBagConstraints gbc_verticalStrut = new GridBagConstraints();
		gbc_verticalStrut.fill = GridBagConstraints.BOTH;
		gbc_verticalStrut.insets = new Insets(0, 0, 5, 5);
		gbc_verticalStrut.gridx = 1;
		gbc_verticalStrut.gridy = 3;
		add(verticalStrut, gbc_verticalStrut);
		
		addBotButton = new JButton("add bot");
		GridBagConstraints gbc_botGame = new GridBagConstraints();
		gbc_botGame.fill = GridBagConstraints.BOTH;
		gbc_botGame.insets = new Insets(0, 0, 5, 5);
		gbc_botGame.gridx = 1;
		gbc_botGame.gridy = 4;
		add(addBotButton, gbc_botGame);
		
		removeBotButton = new JButton("remove bot");
		GridBagConstraints gbc_btnRemoveBot = new GridBagConstraints();
		gbc_btnRemoveBot.fill = GridBagConstraints.BOTH;
		gbc_btnRemoveBot.insets = new Insets(0, 0, 5, 5);
		gbc_btnRemoveBot.gridx = 1;
		gbc_btnRemoveBot.gridy = 5;
		add(removeBotButton, gbc_btnRemoveBot);
		
		verticalStrut_1 = Box.createVerticalStrut(5);
		GridBagConstraints gbc_verticalStrut_1 = new GridBagConstraints();
		gbc_verticalStrut_1.insets = new Insets(0, 0, 5, 5);
		gbc_verticalStrut_1.gridx = 1;
		gbc_verticalStrut_1.gridy = 6;
		add(verticalStrut_1, gbc_verticalStrut_1);
		
		leaveButton = new JButton("Leave");
		GridBagConstraints gbc_leaveButton = new GridBagConstraints();
		gbc_leaveButton.insets = new Insets(0, 0, 5, 5);
		gbc_leaveButton.fill = GridBagConstraints.BOTH;
		gbc_leaveButton.gridx = 1;
		gbc_leaveButton.gridy = 7;
		add(leaveButton, gbc_leaveButton);
		
		leaveSpectatorButton = new JButton("Leave");
		GridBagConstraints gbc_leaveButton_1 = new GridBagConstraints();
		gbc_leaveButton_1.fill = GridBagConstraints.BOTH;
		gbc_leaveButton_1.insets = new Insets(0, 0, 5, 5);
		gbc_leaveButton_1.gridx = 1;
		gbc_leaveButton_1.gridy = 8;
		add(leaveSpectatorButton, gbc_leaveButton_1);
		this.leaveSpectatorButton.setVisible(false);
		

	}
	
	public void setStartButtonEnabled(boolean enabled) {
        this.startButton.setEnabled(enabled);
        repaint();
    }
	
	public void setSpectatorMode(boolean isSpectator) {
		this.startButton.setVisible(!isSpectator);
		this.readyButton.setVisible(!isSpectator);
		this.leaveButton.setVisible(!isSpectator);
		this.addBotButton.setVisible(!isSpectator);
		this.removeBotButton.setVisible(!isSpectator);
		this.leaveSpectatorButton.setVisible(isSpectator);
	}

}
