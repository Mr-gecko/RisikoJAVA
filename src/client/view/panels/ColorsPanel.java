package client.view.panels;

import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JPanel;

import model.Lobby;
import model.Player;
import model.PlayerColor;
import javax.swing.BoxLayout;
import javax.swing.border.EmptyBorder;

public class ColorsPanel extends JPanel {

	private ArrayList<PlayerColor> availableColors;
	private ColorSlotPanel[] colorPanels;
	
	public ColorsPanel() {
		setBorder(new EmptyBorder(0, 0, 0, 0));
		this.availableColors = new ArrayList<>(Arrays.asList(PlayerColor.values()));
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this.availableColors.remove(PlayerColor.NOT_SELECTED);
		
		this.colorPanels = new ColorSlotPanel[availableColors.size()];
		
		for (PlayerColor pColor : availableColors) {
			int index = availableColors.indexOf(pColor);
			this.colorPanels[index] = new ColorSlotPanel(pColor);
			add(this.colorPanels[index]);
		}
		
	}
	
	public ColorSlotPanel[] getColorSlotPanels() {
		return this.colorPanels;
	}
	
	/**
	 * method to call when picking a color.
	 * every time you pick a different color the previous one has to be 
	 * reset for others players or the player itself to be able to pick it again
	 * after the first time
	 */
	public void resetColorSlotPanels() {
		for (ColorSlotPanel panel : colorPanels) {
			panel.reset();
		}
	}
	
	/**
	 * update the view of each client based on the current lobby they know
	 * @param lobby
	 */
	public void updateColors(Lobby lobby) {
	    // reset all first
	    resetColorSlotPanels();
	    // mark taken colors
	    for (Player player : lobby.getPlayers()) {
	        if (player.getColor() != PlayerColor.NOT_SELECTED) {
	            for (ColorSlotPanel panel : colorPanels) {
	                if (panel.getColor() == player.getColor()) {
	                    panel.pick(player.getName());
	                }
	            }
	        }
	    }
	}

}
