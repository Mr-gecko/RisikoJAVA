package client.controller;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.SwingUtilities;

import client.network.ClientNetwork;
import client.view.MainFrame;
import model.Game;
import model.Lobby;
import model.Player;
import model.PlayerColor;
import model.enums.TurnPhase;
import model.network.BaseRequest;
import model.network.Payload;
import model.network.Payloads;
import model.network.RequestInstruction;
import model.network.SimpleRequest;
import model.utils.Logger;
import server.controller.ServerBaseController;

public class ClientViewController {

    private final MainFrame mainFrame;
    private final ClientNetwork clientNetwork;
    private final ClientLobbyController clientLobbyController;
    private final ClientGameController clientGameController;
    private final Player player;

    private Game gameSnapshot;

    private ExecutorService networkCommunications = Executors.newCachedThreadPool();

    private boolean readySet = false;
    private boolean isStartedHandled = false;
    private boolean areTurnsStarted = false;
    
    private TurnPhase oldPhase;

    Logger logger = new Logger("CVC");

    public ClientViewController(MainFrame mainFrame, ClientNetwork clientNetwork, ClientLobbyController clientLobbyController, ClientGameController clientGameController, Player player) {
        this.mainFrame = mainFrame;
        this.clientNetwork = clientNetwork;
        this.clientLobbyController = clientLobbyController;
        this.clientGameController = clientGameController;
        this.player = player;

        wireView();
    }

    // #### WIRING ########################################

    private void wireView() {
        mainFrame.setSnapshotSupplier(() -> this.gameSnapshot);
        mainFrame.setOnStartGame(this::startGame);
        mainFrame.setOnAddBot(this::addBot);
        mainFrame.setOnRemoveBot(this::removeBot);
        mainFrame.setOnLeaveLobby(this::leaveLobby);
        mainFrame.setOnLeaveLobbyAsSpectator(this::leaveLobbyAsSpectator);
        mainFrame.setOnToggleReady(this::toggleReady);
        mainFrame.setOnLeaveGame(this::leaveGame);
        mainFrame.setOnSurrenderGame(this::surrenderGame);
        mainFrame.setOnLeaveGameAsSpectator(this::leaveGameAsSpectator);
        mainFrame.setOnNextPhase(this::nextPhase);
        mainFrame.setOnColorPicked((slot, color) -> {
            player.setColor(color);
            slot.pick(player.getName());
            networkCommunications.execute(() -> clientLobbyController.updatePlayer(player));
        });
        mainFrame.setOnCardsButton(() -> {
            SwingUtilities.invokeLater(() -> {
                mainFrame.getGameView().showCardsDialog(clientNetwork.getId(), this::onTradeCards);
            });
        });
        
        mainFrame.setOnOptionChanged(() -> {
        	mainFrame.getGameView().resetSelection();
        });
//        mainFrame.setOnTradeCards(() -> {
//        	
//        });
        

        ClientInputController inputController = new ClientInputController(
            mainFrame.getGameView(), clientNetwork.getId(), clientLobbyController, clientGameController
        );
        
        inputController.setOnNotEnoughTroops(mainFrame::showNotEnoughTroops);
        inputController.setOnNotBordering(mainFrame::showNotBordering);
        inputController.setOnCountryNotOwned(mainFrame::showCountryNotOwned);
        inputController.setOnCannotAttackInMovePhase(mainFrame::showCannotAttackInMovePhase);
        inputController.setOnCannotMoveInAttackPhase(mainFrame::showCannotMoveInAttackPhase);
        inputController.setOnAttackResult(mainFrame::showAttackResult);
        inputController.setOnFinishedSetupDeploy(mainFrame::showEndSetup);
        
        mainFrame.setOnCountryClicked(inputController::onInput);
        
    }

    // #### HANDLE PAYLOADS ###############################

    public void handleServerPayload(Payload payload) {
        logger.log("handling server payload");
        switch (payload.getType()) {
        case Payloads.LOBBY_RESPONSE -> handleLobbyResponse(payload);
        case Payloads.GAME_RESPONSE  -> handleGameResponse(payload);
        case Payloads.ERROR          -> mainFrame.showError("something went wrong");
        case Payloads.SERVER_SHUTDOWN -> {
            mainFrame.showMessage("Server has shut down", "Disconnected");
            mainFrame.jumpToStartUpFrame();
        }
        case Payloads.CLIENT_DISCONNECTED -> {
            mainFrame.showMessage("You have been disconnected", "Disconnected");
            mainFrame.jumpToStartUpFrame();
        }
        }
    }

    private void handleLobbyResponse(Payload payload) {
        logger.log("handle lobby response");
        Lobby lobby = (Lobby) payload.getData();
        mainFrame.updateLobby(lobby);
    }

    private void handleGameResponse(Payload payload) {
        logger.log("handle game response");
        Game game = (Game) payload.getData();

        // if player got eliminated
        if (!game.getJustEliminated().isEmpty()) {
            for (Player eliminated : game.getJustEliminated()) {
                if (eliminated.getId() == player.getId()) {
                        mainFrame.showGotEliminatedDialog();
                    	mainFrame.setSpectatorMode(true);
                } else {
                    	mainFrame.showEliminationDialog(eliminated.getName());
                }
            }
            game.clearJustEliminated();
        }
        
        if (game.isEnded()) {
        	logger.log("game ended");
            isStartedHandled = false;
            this.gameSnapshot = null;
            Player winner = game.getWinner();
            SwingUtilities.invokeLater(() -> {
                if (winner != null) {
                    mainFrame.showWinnerDialog(winner); // blocks until OK
                } else {
                    mainFrame.showMessage("game ended", "game over"); // blocks until OK
                }
                mainFrame.showLobbyView(); // only runs after OK is pressed
            });
            return;
        }
        
        if (game.isStarted() && !isStartedHandled) {
        	logger.log("game started");
        	isStartedHandled = true;
        	this.gameSnapshot = game;
        	SwingUtilities.invokeLater(() -> {        		
        		mainFrame.getGameView().reset();
        		mainFrame.showGameView();
        		mainFrame.updateGame();
        		if (!game.getLobby().isSpectator(player.getId())) {
        			mainFrame.showGameStartDialog(game);
        		}
//        		mainFrame.showMessage("game started", "game on");
        	});
        }
        
        // the handling of the creation pass here
        if (!game.isStarted()) {
        	this.gameSnapshot = game;
        	logger.debug("CREATE");
        	return;
        }
        
        // setup phase ended
        if (game.areTurnsStarted() && !areTurnsStarted) {
        	areTurnsStarted = true;
        	mainFrame.getGameView().getSlotsPanel().updateSlotsToTurnOrder(game.getTurnOrder());
        	mainFrame.showTurnOrderDialog(game.getTurnOrder());
        }

        this.gameSnapshot = game;
        Player current = game.getCurrentPlayer();
        logger.debug("current player: " + current);
//        boolean isMyTurn = game.getCurrentPlayer() != null && 
//        				   game.getCurrentPlayer().getId() == clientNetwork.getId();
        
        boolean isMyTurn = game.getCurrentPhase() == TurnPhase.SETUP || 
                (game.getCurrentPlayer() != null && 
                 game.getCurrentPlayer().getId() == clientNetwork.getId());
        
        System.out.println("[CVC] phase: " + game.getCurrentPhase() + " | myTurn: " + isMyTurn);
    	mainFrame.setInteractable(isMyTurn);
    	updateEndTurnButton(game.getCurrentPhase(), isMyTurn);
    	mainFrame.updateGame();
    	updateGameInfoPanel();
    	
    	TurnPhase currentPhase = game.getCurrentPhase();
    	
    	if (currentPhase == TurnPhase.SETUP) {
    		oldPhase = currentPhase;
    		return;
    	}
    	
    	if (currentPhase != oldPhase
    		&& oldPhase != TurnPhase.SETUP) {
//    		mainFrame.showNextPhaseDialog(this.gameSnapshot.getCurrentPhase());
    	}
    	
    	oldPhase = currentPhase;
    }

    // #### ACTIONS #######################################
    
    private void startGame() {
        networkCommunications.execute(() -> {
        	this.gameSnapshot = clientGameController.createGame(clientLobbyController.getCurrentLobbyId(), player);
            this.gameSnapshot = clientGameController.startGame(this.gameSnapshot.getId(), player);
            mainFrame.getGameView().updateGameInfo(clientNetwork.getId());
    		mainFrame.getGameView().resetSelection();
        });
    }
    
    private void addBot() {
    	networkCommunications.execute(() -> {
    		clientLobbyController.addBot();
    	});
    }
    
    private void removeBot() {
    	networkCommunications.execute(() -> {
    		clientLobbyController.removeBot();
    	});
    }

    private void leaveLobby() {
        networkCommunications.execute(() -> {
            clientLobbyController.leaveLobby(player);
            clientNetwork.disconnect();
            mainFrame.jumpToStartUpFrame();
        });
    }
    
    private void leaveLobbyAsSpectator() {
        networkCommunications.execute(() -> {
            clientLobbyController.leaveLobbyAsSpectator(player);
            clientNetwork.disconnect();
            mainFrame.jumpToStartUpFrame();
        });
    }
    
    private void leaveGame() {
    	networkCommunications.execute(() -> {
//    		should become bot
    		clientGameController.replaceWithBot(this.gameSnapshot.getId(), player);
            clientNetwork.disconnect();
    		mainFrame.jumpToStartUpFrame();
    	});
    }
    
    private void surrenderGame() {
    	networkCommunications.execute(() -> {
//    		should become spectator
//    		isStartedHandled = false;
//    		clientGameController.dismantleGame(this.gameSnapshot.getId());
    	});
    }
    
    private void leaveGameAsSpectator() {
    	networkCommunications.execute(() -> {
    		clientLobbyController.leaveLobbyAsSpectator(player);
//    		mainFrame.setSpectatorMode(false);
            clientNetwork.disconnect();
    		mainFrame.jumpToStartUpFrame();
    	});
    }
    
    private void nextPhase() {
    	networkCommunications.execute(() -> {
    		clientGameController.nextPhase(this.gameSnapshot.getId());
    		updateGameInfoPanel();
    		mainFrame.getGameView().resetSelection();
    	});
    }

    private void toggleReady() {
        networkCommunications.execute(() -> {
            if (player.getColor() == PlayerColor.NOT_SELECTED) {
                mainFrame.showError("you must select a color first");
                return;
            }
            clientLobbyController.toggleReady(player);
            mainFrame.updateReadyButton(!readySet);
            readySet = !readySet;
        });
    }
    
    private void onTradeCards() {
        networkCommunications.execute(() -> {
            Game updated = clientGameController.tradeCards(gameSnapshot.getId(), player.getId());
            if (updated != null) {
                this.gameSnapshot = updated;
                mainFrame.updateGame();
                updateGameInfoPanel();
            }
        });
    }
    
    private void updateGameInfoPanel() {
    	mainFrame.getGameView().updateGameInfo(clientNetwork.getId());
    }
    
    private void updateEndTurnButton(TurnPhase phase, boolean isMyTurn) {
        SwingUtilities.invokeLater(() -> {
            switch (phase) {

                case SETUP  -> {
                    mainFrame.getGameView().getNextPhaseButton().setText("End Turn");
                    mainFrame.getGameView().getNextPhaseButton().setEnabled(false);
                }
                case DEPLOY -> {
                    mainFrame.getGameView().getNextPhaseButton().setText("Start Attack");
                    mainFrame.getGameView().getNextPhaseButton().setEnabled(isMyTurn);
                }
                case ATTACK -> {
                    mainFrame.getGameView().getNextPhaseButton().setText("Start Move");
                    mainFrame.getGameView().getNextPhaseButton().setEnabled(isMyTurn);
                }
                case MOVE   -> {
                    mainFrame.getGameView().getNextPhaseButton().setText("End Turn");
                    mainFrame.getGameView().getNextPhaseButton().setEnabled(isMyTurn);
                }
            }
        });

    }


}
