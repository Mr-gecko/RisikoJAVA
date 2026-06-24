package client.view.panels;

import javax.swing.JPanel;
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
import java.util.List;
import java.util.function.Consumer;

import javax.swing.JTextArea;
import javax.swing.BoxLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Component;
import javax.swing.Box;
import javax.swing.SwingConstants;

public class GameInfoPanel extends JPanel {
	private PlayerInfoPanel playerInfoPanel;
	private CountryInfoPanel countryInfoPanel;
	private TurnInfoPanel turnInfoPanel;

	public GameInfoPanel() {
		
		TitledBorder border = new TitledBorder(null, "Game Info", TitledBorder.LEADING, TitledBorder.TOP, null, null);
		setBorder(border);
		border.setTitleFont( border.getTitleFont().deriveFont(Font.BOLD) );
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		this.turnInfoPanel = new TurnInfoPanel();
		add(turnInfoPanel);
		
		Component verticalStrut = Box.createVerticalStrut(20);
		add(verticalStrut);
		
		this.countryInfoPanel = new CountryInfoPanel();
		add(countryInfoPanel);
		
		Component verticalStrut_1 = Box.createVerticalStrut(20);
		add(verticalStrut_1);
		
		this.playerInfoPanel = new PlayerInfoPanel();
		add(playerInfoPanel);
		
		clear();
	}
	
	public void update(Game snapshot, Player localPlayer) {
		this.turnInfoPanel.update(snapshot.getCurrentPhase(), snapshot.getCurrentPlayer());
        if (localPlayer != null) {
            List<Card> cards = localPlayer.getDeck().getCards();
            int nCards = cards != null ? (cards.isEmpty() ? 0 : cards.size()) : 0;
            this.playerInfoPanel.update(
                snapshot.getNumberOfCountriesOwnedBy(localPlayer),
                snapshot.getTotalTroopsOf(localPlayer),
                localPlayer.getAvailableTroops(),
                snapshot.calculateTroops(localPlayer),
                nCards
            );
	        repaint();
	    } else {
	        clear();
	    }
	}

	
	public void clear() {
		repaint();
	}
	
	public CountryInfoPanel getCountryInfoPanel() {
		return this.countryInfoPanel;
	}
	
//	@Override
//	protected void paintComponent(Graphics g) {
//		super.paintComponent(g);
//	}
	
	public void setSpectatorMode(boolean value) {
		setVisible(!value);
	}

}

