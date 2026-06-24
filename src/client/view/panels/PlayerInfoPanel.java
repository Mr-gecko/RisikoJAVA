package client.view.panels;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import model.Card;
import model.Player;
import model.enums.TurnPhase;

import javax.swing.JLabel;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.util.List;
import javax.swing.SwingConstants;

public class PlayerInfoPanel extends JPanel {
	
	private JLabel countries;
	private JLabel deployTroops;
	private JLabel nextTurnReinforcement;
	private JLabel cards;

	public PlayerInfoPanel() {
		TitledBorder border = new TitledBorder(null, "[My Info]", TitledBorder.LEADING, TitledBorder.TOP, null, null);
		border.setTitleFont( border.getTitleFont().deriveFont(Font.BOLD) );
		setBorder(border);
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		JPanel CountryPanel = new JPanel();
		add(CountryPanel);
		GridBagLayout gbl_CountryPanel = new GridBagLayout();
		gbl_CountryPanel.columnWidths = new int[]{46, 0, 85, 0};
		gbl_CountryPanel.rowHeights = new int[]{13, 0, 0, 0, 0};
		gbl_CountryPanel.columnWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_CountryPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		CountryPanel.setLayout(gbl_CountryPanel);
		
		JLabel countriesTag = new JLabel("countries:");
		GridBagConstraints gbc_countriesTag = new GridBagConstraints();
		gbc_countriesTag.anchor = GridBagConstraints.NORTHWEST;
		gbc_countriesTag.insets = new Insets(0, 0, 5, 5);
		gbc_countriesTag.gridx = 0;
		gbc_countriesTag.gridy = 0;
		CountryPanel.add(countriesTag, gbc_countriesTag);
		
		this.countries = new JLabel("TEST_COUNTRIES");
		GridBagConstraints gbc_countries = new GridBagConstraints();
		gbc_countries.insets = new Insets(0, 0, 5, 0);
		gbc_countries.anchor = GridBagConstraints.NORTHWEST;
		gbc_countries.gridx = 2;
		gbc_countries.gridy = 0;
		CountryPanel.add(countries, gbc_countries);
		
		JLabel nextTurnReinforcementTag = new JLabel("next turn troops:");
		GridBagConstraints gbc_nextTurnReinforcementTag = new GridBagConstraints();
		gbc_nextTurnReinforcementTag.anchor = GridBagConstraints.WEST;
		gbc_nextTurnReinforcementTag.insets = new Insets(0, 0, 5, 5);
		gbc_nextTurnReinforcementTag.gridx = 0;
		gbc_nextTurnReinforcementTag.gridy = 1;
		CountryPanel.add(nextTurnReinforcementTag, gbc_nextTurnReinforcementTag);
		
		this.nextTurnReinforcement = new JLabel("TEST_REINFORCEMENT");
		GridBagConstraints gbc_nextTurnReinforcement = new GridBagConstraints();
		gbc_nextTurnReinforcement.anchor = GridBagConstraints.WEST;
		gbc_nextTurnReinforcement.insets = new Insets(0, 0, 5, 0);
		gbc_nextTurnReinforcement.gridx = 2;
		gbc_nextTurnReinforcement.gridy = 1;
		CountryPanel.add(nextTurnReinforcement, gbc_nextTurnReinforcement);
		
		JLabel deployTroopsTag = new JLabel("deployable:");
		GridBagConstraints gbc_deployTroopsTag = new GridBagConstraints();
		gbc_deployTroopsTag.anchor = GridBagConstraints.WEST;
		gbc_deployTroopsTag.insets = new Insets(0, 0, 5, 5);
		gbc_deployTroopsTag.gridx = 0;
		gbc_deployTroopsTag.gridy = 2;
		CountryPanel.add(deployTroopsTag, gbc_deployTroopsTag);
		
		this.deployTroops = new JLabel("TEST_DEPLOYABLE");
		GridBagConstraints gbc_deployTroops = new GridBagConstraints();
		gbc_deployTroops.anchor = GridBagConstraints.WEST;
		gbc_deployTroops.insets = new Insets(0, 0, 5, 0);
		gbc_deployTroops.gridx = 2;
		gbc_deployTroops.gridy = 2;
		CountryPanel.add(deployTroops, gbc_deployTroops);
		
		JLabel cardsTag = new JLabel("cards:");
		GridBagConstraints gbc_cardsTag = new GridBagConstraints();
		gbc_cardsTag.anchor = GridBagConstraints.WEST;
		gbc_cardsTag.insets = new Insets(0, 0, 0, 5);
		gbc_cardsTag.gridx = 0;
		gbc_cardsTag.gridy = 3;
		CountryPanel.add(cardsTag, gbc_cardsTag);
		
		this.cards = new JLabel("TEST_CARDS");
		GridBagConstraints gbc_cards = new GridBagConstraints();
		gbc_cards.anchor = GridBagConstraints.WEST;
		gbc_cards.gridx = 2;
		gbc_cards.gridy = 3;
		CountryPanel.add(cards, gbc_cards);
		
		clear();
	}

	
	public void clear() {
		this.countries.setText("");
		this.deployTroops.setText("");
		this.nextTurnReinforcement.setText("");
		this.cards.setText("");
		repaint();
	}
	
	/**
	 * 
	 * @param nCountries
	 * @param totalTroops
	 * @param deployable
	 * @param nextTurnTroops
	 * @param nCards
	 */
	public void update(int nCountries, int totalTroops, int deployable, int nextTurnTroops, int nCards) {
		this.countries.setText("" + nCountries);
//		this.totalTroops.setText("" + totalTroops);
		this.deployTroops.setText("" + deployable);
		this.nextTurnReinforcement.setText("" + nextTurnTroops);
		this.cards.setText("" + nCards);	 
		repaint();
	}
	
}
