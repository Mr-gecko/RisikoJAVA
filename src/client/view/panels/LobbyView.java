package client.view.panels;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagLayout;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

import model.Lobby;

public class LobbyView extends JPanel {
	
	private SlotsPanel slotsPanel;
	public LobbyActionsPanel actionsPanel;
	public ColorsPanel colorsPanel;

	public LobbyView() {
		setLayout(new BorderLayout(0, 0));
		
		
		JPanel panel = new JPanel();
		add(panel);
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

		
		this.slotsPanel = new SlotsPanel();
		panel.add(slotsPanel);
		
		this.actionsPanel = new LobbyActionsPanel();
		GridBagLayout gridBagLayout = (GridBagLayout) actionsPanel.getLayout();
		gridBagLayout.rowWeights = new double[]{1.0, 0.0, 0.0, 0.0, 1.0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0, 0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, 0.0, 1.0};
		gridBagLayout.columnWidths = new int[]{0, 0, 0};
		panel.add(actionsPanel);

		Component verticalStrut_3 = Box.createVerticalStrut(20);
		panel.add(verticalStrut_3);
		
		
		this.colorsPanel = new ColorsPanel();
		panel.add(colorsPanel);
		
		Component verticalStrut_2 = Box.createVerticalStrut(220);
		panel.add(verticalStrut_2);
		
		
		
		int verticalBorder = 20;
		int horizontalBorder = 400;
		
		Component verticalStrut = Box.createVerticalStrut(verticalBorder);
		add(verticalStrut, BorderLayout.NORTH);
		
		Component horizontalStrut = Box.createHorizontalStrut(horizontalBorder);
		add(horizontalStrut, BorderLayout.WEST);
		
		Component horizontalStrut_1 = Box.createHorizontalStrut(horizontalBorder);
		add(horizontalStrut_1, BorderLayout.EAST);
		
		Component verticalStrut_1 = Box.createVerticalStrut(61);
		add(verticalStrut_1, BorderLayout.SOUTH);
		
	}
	
	public void update(Lobby lobby) {
		slotsPanel.update(lobby);
		colorsPanel.updateColors(lobby);
		actionsPanel.setStartButtonEnabled(lobby.canStart());
		repaint();
	}
	
	public ColorsPanel getColorsPanel() {
		return this.colorsPanel;
	}
	
	public void setSpectatorMode(boolean isSpectator) {
		this.actionsPanel.setSpectatorMode(isSpectator);
	}
	
}

