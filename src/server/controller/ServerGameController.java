package server.controller;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;


import model.*;
import model.enums.TurnPhase;
import model.network.*;
import model.utils.Logger;
import server.model.*;

public class ServerGameController extends ServerBaseController<ServerGameController>{

	private final ServerGameModel gameModel;
	private final ServerLobbyModel lobbyModel;
	
	private Game cachedGame;
	
	private Logger logger = new Logger("SGC");
		
	public ServerGameController(ServerGameModel gameModel, ServerLobbyModel lobbyModel) {
		this.gameModel = gameModel;
		this.lobbyModel = lobbyModel;
		logger.log("initiated");
	}

	@Override
	public void submitRequest(Request<ServerGameController> request) {
		this.requests.offer(request);
		this.requestHandledSuccessfully = false;
		logger.log("request submitted");
	}

	@Override
	public Payload buildResponse(BaseRequest request) {
		request = (GameRequest) request;
		logger.log("about to build response");
		// payload type | updated game | requestHandledSuccessfully
		RequestInstruction instruction = request.getInstruction();
		boolean handled = this.requestHandledSuccessfully;
		switch(instruction) {
		case GAME_CREATE -> {return new Payload(Payloads.GAME_RESPONSE, this.cachedGame, this.requestHandledSuccessfully);}
		case GAME_DISMANTLE -> {return new Payload(Payloads.GAME_RESPONSE, this.cachedGame, this.requestHandledSuccessfully);}		
		case GAME_SNAPSHOT -> {return new Payload(Payloads.GAME_RESPONSE, this.cachedGame, this.requestHandledSuccessfully);}
		case GAME_DEPLOY_TROOPS -> {return new Payload(Payloads.GAME_RESPONSE, this.cachedGame, this.requestHandledSuccessfully);}
		case GAME_START -> {return new Payload(Payloads.GAME_RESPONSE, this.cachedGame, this.requestHandledSuccessfully);}
		case GAME_ATTACK -> {return new Payload(Payloads.GAME_RESPONSE, this.cachedGame, this.requestHandledSuccessfully);}
		case GAME_MOVE_TROOPS -> {return new Payload(Payloads.GAME_RESPONSE, this.cachedGame, this.requestHandledSuccessfully);}
		case GAME_TRADE_CARDS -> {return new Payload(Payloads.GAME_RESPONSE, this.cachedGame, this.requestHandledSuccessfully);}
		case GAME_PICK_CARD -> {return new Payload(Payloads.GAME_RESPONSE, this.cachedGame, this.requestHandledSuccessfully);}
		case GAME_NEXT_PHASE -> {return new Payload(Payloads.GAME_RESPONSE, this.cachedGame, this.requestHandledSuccessfully);}
		case GAME_REPLACE_WITH_BOT -> {return new Payload(Payloads.GAME_RESPONSE, this.cachedGame, this.requestHandledSuccessfully);}
		default -> {	
			logger.log("instruction: " + instruction + "; cannot build response");
			return new Payload(Payloads.ERROR, "cannot build response");
			}
		}
	}
	
	
	/**
	 * create game 
	 * @param id of parent lobby
	 */
	public void createGame(long id) {
		try {
		Lobby lobby = lobbyModel.getLobby(id);
		Game game = gameModel.createGame(lobby);
		cacheGame(game);
		markSuccess();
		logger.log("game created: " + game + "lobby: " + lobby);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	
	
	/**
	 * dismantle game
	 * @param id
	 */
	public void dismantleGame(long id) {
		Game game = gameModel.getGame(id);
		game.end();
		gameModel.dismantleGame(id);
		cacheGame(game); // cache this so the response could be broadcasted to the lobby of the game,
						 // even if the game object does not exists on the server model anymore and 
						 // cannot be accessed
		markSuccess();
		logger.log("game dismantled; id: " + id);
	}
	
	
	
	
	
	
	public Game getGameFromPlayerId(long id) {
		return gameModel.getGameFromPlayerId(id);
	}
	
	public Game getGame(long id) {
		return gameModel.getGame(id);
	}
	
	
	
	
	public void getGameSnapshot(long id) {
		try {
			Game game = gameModel.getGame(id);
			cacheGame(game);
			markSuccess();
			logger.log("game snapshot parsed");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	
	
	public void deployTroops(long id, long playerId, String countryId, int troops) {
		try {
			Game game = gameModel.getGame(id);
			Player player = game.getPlayer(playerId);
			
			// only during setup phase
			if (game.getCurrentPhase() == TurnPhase.SETUP) {
		        // during setup: no turn order, anyone can deploy simultaneously
		        if (!game.isCountryOwnedBy(countryId, playerId)) return;
		        if (player.getAvailableTroops() < troops) return;
		        if (troops < 0) return; // no withdrawing during setup
		        game.getWorld().getCountry(countryId).addTroops(troops);
		        player.removeTroopsToDeploy(troops);
		        // if player has no more troops to place, mark ready
		        if (player.getAvailableTroops() == 0) {
		            game.markSetupReady(playerId);
		        }
		        cacheGame(game);
		        markSuccess();
		        return;
		    }
			
			
			if (!game.isPlayerTurn(playerId) || !game.isValidPhase(TurnPhase.DEPLOY)) return; // validation for action
			
		    // withdraw lock logic
		    if (troops < 0) {
		        String lastCountry = game.getDeployedCountry(playerId);
		        int deployedThisTurn = game.getDeployedTroops(playerId);
		        Country country = game.getWorld().getCountry(countryId);
		        
		        if (lastCountry == null || !lastCountry.equals(countryId)) return; // can't withdraw from this country
		        if (deployedThisTurn + troops < 0) return; // can't withdraw more than deployed
		        if (country.getTroops() <= 1) return;
		    }
			
		    
		    // default deploy usage
			game.getWorld().getCountry(countryId).addTroops(troops);
			player.removeTroopsToDeploy(troops);
			game.updateDeployment(playerId, countryId, troops);
			cacheGame(game);
			markSuccess();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public void startGame(long id) {
		try {
			Game game = gameModel.getGame(id);
			
			game.start();
			cacheGame(game);
			markSuccess();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	private void cacheGame(Game game) {
		this.cachedGame = game;
	}
	
	
	
	public void assignTroopsToPlayer(long id, long playerId) {
		Game game = gameModel.getGame(id);
//		game.
	}
	
	
	
	public void attack(long id, long playerId, String sourceId, String targetId, int troops) {
		try {
			Game game = gameModel.getGame(id);
			if (!game.isPlayerTurn(playerId) || !game.isValidPhase(TurnPhase.ATTACK)) return; // validation for action
			Country source = game.getWorld().getCountry(sourceId);
			Country target = game.getWorld().getCountry(targetId);
			Player previousOwner = game.getPlayer(target.getOwner());
			
//			CLASSIC DICE BATTLE PROBABILITY
			int[] losses = BattleProbability.bloodDices(troops, target.getTroops());
//			int[] losses = BattleProbability.rawP(troops, target.getTroops());
			source.removeTroops(losses[0]);
			target.removeTroops(losses[1]);
			
			if (source.getTroops() > 1
			 && target.getTroops() > 1) {
				
			}
			
//			DYNAMIC BATTLE PROBABILITY
//			while(source.getTroops() > 1 && troops > 0 && target.getTroops() > 0) {
//				// random double from 0.0 to 1.0 < P (percentage of winning for each troop)
//				if (ThreadLocalRandom.current().nextDouble() < BattleProbability.dynamicP(troops, target.getTroops())) {
//					// source wins -> target loses
//					target.removeTroops(1);
//				} else {
//					// target wins -> source loses
//					source.removeTroops(1);
//					troops--;
//				}
//			}
//			System.out.println("conquest — attackingTroops remaining: " + troops + " | target troops: " + target.getTroops());
			
			// CONQUEST
			if (target.getTroops() <= 0) {
			    target.setOwner(source.getOwner());
			    
			    int survivedTroops = source.getTroops();
			    if (survivedTroops > 0) {
			        source.removeTroops(survivedTroops -1);
			        target.addTroops(survivedTroops -1);
			        game.setConquered(true); // flag this to pick card in nextPhase inside game
			        
			        
				    if (previousOwner != null && game.getNumberOfCountriesOwnedBy(previousOwner) == 0) {
				        game.eliminatePlayer(previousOwner.getId()); // removes from turnOrder
				        // optionally: transfer eliminated player's cards to the attacker
				    }
			        
			    } else {
			    	logger.debug("THIS SHOULD NOT HAPPEND");
			    }

			    
			}
			
			// check winning condition
			Player winner = game.checkWinCondition();
		    if (winner != null) {
		        game.end(winner);
		    }
		    
		    
			
			cacheGame(game);
			markSuccess();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public void move(long id, long playerId, String sourceId, String targetId, int troops) {
		try {
			Game game = gameModel.getGame(id);
			if (!game.isPlayerTurn(playerId) || !game.isValidPhase(TurnPhase.MOVE)) return; // validation for action
			Country source = game.getWorld().getCountry(sourceId);
			Country target = game.getWorld().getCountry(targetId);
			
			if (source.getTroops() > 1 && source.getTroops() > troops) {
				source.removeTroops(troops);
				target.addTroops(troops);
				cacheGame(game);
				markSuccess();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	
	public void pickCard(long id, long playerId) {
		Game game = gameModel.getGame(id);
		Player player = game.getPlayer(playerId);
		
//		if (game.getCurrentPhase() != TurnPhase.MOVE) return;
//		if (game.getCurrentPlayer().getId() != playerId) return;
		
		Card card = game.getDeck().pick();
		player.getDeck().addCard(card);
		cacheGame(game);
		markSuccess();
	}
	
	public void nextPhase(long id) {
		Game game = gameModel.getGame(id);
		game.nextPhase();
		
		// check for winner
		Player winner = game.checkWinCondition();
	    if (winner != null) game.end(winner);
	    
		cacheGame(game);
		markSuccess();
	}
	
	public void tradeCards(long id, long playerId) {
		Game game = gameModel.getGame(id);
		Player player = game.getPlayer(playerId);
		
//		if (game.getCurrentPhase() != TurnPhase.DEPLOY) return;
//		if (game.getCurrentPlayer().getId() != playerId) return; // block if not correct player turn
		
		int bonus = player.tradeCards();
		if (bonus == 0) return;
		
		player.addTroopsToDeploy(bonus);
		cacheGame(game);
		markSuccess();
	}
	
	public void replaceWithBot(long id, long playerId, Player bot) {
		Game game = gameModel.getGame(id);
		Player player = game.getPlayer(playerId);
		Lobby lobby = game.getLobby();
		lobby.getPlayerSlot(playerId).setPlayer(bot);
		if (game.getTurnOrder() != null) {
			game.replacePlayerInTurnOrder(playerId, bot);
		}
		lobby = lobbyModel.checkForEmptyLobby(lobby);
		cacheGame(game);
		markSuccess();
	}

	
}
