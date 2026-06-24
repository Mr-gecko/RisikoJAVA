package server.controller;

import java.net.Authenticator.RequestorType;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import model.*;
import model.network.*;
import model.utils.Logger;
import server.model.ServerLobbyModel;

public class ServerLobbyController extends ServerBaseController<ServerLobbyController> implements Runnable {

	private final ServerLobbyModel model;
			
	private Lobby cachedLobby;
	
	private Logger logger = new Logger("SLC");
	
	// #### CONSTRUCTOR ###################################
	
	public ServerLobbyController(ServerLobbyModel model) {
		this.model = model;
		logger.log("initiated");
	}
	
	// #### METHODS #######################################
	
	public void submitRequest(Request<ServerLobbyController> request) {
		this.cachedLobby = null; // (cachedLobby != null) is used as success check
		this.requestHandledSuccessfully = false; // check if request has been successfully executed
		this.requests.offer(request);
		logger.log("request submitted");
	}
	
	public Payload buildResponse(BaseRequest request) {
		request = (LobbyRequest)request;
		RequestInstruction instruction = request.getInstruction();
		// payload type | updated lobby | requestHandledSuccessfully
		logger.log("about to build response");
		boolean handled = this.requestHandledSuccessfully;
		switch(instruction) {
		case LOBBY_ADD_PLAYER -> {return new Payload(Payloads.LOBBY_RESPONSE, this.cachedLobby, handled);}
		case LOBBY_REMOVE_PLAYER -> { return new Payload(Payloads.LOBBY_RESPONSE, this.cachedLobby, handled);}
		case LOBBY_READY_PLAYER -> { return new Payload(Payloads.LOBBY_RESPONSE, this.cachedLobby, handled);}
		case LOBBY_CREATE -> { return new Payload(Payloads.LOBBY_RESPONSE, this.cachedLobby, handled);}
		case LOBBY_DISMANTLE -> { return new Payload(Payloads.LOBBY_RESPONSE, this.cachedLobby, handled);}
		case LOBBY_UPDATE_PLAYER -> { return new Payload(Payloads.LOBBY_RESPONSE, this.cachedLobby, handled);}
		case LOBBY_ADD_SPECTATOR -> { return new Payload(Payloads.LOBBY_RESPONSE, this.cachedLobby, handled);}
		case LOBBY_REMOVE_SPECTATOR -> { return new Payload(Payloads.LOBBY_RESPONSE, this.cachedLobby, handled);}
		case LOBBY_ADD_BOT -> { return new Payload(Payloads.LOBBY_RESPONSE, this.cachedLobby, handled);}
		case LOBBY_REMOVE_BOT -> { return new Payload(Payloads.LOBBY_RESPONSE, this.cachedLobby, handled);}
		// devs
		case DEV_CHANGE_LOBBY_CODE -> { return new Payload(Payloads.LOBBY_RESPONSE, this.cachedLobby, handled);}
		default -> {	
			logger.log("instruction: " + instruction + "; cannot build response");
			return new Payload(Payloads.ERROR, "cannot build response");
			}
		}
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	/**
	 * create lobby
	 */
	public void createLobby() {
		long id = nextIdGetAndIncrement();
		Lobby lobby = model.createLobby(id);
		cacheLobby(lobby);
		markSuccess();
		logger.log("lobby created; code: " +  lobby.getCode());
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	/**
	 * dismantle lobby
	 * @param code
	 */
	public void dismantleLobby(Lobby lobby) {
	    model.dismantleLobby(lobby);
	    cacheLobby(lobby);
		markSuccess();
		logger.log("lobby dismantled: " + lobby);
	}
	
		// overloading
		public void dismantleLobby(String code) {
		    Lobby lobby = model.getLobby(code);
		    dismantleLobby(lobby);
		}
		
		// overloading
		public void dismantleLobby(long id) {
		    Lobby lobby = model.getLobby(id);
		    dismantleLobby(lobby);
		}
	
	
	
	
		
		
		
		
		
		
		
		
		
		
		
		
		
		
	
	
	/**
	 * add player to lobby
	 * @param code
	 * @param player
	 */
	public void addPlayer(Lobby lobby, Player player) {
		try {
			
			if (lobby.isFull() || lobby.isLocked()) {
			    cacheLobby(null);
			    markSuccess(false); // sets requestHandledSuccessfully = false
			    return;
			}
			
			model.addPlayer(lobby, player);
			cacheLobby(lobby);
			markSuccess();
			logger.log("player: " + player + "; added to lobby: " + lobby);
		} catch (Exception e) {
			markSuccess(false);
		}
	}
	
		// overloading
		public void addPlayer(String code, Player player) {
			Lobby lobby = model.getLobby(code);
			addPlayer(lobby, player);
		}
		
		// overloading
		public void addPlayer(long id, Player player) {
			Lobby lobby = model.getLobby(id);
			addPlayer(lobby, player);
		}
	

		
		
		
		
	
		
	public void addBot(Lobby lobby, Player player) {
		try {
			
			if (lobby.isFull() || lobby.isLocked()) {
			    cacheLobby(lobby);
			    markSuccess(true); // sets requestHandledSuccessfully = false
			    logger.log("lobby full");
			    return;
			}
			player.setColor(lobby.getRandomFreeColor());
			model.addBot(lobby, player);
			cacheLobby(lobby);
			markSuccess();
			logger.log("bot: " + player + "; added to lobby: " + lobby);
		} catch (Exception e) {
			markSuccess(false);
		}
	}
	
		// overloading
		public void addBot(String code, Player player) {
			Lobby lobby = model.getLobby(code);
			addBot(lobby, player);
		}
		
		// overloading
		public void addBot(long id, Player player) {
			Lobby lobby = model.getLobby(id);
			addBot(lobby, player);
		}
		
		
		
	public void removeBot(Lobby lobby) {
		try {
			model.removeBot(lobby);
			cacheLobby(lobby);
			markSuccess();
		} catch (Exception e) {
			e.printStackTrace();
			cacheLobby(lobby);
			markSuccess();
			logger.log("no bot");
		}
	}
	
	public void removeBot(long id) {
		Lobby lobby = model.getLobby(id);
		removeBot(lobby);
	}
	
	
	/**
	 * remove player from lobby
	 * @param code
	 * @param player
	 */
	public void removePlayer(Lobby lobby, Player player) {
		try {
			model.removePlayer(lobby, player);
			cacheLobby(lobby);
			markSuccess();
			logger.log("player: " + player + "; removed from lobby: " + lobby);
		} catch (Exception e) {
			markSuccess(false);
		}
	}
	
		// overloading
		public void removePlayer(String code, Player player) {
			Lobby lobby = model.getLobby(code);
			removePlayer(lobby, player);
		}
		
		// overloading
		public void removePlayer(long id, Player player) {
			Lobby lobby = model.getLobby(id);
			removePlayer(lobby, player);
		}

	
	
	
	
		
		
		
		
		
		
		
		
		
	
	

	/** 
	 * [NOT IMPLEMENTED CORRECTLY](no lobby object to cache -> cannot send proper response to sender) you don't get to send a response to the player
	 * remove player no matter the lobby
	 * @param player
	 */
//	public void removePlayerAbsolute(Player player) {
//		model.removePlayerAbsolute(player);
//		markSuccess();
//	}
	
		// overloading
		public void removePlayerAbsolute(long id) {
			Lobby lobby = model.removePlayerAbsolute(id);
			cacheLobby(lobby);
			markSuccess();
		}
		
		
		
	public Player getPlayerAbsolute(long id) {
		return model.getPlayerAbsolute(id);
	}
	
	
	
		
		
		
		
		
		
		
		
		
	
	/**
	 * [NOT IMPLEMENTED CORRECLTY] lobby get deleted immediately after -> cannot cache and send proper response
	 * @param code
	 */
	public void removeAll(Lobby lobby) {
		model.removeAll(lobby);
		cacheLobby(lobby);
		markSuccess();
		logger.log("removed all players from lobby: " + lobby);
	}
	
		// overloading
		public void removeAll(long id) {
			Lobby lobby = model.getLobby(id);
			removeAll(lobby);
		}
	
	
	
		
		
		
		
		
		
		
		
		
		
		
	
	
	/**
	 * set player status
	 * @param code
	 * @param player
	 * @param status
	 */
	public void setPlayerStatus(Lobby lobby, Player player, SlotStatus status){
		model.setPlayerStatus(lobby, player, status);
		cacheLobby(lobby);
		markSuccess();
		logger.log("player: " + player + "; status set to: " + status + "; in lobby: " + lobby);
	}
	
		// overloading
		public void setPlayerStatus(String code, Player player, SlotStatus status) {
			Lobby lobby = model.getLobby(code);
			setPlayerStatus(lobby, player, status);
		}
		
	
	
	
		
		
		
		
		
		
		
		
		
		
		
	
	/**
	 * [NOT USEFUL/USED] get status after getting lobby
	 * return player slot status
	 * @param code
	 * @param player
	 * @return
	 */
	public SlotStatus getPlayerStatus(Lobby lobby, Player player) {
		SlotStatus status = model.getPlayerStatus(lobby, player);
		markSuccess();
		return status;
	}
	
		// overloading
		public SlotStatus getPlayerStatus(String code, Player player) {
			Lobby lobby = model.getLobby(code);
			return getPlayerStatus(lobby, player);
		}
	
	
	
		
		
		
		
		
		
		
		
		
	
	/**
	 * switch between ready and connected for player status
	 * @param code
	 * @param player
	 */
	public void togglePlayerReady(Lobby lobby, Player player) {
		model.togglePlayerReadyStatus(lobby, player);
		cacheLobby(lobby);
		markSuccess();
		logger.log("player: " + player + "; status toggled ready/connected in lobby: " + lobby);
	}
	
		// overloading
		public void togglePlayerReady(String code, Player player) {
			Lobby lobby = model.getLobby(code);
			togglePlayerReady(lobby, player);
		}
	
		// overloading
		public void togglePlayerReady(long id, Player player) {
			Lobby lobby = model.getLobby(id);
			togglePlayerReady(lobby, player);
		}
	
	
		
		
		
		
		
		
		
		
		
	
	
	
	
	/**
	 * update player object in the model
	 * used to update color: cannot send into request the PlayerColor, so
	 * updates the player object and pass it to be updated
	 * @param code
	 * @param player
	 */
	public void updatePlayer(Lobby lobby, Player player) {
		model.updatePlayer(lobby, player);
		cacheLobby(lobby);
		markSuccess();
		logger.log("player: " + player.getId() + " updated");
	}
	
		// overloading
		public void updatePlayer(String code, Player player) {
			Lobby lobby = model.getLobby(code);
			updatePlayer(lobby, player);
		}
		
		// overloading
		public void updatePlayer(long id, Player player) {
			Lobby lobby = model.getLobby(id);
			updatePlayer(lobby, player);
		}
	
	
	
	
	
		
		
		
		
		
		
		
		
		
		
		
	/**
	 * add spectator to lobby
	 * @param lobby
	 * @param player
	 */
	public void addSpectator(Lobby lobby, Player player) {
		try {
			model.addSpectator(lobby, player);
			cacheLobby(lobby);
			markSuccess();
			logger.log("spectator: " + player + "; added in lobby: " + lobby);
		} catch (Exception e) {
			markSuccess(false);
		}
	}
	
		// overloading
		public void addSpectator(String code, Player player) {
			Lobby lobby = model.getLobby(code);
			addSpectator(lobby, player);
		}
	
		// overloading
		public void addSpectator(long id, Player player) {
			Lobby lobby = model.getLobby(id);
			addSpectator(lobby, player);
		}
	

	
	
		
		
	/**
	 * remove spectator from lobby
	 * @param lobby
	 * @param player
	 */
	public void removeSpectator(Lobby lobby, Player player) {
		try {
			model.removeSpectator(lobby, player);
			cacheLobby(lobby);
			markSuccess();
			logger.log("spectator: " + player + "; removed from lobby: " + lobby);
		} catch (Exception e) {
			markSuccess(false);
		}
	}
	
		// overloading
		public void removeSpectator(String code, Player player) {
			Lobby lobby = model.getLobby(code);
			removeSpectator(lobby, player);
		}
		
		// overloading
		public void removeSpectator(long id, Player player) {
			Lobby lobby = model.getLobby(id);
			removeSpectator(lobby, player);
		}
		
	
		
	
	
	
	
	
	
	/**
	 * return cached lobby
	 * @return lobby
	 */
	public Lobby getCachedLobby() {
		return this.cachedLobby;
	}
	
	
	
	
	
	/**
	 * return model lobbies map
	 * @return lobbies map HashMap<String, Lobby>
	 */
	public HashMap<Long, Lobby> getLobbies() {
		return model.getLobbies();
	}
	
	
	
	
	public Lobby getLobby(long id) {
	    return model.getLobby(id);
	}
	
	private void cacheLobby(Lobby lobby) {
		this.cachedLobby = lobby;
	}

	
	
	// #### DEV METHODS ######################################
	
	public void changeLobbyCode(Lobby lobby, String custom) {
		lobby.setCode(custom);
		cacheLobby(lobby);
		markSuccess();
	}
	
		// overloading
		public void changeLobbyCode(String code, String custom) {
			Lobby lobby = model.getLobby(code);
			changeLobbyCode(lobby, custom);
		}
		
		// overloading
		public void changeLobbyCode(long id, String custom) {
			Lobby lobby = model.getLobby(id);
			changeLobbyCode(lobby, custom);
		}
	
	

}
