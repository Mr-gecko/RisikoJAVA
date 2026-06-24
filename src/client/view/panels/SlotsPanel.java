package client.view.panels;

import javax.swing.JPanel;
import java.awt.GridLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;
import java.awt.Color;
import java.awt.Component;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.border.TitledBorder;

import model.Lobby;
import model.Player;
import model.Slot;
import model.SlotStatus;

public class SlotsPanel extends JPanel {
	
	private TitledBorder titleBorder;
	private ArrayList<SlotPanel> slotPanels;
	
	private SlotPanel slotPanel1;
	private SlotPanel slotPanel2;
	private SlotPanel slotPanel3;
	private SlotPanel slotPanel4;
	private SlotPanel slotPanel5;
	private SlotPanel slotPanel6;
	
	private List<SlotPanel> slotsPanels;

	public SlotsPanel() {
		this.titleBorder = new TitledBorder(null, "Lobby Code: ######", TitledBorder.LEADING, TitledBorder.TOP, null, null);
		setBorder(titleBorder);
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		this.slotPanels = new ArrayList<>();
		
		slotPanel1 = new SlotPanel();
		add(slotPanel1);
		this.slotPanels.add(slotPanel1);
		
		slotPanel2 = new SlotPanel();
		add(slotPanel2);
		this.slotPanels.add(slotPanel2);		
		
		slotPanel3 = new SlotPanel();
		add(slotPanel3);
		this.slotPanels.add(slotPanel3);
		
		slotPanel4 = new SlotPanel();
		add(slotPanel4);
		this.slotPanels.add(slotPanel4);
		
		slotPanel5 = new SlotPanel();
		add(slotPanel5);
		this.slotPanels.add(slotPanel5);
		
		slotPanel6 = new SlotPanel();
		add(slotPanel6);
		this.slotPanels.add(slotPanel6);
		
		this.slotPanels = new ArrayList<>();
		this.slotPanels.add(slotPanel1);
		this.slotPanels.add(slotPanel2);
		this.slotPanels.add(slotPanel3);
		this.slotPanels.add(slotPanel4);
		this.slotPanels.add(slotPanel5);
		this.slotPanels.add(slotPanel6);

	}
	
	public void updateSlotsToTurnOrder(List<Player> turnOrder) {
		this.slotPanels.stream().forEach(SlotPanel::reset);
		IntStream.range(0, Math.min(slotPanels.size(), turnOrder.size()))
        .forEach(i -> {
        	SlotPanel slotPanel = slotPanels.get(i);
        	Player player = turnOrder.get(i);
        	Color color = (player.getColor() != null) ? player.getColor().getColor() : new Color(155, 155, 155);
        	slotPanel.setPlayerColor(color);
        	slotPanel.setName(player.getName());
        	slotPanel.setReadyStatus(SlotStatus.EMPTY);
        });
		
	}
	
	public void updateTitleBorder(String title) {
		titleBorder.setTitle(title);
		repaint();
//		setBorder(titleBorder);
	}
	
	public void updateSlots(Lobby lobby) {
		Slot[] slots = Arrays.stream(lobby.getSlots())
				// sort empty slots, filled slots on top
		        .sorted((a, b) -> Boolean.compare(a.isEmpty(), b.isEmpty()))
		        .toArray(Slot[]::new);
		    
		    for (int i = 0; i < slots.length; i++) {
		        SlotPanel slotPanel = this.slotPanels.get(i);
		        if (!slots[i].isEmpty()) {
		            Player p = slots[i].getPlayer();
		            Color color = (p.getColor() != null) ? p.getColor().getColor() : new Color(155, 155, 155);
		            slotPanel.setPlayerColor(color);
		            slotPanel.setPlayerName(p.getName());
		            slotPanel.setReadyStatus(slots[i].getStatus());
		        } else {
		            slotPanel.reset();
		        }
		    }
		    revalidate();
	}

	
	public void update(Lobby lobby) {
		if (lobby == null) return;
		titleBorder.setTitle("Lobby Code: " + lobby.getCode());
		updateSlots(lobby);
		repaint();
	}
	
	public void reset() {
		this.titleBorder.setTitle("Lobby Code: ######");
		slotPanels.forEach(SlotPanel::reset);
		repaint();
	}

}
