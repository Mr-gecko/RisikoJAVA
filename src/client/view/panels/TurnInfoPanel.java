package client.view.panels;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import model.Card;
import model.Game;
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
import javax.swing.SwingUtilities;

public class TurnInfoPanel extends JPanel {
	
	private JLabel phase;
	private JLabel turn;
	private RoundPanel turnColor;

	public TurnInfoPanel() {
		TitledBorder border = new TitledBorder(null, "[Turn Info]", TitledBorder.LEADING, TitledBorder.TOP, null, null);
		border.setTitleFont( border.getTitleFont().deriveFont(Font.BOLD) );
		setBorder(border);
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		JPanel turnPanel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) turnPanel.getLayout();
		flowLayout.setAlignment(FlowLayout.LEFT);
		add(turnPanel);
		
		JLabel turnTag = new JLabel("turn of:");
		turnPanel.add(turnTag);
		
		this.turn = new JLabel("TEST_TURN");
		turnPanel.add(turn);
		
		this.turnColor = new RoundPanel(5);
		turnPanel.add(turnColor);
		
		JPanel phasePanel = new JPanel();
		FlowLayout fl_phasePanel = (FlowLayout) phasePanel.getLayout();
		fl_phasePanel.setAlignment(FlowLayout.LEFT);
		add(phasePanel);
		
		JLabel phaseTag = new JLabel("current phase:");
		phasePanel.add(phaseTag);
		
		this.phase = new JLabel("TEST_PHASE");
		phasePanel.add(phase);
		
		
		clear();
	}

	
	public void clear() {
		this.turn.setText("");
		this.turnColor.setColor(new Color(0,0,0,0));
		this.phase.setText("");
		repaint();
	}
	
	public void update(TurnPhase phase, Player currentPlayer) {
	    if (phase != null) {
	        this.phase.setText("" + phase);
	        if (currentPlayer != null) {
	            this.turn.setText(currentPlayer.getName()); // whose turn
	            this.turnColor.setColor(currentPlayer.getColor().getColor());
	        }
	        repaint();
	    } else {
	        clear();
	    }
	}
	
}
