package client.view.panels;

import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import client.controller.ClientInput;
import client.view.renderers.CountryRenderer;
import model.Country;

import javax.swing.JLabel;
import java.awt.GridLayout;
import java.awt.GridBagLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.util.function.Consumer;

import javax.swing.JTextArea;
import javax.swing.BoxLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Component;
import javax.swing.Box;

public class CountryInfoPanel extends JPanel {
	
	
	private JLabel name;
	private JLabel continent;
	private JLabel troops;
	private JLabel player;
	private RoundPanel roundPanel;
	private Consumer<ClientInput> onCountryClicked;

	public CountryInfoPanel() {
		
		TitledBorder border = new TitledBorder(null, "[Selected Country]", TitledBorder.LEADING, TitledBorder.TOP, null, null);
		setBorder(border);
		border.setTitleFont( border.getTitleFont().deriveFont(Font.BOLD) );
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		JPanel countryPanel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) countryPanel.getLayout();
		flowLayout.setAlignment(FlowLayout.LEFT);
		add(countryPanel);
		
		JLabel nameTag = new JLabel("country:");
		countryPanel.add(nameTag);
		
		this.name = new JLabel("TEST_NAME");
		countryPanel.add(name);
		
		JPanel continentPanel = new JPanel();
		FlowLayout fl_continentPanel = (FlowLayout) continentPanel.getLayout();
		fl_continentPanel.setAlignment(FlowLayout.LEFT);
		add(continentPanel);
		
		JLabel continentTag = new JLabel("continent:");
		continentPanel.add(continentTag);
		
		this.continent = new JLabel("TEST_CONTINENT");
		continentPanel.add(continent);
		
		JPanel troopsPanel = new JPanel();
		FlowLayout flowLayout_1 = (FlowLayout) troopsPanel.getLayout();
		flowLayout_1.setAlignment(FlowLayout.LEFT);
		add(troopsPanel);
		
		JLabel troopsTag = new JLabel("troops:  ");
		troopsPanel.add(troopsTag);
		
		this.troops = new JLabel("TEST_NAME");
		troopsPanel.add(troops);
		
		JPanel playerPanel = new JPanel();
		FlowLayout flowLayout_2 = (FlowLayout) playerPanel.getLayout();
		flowLayout_2.setAlignment(FlowLayout.LEFT);
//		add(playerPanel); // not used
		
		JLabel playerTag = new JLabel("player:  ");
		playerPanel.add(playerTag);
		
		this.player = new JLabel("TEST_NAME");
		playerPanel.add(player);
		
		JPanel colorPanel = new JPanel();
		FlowLayout flowLayout_3 = (FlowLayout) colorPanel.getLayout();
		flowLayout_3.setAlignment(FlowLayout.LEFT);
		add(colorPanel);
		
		JLabel colorTag = new JLabel("color:");
		colorPanel.add(colorTag);
		
		Component horizontalStrut = Box.createHorizontalStrut(10);
		colorPanel.add(horizontalStrut);
		
		this.roundPanel = new RoundPanel(5);
		colorPanel.add(roundPanel);
		
		clear();
	}
	
	public void update(Country country) {
		if (country != null) {
			this.name.setText(country.getName());
			this.continent.setText(country.getContinentName());
			this.troops.setText(""+country.getTroops());
			this.roundPanel.setColor(country.getOwner().getColor());
			repaint();
		} else {
			clear();
		}
	}
	
	public void setOnCountryClicked(Consumer<ClientInput> onCountryClicked) {
	    this.onCountryClicked = onCountryClicked;
	}
	
	public void clear() {
		this.name.setText("");
		this.continent.setText("");
		this.troops.setText("");
		this.roundPanel.setTransparent();
		repaint();
	}
	
//	@Override
//	protected void paintComponent(Graphics g) {
//		super.paintComponent(g);
//	}

}
