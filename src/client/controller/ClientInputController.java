package client.controller;

import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;

import client.view.panels.GameView;
import client.view.renderers.CountryRenderer;
import model.AttackReport;
import model.Country;
import model.Game;
import model.Player;
import model.PlayerColor;
import model.enums.AttackResult;
import model.enums.TurnPhase;

public class ClientInputController {

	private final ClientGameController gameController;
	private final ClientLobbyController lobbyController;
	private final GameView gameView;
	private final long playerId;
	
	private ExecutorService networkCommunications = Executors.newCachedThreadPool();
	
	private Runnable onNotEnoughTroops;
	private Runnable onNotBordering;
	private Runnable onCountryNotOwned;
	private Runnable onCannotAttackInMovePhase;
	private Runnable onCannotMoveInAttackPhase;
	private Consumer<AttackReport> onAttackResult;
	private Runnable onFinishedSetupDeploy;

	
	public void setOnNotEnoughTroops(Runnable action) { this.onNotEnoughTroops = action; }
	public void setOnNotBordering(Runnable action) { this.onNotBordering = action; }
	public void setOnCountryNotOwned(Runnable action) { this.onCountryNotOwned = action; }	
	public void setOnCannotAttackInMovePhase(Runnable action) { this.onCannotAttackInMovePhase = action; }
	public void setOnCannotMoveInAttackPhase(Runnable action) { this.onCannotMoveInAttackPhase = action; }
	public void setOnAttackResult(Consumer<AttackReport> onAttackResult) { this.onAttackResult = onAttackResult;}
	public void setOnFinishedSetupDeploy(Runnable onFinishedSetupDeploy) { this.onFinishedSetupDeploy = onFinishedSetupDeploy;}
	
	
	
	public ClientInputController(GameView gameView, long playerId, ClientLobbyController lobbyController, ClientGameController gameController) {
		this.gameView = gameView;
		this.playerId = playerId;
		this.lobbyController = lobbyController;
		this.gameController = gameController;
	}
	
	public void onInput(ClientInput input) {
		if (input.getTargetId() == null) {
			gameView.resetSelection();
			return;
		}
		
		switch (gameView.getWorldPanel().getActionPanel().getSelectedOption()) {
		case "info" -> handleInfo(input, gameView.getSnapshotSupplier().get());
		case "deploy" -> {
			if (!this.gameView.getSnapshotSupplier().get().isCountryOwnedBy(input.getTargetId(), playerId)) {
				gameView.countryNotOwnedPopUp();
		        return;
		    }
			handleDeploy(input, gameView.getSnapshotSupplier().get());
		}
		case "order" -> {
			if (!this.gameView.getSnapshotSupplier().get().isCountryOwnedBy(input.getTargetId(), playerId)) {
//	        	gameView.countryNotOwnedPopUp();
//		        return;
		    }
			handleOrder(input, gameView.getSnapshotSupplier().get());
		}
		}
		
	}
	
	private void executeRequest_old(Runnable request) {
	    gameView.getWorldPanel().setInteractable(false);
	    networkCommunications.execute(() -> {
	        request.run();
	        SwingUtilities.invokeLater(() -> gameView.getWorldPanel().setInteractable(true));
	    });
	}
	
	private void executeRequest(Runnable request) {
	    // schedule cursor change only if request takes longer than 200ms
	    ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
	    ScheduledFuture<?> cursorTask = scheduler.schedule(() -> {
	        SwingUtilities.invokeLater(() -> 
	            gameView.getWorldPanel().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR))
	        );
	    }, 100, TimeUnit.MILLISECONDS);

	    gameView.getWorldPanel().setInteractable(false);

	    networkCommunications.execute(() -> {
	        try {
	            request.run();
	        } finally {
	            cursorTask.cancel(false); // cancel cursor change if request already done
	            scheduler.shutdown();
	            SwingUtilities.invokeLater(() -> {
	                gameView.getWorldPanel().setCursor(Cursor.getDefaultCursor());
	                gameView.getWorldPanel().setInteractable(true);
	            });
	        }
	    });
	}
	
	private void handleInfo(ClientInput input, Game snapshot) {
		gameView.getWorldPanel().infoClick(input.getTargetId());
		updateCountryInfo(input, snapshot);
	}
	
	private void handleDeploy(ClientInput input, Game snapshot) {
		if (!input.isMouse()) {return;}
		int toDeploy = switch (input.getButton()) {
		case MouseEvent.BUTTON1 -> +1; // left click add troops
		case MouseEvent.BUTTON3 -> -1; // right click remove troops
		default -> 0; // do nothing
		};
		
		if (toDeploy == 0) return;
		
	    Country country = snapshot.getWorld().getCountry(input.getTargetId());
	    Player player = snapshot.getPlayer(playerId);
		
	    // client-side validation before optimistic update
	    if (toDeploy > 0 && player.getAvailableTroops() < 1) return; // no troops to deploy
	    if (toDeploy < 0 && country.getTroops() <= 1) return;         // can't withdraw below 1

		
	    executeRequest(() -> {
	        Game updated = gameController.deployTroops(snapshot.getId(), playerId, input.getTargetId(), toDeploy);
	        if (updated != null) {
	        	SwingUtilities.invokeLater(() -> {
	        	gameView.getWorldPanel().deployClick(input.getTargetId());
	            gameView.update();
	            updateCountryInfo(input, updated);
	        	});
	            if (updated.getCurrentPhase() == TurnPhase.SETUP
	            		&& updated.getPlayer(playerId).getAvailableTroops() <= 0) {
	            	onFinishedSetupDeploy.run();
	            }
	        }
	        
	    });

	}
	
	private void handleOrder(ClientInput input, Game snapshot) {
		
		if (input.getTargetId() == null) {
				gameView.resetSelection();
			return;
		}
		
		if (!gameView.getWorldPanel().hasOrderSource()) { // first click
			gameView.getWorldPanel().orderClick(input.getTargetId());
			updateCountryInfo(input, snapshot);
		} else { // second click
			
			String sourceId = gameView.getWorldPanel().getCachedRenderer().getId();
			String targetId = input.getTargetId();
			
			// target is the source
			if (sourceId.equals(targetId)) {
					gameView.resetSelection();
				return;
			}
			
			// minimum troops to have (2)
			if (snapshot.getWorld().getCountry(sourceId).getTroops() < 2) {
				if (onNotEnoughTroops != null) onNotEnoughTroops.run(); 
				gameView.resetSelection();
				return;
			}
			
			// target country is not in the borders list of the source country
			if (!snapshot.getWorld().getCountry(sourceId).getBorders().contains(targetId)) {
				if (onNotBordering != null) onNotBordering.run();
				gameView.resetSelection();
				return;
			}
			
			if (confirmOrder(sourceId, targetId, snapshot)) {
				// success
				gameView.getWorldPanel().orderClick(targetId);
			} else {
				// unsuccess
				gameView.resetSelection();		
			}

		}
	}
	
	private boolean confirmOrder(String sourceId, String targetId, Game snapshot) {
		System.out.println("[confirmOrder] called: " + sourceId + " -> " + targetId);
		Country source = snapshot.getWorld().getCountry(sourceId);
		Country target = snapshot.getWorld().getCountry(targetId);
		CountryRenderer sourceRenderer = this.gameView.getWorldPanel().getCountryRenderer(sourceId);
		CountryRenderer targetRenderer = this.gameView.getWorldPanel().getCountryRenderer(targetId);
		Player player = snapshot.getPlayer(this.playerId);
		boolean isEnemy = target.getOwner() != player.getColor();
		String action = isEnemy ? "Attack" : "Move";
		
		if (isEnemy && snapshot.getCurrentPhase() == TurnPhase.MOVE) {
			if (onCannotAttackInMovePhase != null) onCannotAttackInMovePhase.run();
			gameView.resetSelection();
			return false;
		}
		
		if (!isEnemy && snapshot.getCurrentPhase() == TurnPhase.ATTACK) {
			if (onCannotMoveInAttackPhase != null) onCannotMoveInAttackPhase.run();
			gameView.resetSelection();
			return false;
		}
		
		int usableTroops = source.getTroops() -1;
		
	    JSlider slider = new JSlider(1, usableTroops, 1);
	    slider.setMajorTickSpacing(Math.max(1, usableTroops / 5));
	    slider.setMinorTickSpacing(1);
	    slider.setPaintTicks(true);
	    slider.setPaintLabels(true);
	    slider.setSnapToTicks(true);

	    JLabel troopsLabel = new JLabel("Troops: 1");
	    slider.addChangeListener(e -> {
	        troopsLabel.setText("Troops: " + slider.getValue());
	    });
	    
	    Object[] message = {
	            action + " from " + sourceRenderer.getName()
	                   + " to " + targetRenderer.getName(),
	            troopsLabel,
	            slider
	        };
		
	    // TODO: better graphics for this. maybe custom class
	    int sliderOption = JOptionPane.showConfirmDialog(
	            gameView,
	            message,
	            action,
	            JOptionPane.OK_CANCEL_OPTION,
	            JOptionPane.QUESTION_MESSAGE
	        );
	    
	    if (sliderOption != JOptionPane.OK_OPTION) {
	    	gameView.resetSelection();
	    	return false;
	    }
	    
	    int troops = slider.getValue();
	    int sourceTroopsBefore = source.getTroops();
	    int targetTroopsBefore = target.getTroops();
	    PlayerColor attackerColor = snapshot.getPlayer(playerId).getColor();
	    
	    executeRequest(() -> {
	        Game updated;
	        if (isEnemy) {
	            updated = gameController.attack(snapshot.getId(), playerId, sourceId, targetId, troops);
	            if (updated != null && onAttackResult != null) {
	                Country sourceAfter = updated.getWorld().getCountry(sourceId);
	                Country targetAfter = updated.getWorld().getCountry(targetId);
	                
	                boolean conquered = targetAfter.getOwner() == attackerColor;

	                int attackerLosses;
	                int defenderLosses;
	                int attackerRemaining;
	                int defenderRemaining;

	                if (conquered) {
	                    int troopsMovedToTarget = targetAfter.getTroops();
	                    int totalLeft = sourceTroopsBefore - sourceAfter.getTroops();
	                    attackerLosses = totalLeft - troopsMovedToTarget;
	                    defenderLosses = targetTroopsBefore;
	                    attackerRemaining = targetAfter.getTroops(); // only the ones who pushed through
	                    defenderRemaining = 0;
	                } else {
	                    attackerLosses = sourceTroopsBefore - sourceAfter.getTroops();
	                    defenderLosses = targetTroopsBefore - targetAfter.getTroops();
	                    attackerRemaining = sourceAfter.getTroops();
	                    defenderRemaining = targetAfter.getTroops();
	                }

	                AttackResult result = conquered ? AttackResult.VICTORY :
	                    attackerLosses > defenderLosses ? AttackResult.DEFEAT :
	                    attackerLosses < defenderLosses ? AttackResult.ADVANTAGE : AttackResult.TIE;

	                AttackReport report = new AttackReport(result, attackerLosses, defenderLosses,
	                                                       attackerRemaining, defenderRemaining);
	                onAttackResult.accept(report);
	            }
	        } else {
	            updated = gameController.move(snapshot.getId(), playerId, sourceId, targetId, troops);
	            updated = gameController.nextPhase(snapshot.getId());
	        }
	        
	        Game finalUpdated = updated; // final reference for lambda
	        if (finalUpdated != null) SwingUtilities.invokeLater(() -> {
	            gameView.update();
	            gameView.getGameInfoPanel().getCountryInfoPanel()
	                .update(finalUpdated.getWorld().getCountry(targetId));
	        });
	    });

	    return true;
	    
	}
	
	private void updateCountryInfo(ClientInput input, Game snapshot) {
		gameView.getGameInfoPanel().getCountryInfoPanel().update(snapshot.getWorld().getCountry(input.getTargetId())); // update countryInfoPanel
	}
	
	
	
}
