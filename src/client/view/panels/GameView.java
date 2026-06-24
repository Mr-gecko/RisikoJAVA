package client.view.panels;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import java.awt.BorderLayout;
import javax.swing.BoxLayout;
import java.awt.Component;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.swing.Box;
import javax.swing.border.EmptyBorder;

import client.controller.ClientInput;
import model.Card;
import model.Game;
import model.Lobby;
import model.Player;
import model.enums.CardType;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

public class GameView extends JPanel {
	
	
	private Supplier<Game> snapshotSupplier;
	private WorldPanel worldPanel;
	private GameOptionsPanel optionsPanel;
	private SlotsPanel slotsPanel;
	private Component verticalStrut_1;
	private GameInfoPanel gameInfoPanel;
	private Component verticalStrut_2;
	private GameActionsPanel gameActionsPanel;
	
	public GameView(Supplier<Game> snapshotSupplier) throws Exception {
		this.snapshotSupplier = snapshotSupplier;
		setLayout(new BorderLayout(0, 0));
		
		JPanel panel = new JPanel();
		panel.setBorder(new EmptyBorder(20, 20, 40, 20));
		add(panel, BorderLayout.EAST);
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		
		this.optionsPanel = new GameOptionsPanel();
		optionsPanel.setAlignmentX(Component.RIGHT_ALIGNMENT);
		panel.add(optionsPanel);
		
		Component verticalStrut = Box.createVerticalGlue();
		panel.add(verticalStrut);
		
		gameInfoPanel = new GameInfoPanel();
		gameInfoPanel.setAlignmentX(Component.RIGHT_ALIGNMENT);
		panel.add(gameInfoPanel);
		
		verticalStrut_2 = Box.createVerticalGlue();
		panel.add(verticalStrut_2);
		
		gameActionsPanel = new GameActionsPanel();
		gameActionsPanel.setAlignmentX(Component.RIGHT_ALIGNMENT);
		panel.add(gameActionsPanel);
		
		verticalStrut_1 = Box.createVerticalGlue();
		panel.add(verticalStrut_1);
		
		this.slotsPanel = new SlotsPanel();
		slotsPanel.setAlignmentX(Component.RIGHT_ALIGNMENT);
		panel.add(slotsPanel);
		
		
		this.worldPanel = new WorldPanel(this.optionsPanel, this.snapshotSupplier);
		add(worldPanel, BorderLayout.CENTER);

	}
	
	public SlotsPanel getSlotsPanel() {
		return this.slotsPanel;
	}
	
	public WorldPanel getWorldPanel() {
		return this.worldPanel;
	}
	
	public GameOptionsPanel getOptionsPanel() {
		return this.optionsPanel;
	}
	
	public GameInfoPanel getGameInfoPanel() {
		return this.gameInfoPanel;
	}
	
	public JButton getLeaveButton() {
		return this.gameActionsPanel.getLeaveButton();
	}
	
	public JButton getNextPhaseButton() {
		return this.gameActionsPanel.getNextPhaseButton();
	}
	
	public JButton getCardsButton() {
	    return this.gameActionsPanel.getCardsButton();
	}
	
	public JButton getSurrendereButton() {
		return this.gameActionsPanel.getSurrenderButton();
	}
	
	public GameActionsPanel getGameActionPanel() {
		return this.gameActionsPanel;
	}
	
	// #### UPDATES #######################################
	
	public void updateSlots(Lobby lobby) {
		SwingUtilities.invokeLater(() -> {
			this.slotsPanel.updateSlots(lobby);
			repaint();
		});
	}
	
	public void update() {
		if (snapshotSupplier == null || snapshotSupplier.get() == null) return;
		SwingUtilities.invokeLater(() -> {
			this.worldPanel.update();
			this.slotsPanel.update(this.snapshotSupplier.get().getLobby());
			this.optionsPanel.update(this.snapshotSupplier.get().getCurrentPhase());
			repaint();			
		});
	}
	
	public void reset() {
		SwingUtilities.invokeLater(() -> {			
			this.worldPanel.reset();
			this.slotsPanel.reset();
			repaint();
		});
	}
	
	public void updateGameInfo(long localPlayerId) {
	    SwingUtilities.invokeLater(() -> {
	        Game snapshot = this.snapshotSupplier.get();
	        if (snapshot == null) return;
	        Player localPlayer = snapshot.getPlayer(localPlayerId); // local player for troops
	        this.gameInfoPanel.update(snapshot, localPlayer);
	    });
	}
	
	public void resetSelection() {
		SwingUtilities.invokeLater(() -> {
			this.worldPanel.seaClick();
			this.gameInfoPanel.getCountryInfoPanel().clear();			
		});
	}
	
	public void clearSelection() {
		SwingUtilities.invokeLater(() -> {
			this.gameInfoPanel.getCountryInfoPanel().clear();
		});
	}
	
	
	
	// #### GETTERS & SETTERS #############################
	
	public void setSnapshotSupplier(Supplier<Game> supplier) {
		this.snapshotSupplier = supplier;
		this.worldPanel.setSnapshotSupplier(supplier);
	}
	
	public void setOnCountryClicked(Consumer<ClientInput> callback) {
	    this.worldPanel.setOnCountryClicked(callback);
	}
	
	public Supplier<Game> getSnapshotSupplier(){
		return this.snapshotSupplier;
	}
	
	// #### pop-ups #######################################
	
	public void countryNotOwnedPopUp() {
		SwingUtilities.invokeLater(() -> {
			JOptionPane.showMessageDialog(this, "you don't own this country", "info", JOptionPane.INFORMATION_MESSAGE);
		});
	}

	
	public void showCardsDialog(long playerId, Runnable onTrade) {
		Player player = this.snapshotSupplier.get().getPlayer(playerId);
	    List<Card> cards = player.getDeck().getCards();

	    if (cards.isEmpty()) {
	    	SwingUtilities.invokeLater(() -> {
	    		JOptionPane.showMessageDialog(this, "you have no cards", "cards", JOptionPane.INFORMATION_MESSAGE);
	    	});
	        return;
	    }

	    JPanel panel = new JPanel();
	    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

	    // group by type
	    Map<CardType, Long> grouped = cards.stream()
	        .collect(Collectors.groupingBy(Card::getType, Collectors.counting()));

	    for (Map.Entry<CardType, Long> entry : grouped.entrySet()) {
	        JLabel label = new JLabel(entry.getKey() + ": " + entry.getValue());
	        label.setAlignmentX(Component.LEFT_ALIGNMENT);
	        panel.add(label);
	    }

	    panel.add(Box.createVerticalStrut(10));

	    JLabel bonusLabel = new JLabel("trade bonus: " +
	        (player.canTradeCards() ? player.getCardBonus() + " troops" : "no valid set"));
	    bonusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
	    panel.add(bonusLabel);

	    if (player.canTradeCards()) {
	        panel.add(Box.createVerticalStrut(10));
	        JButton tradeButton = new JButton("trade");
	        tradeButton.setAlignmentX(Component.LEFT_ALIGNMENT);
	        tradeButton.addActionListener(e -> {
	            SwingUtilities.getWindowAncestor(tradeButton).dispose(); // close dialog
	            onTrade.run();
	            updateGameInfo(player.getId());
	        });
	        panel.add(tradeButton);
	    }

	    SwingUtilities.invokeLater(() -> {
	    	JOptionPane.showMessageDialog(this, panel, "your cards", JOptionPane.PLAIN_MESSAGE);
	    });
	}
	
	
	// #### SPECTATOR #####################################
	
	public void setSpectatorMode(boolean value) {
		SwingUtilities.invokeLater(() -> {
			optionsPanel.setSpectatorMode(value);
			gameActionsPanel.setSpectatorMode(value);
			gameInfoPanel.setSpectatorMode(value);
			worldPanel.setSpectatorMode(value);
		});
	}

}
