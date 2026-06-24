package model.network;

import model.*;
import server.controller.ServerBaseController;
import server.controller.ServerLobbyController;

import java.io.Serializable;
import java.time.chrono.IsoChronology;

public class LobbyRequest extends BaseRequest implements Request<ServerLobbyController>, Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private long id;
	private String code;
	private Player player;
	
	// kinda overloading
	public LobbyRequest(RequestInstruction instruction, long id, Player player) {
		super(instruction);
		this.id = id;
		this.player = player;
	}
	
	// overloading
	public LobbyRequest(RequestInstruction instruction, String code, Player player) {
		super(instruction);
		this.code = code;
		this.player = player;
	}

	// overloading
	public LobbyRequest(RequestInstruction instruction, Player player) {
		super(instruction);
		this.player = player;
	}
	
	// overloading
	public LobbyRequest(RequestInstruction instruction) {
		super(instruction);
	}
	
	@Override
	public void handle(ServerBaseController controller) {
		handle((ServerLobbyController) controller);
	}
	
	@Override
	public void handle(ServerLobbyController serverLobbyController) {
		switch (this.instruction) {
			case LOBBY_ADD_PLAYER -> {
				if (this.code == null) {
					serverLobbyController.addPlayer(id, player); // this is more for internal management
				} else {
					serverLobbyController.addPlayer(code, player); // this is used by the user
				}
			}
			case LOBBY_REMOVE_PLAYER -> serverLobbyController.removePlayer(id, player);
			case LOBBY_READY_PLAYER -> serverLobbyController.togglePlayerReady(id, player);
			case LOBBY_CREATE -> serverLobbyController.createLobby();
			case LOBBY_DISMANTLE -> serverLobbyController.dismantleLobby(id);
			case LOBBY_UPDATE_PLAYER -> serverLobbyController.updatePlayer(id, player);
			case LOBBY_ADD_SPECTATOR -> {
				if (this.code == null) {
					serverLobbyController.addSpectator(id, player); // this is more for internal management
				} else {
					serverLobbyController.addSpectator(code, player); // this is used by the user
				}
			}
			case LOBBY_REMOVE_SPECTATOR -> serverLobbyController.removeSpectator(id, player);
			case LOBBY_ADD_BOT -> serverLobbyController.addBot(id, player);
			case LOBBY_REMOVE_BOT -> serverLobbyController.removeBot(id);
			//  ################## [DEV] [REMOVE] ##########################
			case DEV_CHANGE_LOBBY_CODE -> serverLobbyController.changeLobbyCode(id, player.getName());
			}
		}
		
	
	
	public String toString() {
		return "[LOBBY REQUEST] instruction: " + this.instruction + "\nplayer: " + player;
//		String result = "[lobby request]  |  "
//					  + "instruction : " + this.instruction + "  |  "
//					  + "code: " + this.code + "  |  "
//					  + "player: " + this.player;
//		return result;
	}
	
}


