package client.controller;

import java.io.IOException;

import client.network.ClientNetwork;

import model.*;
import model.network.*;

public class ClientLobbyController extends ClientBaseController{

	private long currentLobbyId;
	private String currentLobbyCode; // [DEPRECATED] -> use currentLobbyId instead
	
	public ClientLobbyController(ClientNetwork clientNetwork) {
		super(clientNetwork);
	}
	
	@Override
	public Payload sendRequestAndWait(RequestInstruction instruction) {
		try {
			LobbyRequest request = new LobbyRequest(instruction);
			Payload payload = new Payload(Payloads.LOBBY_REQUEST, request);
			return this.clientNetwork.sendAndWait(payload);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	// overloading
	public Payload sendRequestAndWait(RequestInstruction instruction, long id, Player player) {
		try {
			LobbyRequest request = new LobbyRequest(instruction, id, player);
			Payload payload = new Payload(Payloads.LOBBY_REQUEST, request);
			return this.clientNetwork.sendAndWait(payload);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	// [CRUCIAL] overloading
		public Payload sendRequestAndWait(RequestInstruction instruction, String code, Player player) {
			try {
				LobbyRequest request = new LobbyRequest(instruction, code, player);
				Payload payload = new Payload(Payloads.LOBBY_REQUEST, request);
				return this.clientNetwork.sendAndWait(payload);
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
	
	// [DEPRECATED]
	// overloading
//	public Payload sendRequestAndWait(RequestInstruction instruction, String code, Player player) {
//		try {
//			LobbyRequest request = new LobbyRequest(instruction, code, player);
//			Payload payload = new Payload(Payloads.LOBBY_REQUEST, request);
//			return this.clientNetwork.sendAndWait(payload);
//		} catch (Exception e) {
//			e.printStackTrace();
//			return null;
//		}
//	}

	
	
	
	
	
	
	
	/**
	 * create lobby request
	 * @return
	 */
	public long createLobby() {
		Payload response = sendRequestAndWait(RequestInstruction.LOBBY_CREATE);
		if (isResponseValid(response)) {
			return ((Lobby)response.getData()).getId();
		} throw new NullPointerException("impossible to create lobby");
	}
	
	
	
	
	
	
	/**
	 * join lobby request
	 * @param code
	 * @param player
	 * @return
	 */
	public boolean joinLobby(long id, Player player) {
		Payload response = sendRequestAndWait(RequestInstruction.LOBBY_ADD_PLAYER, id, player);
		if (isResponseValid(response)) {
			setCurrentLobbyId(id);
			return true;
		} return false; 
//		throw new NullPointerException("impossible to join lobby");
	}
	
	// [CRUCIAL] overloading
	public boolean joinLobby(String code, Player player) {
		Payload response = sendRequestAndWait(RequestInstruction.LOBBY_ADD_PLAYER, code, player);
		if (isResponseValid(response)) {
			setCurrentLobbyId(((Lobby)response.getData()).getId());
			return true;
		} return false; 
//		throw new NullPointerException("impossible to join lobby");
	}
	
	
	
	
	
	/**
	 * leave lobby request
	 * @param player
	 */
	public void leaveLobby(Player player) {
		sendRequestAndWait(RequestInstruction.LOBBY_REMOVE_PLAYER, getCurrentLobbyId(), player);
	}
	
	
	
	
	
	/**
	 * dismantle lobby request
	 */
	public void dismantleLobby() {
		sendRequestAndWait(RequestInstruction.LOBBY_DISMANTLE, getCurrentLobbyId(), null);
	}
	
	
	
	
	
	
	
	/**
	 * toggle ready status request
	 * @param player
	 */
	public void toggleReady(Player player) {
		sendRequestAndWait(RequestInstruction.LOBBY_READY_PLAYER, getCurrentLobbyId(), player);
	}
	
	
	
	
	
	
	/**
	 * update player object request
	 * @param player
	 */
	public void updatePlayer(Player player) {
		sendRequestAndWait(RequestInstruction.LOBBY_UPDATE_PLAYER, getCurrentLobbyId(), player);
	}
	
	

	
	
	
	
	
	
	
	
	
	
	/**
	 * join lobby as spectator
	 * @param id
	 * @param player
	 * @return
	 */
	public boolean joinLobbyAsSpectator(long id, Player player) {
		Payload response = sendRequestAndWait(RequestInstruction.LOBBY_ADD_SPECTATOR, id, player);
		if (isResponseValid(response)) {
			setCurrentLobbyId(id);
			return true;
		} throw new NullPointerException("impossible to join lobby");
	}
	
	// [CRUCIAL] overloading
	public boolean joinLobbyAsSpectator(String code, Player player) {
		Payload response = sendRequestAndWait(RequestInstruction.LOBBY_ADD_SPECTATOR, code, player);
		if (isResponseValid(response)) {
			setCurrentLobbyId(((Lobby)response.getData()).getId());
			return true;
		} throw new NullPointerException("impossible to join lobby as spectator");
	}
	
	
	
	
	
	
	
	
	
	
	
	
	/**
	 * leave lobby as spectator request
	 * @param player
	 */
	public void leaveLobbyAsSpectator(Player player) {
		sendRequestAndWait(RequestInstruction.LOBBY_REMOVE_SPECTATOR, getCurrentLobbyId(), player);
	}
	
	
	
	
	
	
	
	
	
	
	public void addBot() {
		
		// fetch and increment id from server network
		SimpleRequest request = new SimpleRequest(RequestInstruction.GET_AND_INCREMENT_ID);
		Payload payload = new Payload(Payloads.SIMPLE_REQUEST, request);
		Payload idResponse = null;
		try {
			idResponse = this.clientNetwork.sendAndWait(payload);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (idResponse == null) {
			return;
		}
		
		
		// create player object
		long id = (long) idResponse.getData();
		Player player = new Player("BOT", null);
		player.setId(id);
		player.setBot(true);
		
		// pass player object 
		Payload response = sendRequestAndWait(RequestInstruction.LOBBY_ADD_BOT, getCurrentLobbyId(), player);
	}
	
	
	public void removeBot() {
		Payload response = sendRequestAndWait(RequestInstruction.LOBBY_REMOVE_BOT, getCurrentLobbyId(), null);
	}
	
	
	public long getCurrentLobbyId() {
		return this.currentLobbyId;
	}
	
	public void setCurrentLobbyId(long id) {
		this.currentLobbyId = id;
	}
	
	
	// #### DEPRECATED ####################################
	
//	public void sendRequest(RequestInstruction instruction, String code, Player player) {
//		try {
//			LobbyRequest request = new LobbyRequest(instruction, code, player);
//			Payload payload = new Payload(Payloads.LOBBY_REQUEST, request);
//			this.clientNetwork.send(payload);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}
	
	
	// [DEPRECATED]
//	public String getCurrentLobbyCode() {
//		return this.currentLobbyCode;
//	}
	
	
	
	// #### DEV COMMANDS ##################################
	
	public void changeLobbyCode(long id, Player player) {
		sendRequestAndWait(RequestInstruction.DEV_CHANGE_LOBBY_CODE, id, player);
	}
	
}
