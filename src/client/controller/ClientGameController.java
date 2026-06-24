 package client.controller;

import client.network.ClientNetwork;
import model.*;
import model.network.*;
import model.utils.Logger;

public class ClientGameController extends ClientBaseController {

	private Logger logger = new Logger("CGC");
	
	public ClientGameController(ClientNetwork clientNetwork) {
		super(clientNetwork);
	}

	@Override
	public Payload sendRequestAndWait(RequestInstruction instruction) {
		try {
			GameRequest request = new GameRequest(instruction);
			Payload payload = new Payload(Payloads.GAME_REQUEST, request);
			return this.clientNetwork.sendAndWait(payload);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public Payload sendRequestAndWait(RequestInstruction instruction, long id) {
		try {
			GameRequest request = new GameRequest(instruction, id);
			Payload payload = new Payload(Payloads.GAME_REQUEST, request);
			return this.clientNetwork.sendAndWait(payload);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	
	public Payload sendRequestAndWait(RequestInstruction instruction, long id, long playerId, 
						String sourceId, String targetId, int troops){
		try {
			GameRequest request = new GameRequest(instruction, id, playerId, sourceId, targetId, troops);
			Payload payload = new Payload(Payloads.GAME_REQUEST, request);
			return this.clientNetwork.sendAndWait(payload);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public Payload sendRequestAndWait(RequestInstruction instruction, String code, Player player) {
		try {
			GameRequest request = new GameRequest(instruction, code, player);
			Payload payload = new Payload(Payloads.GAME_REQUEST, request);
			return this.clientNetwork.sendAndWait(payload);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public Payload sendRequestAndWait(RequestInstruction instruction, long id, Player player) {
		try {
			GameRequest request = new GameRequest(instruction, id, player);
			Payload payload = new Payload(Payloads.GAME_REQUEST, request);
			return this.clientNetwork.sendAndWait(payload);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public Payload sendRequestAndWait(RequestInstruction instruction, long id, long playerId) {
		try {
			GameRequest request = new GameRequest(instruction, id, playerId);
			Payload payload = new Payload(Payloads.GAME_REQUEST, request);
			return this.clientNetwork.sendAndWait(payload);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public Payload sendRequestAndWait(RequestInstruction instruction, long id, long playerId, Player player) {
		try {
			GameRequest request = new GameRequest(instruction, id, playerId, player);
			Payload payload = new Payload(Payloads.GAME_REQUEST, request);
			return this.clientNetwork.sendAndWait(payload);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public Payload sendRequestAndWait(GameRequest request) {
		try {
			Payload payload = new Payload(Payloads.GAME_REQUEST, request);
			return this.clientNetwork.sendAndWait(payload);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	
	
	/**
	 * create game request
	 * @param id
	 * @param player
	 * @return
	 */
	public Game createGame(long id, Player player) {
		Payload response = sendRequestAndWait(RequestInstruction.GAME_CREATE, id, player);
		if (isResponseValid(response)) {
			Game game = (Game) response.getData();
			return game;
		} throw new NullPointerException("impossible to create game");
	}
	
	public void dismantleGame(long id) {
		Payload response = sendRequestAndWait(RequestInstruction.GAME_DISMANTLE, id, null);
	}
	
	
//	// [DEPRECATED](maybe) overloading
//	public Game createGame(String code, Player player) {
//		Payload response = sendRequestAndWait(RequestInstruction.GAME_CREATE, code, player);
//		if (isResponseValid(response)) {
//			Game game = (Game) response.getData();
//			return game;
//		} throw new NullPointerException("impossible to create game");
//	}
	
	
	/**
	 * get game snapshot request
	 * @param id
	 * @param player
	 * @return
	 */
	public Game getGameSnapshot(long id, Player player) {
		Payload response = sendRequestAndWait(RequestInstruction.GAME_SNAPSHOT, id, player);
		if (isResponseValid(response)) {
			Game game = (Game) response.getData();
			return game;
		} throw new NullPointerException("impossible to get game snapshot from server");
	}

	
	public Game replaceWithBot(long id, Player player) {
		
		long playerId = player.getId();
		SimpleRequest request = new SimpleRequest(RequestInstruction.GET_AND_INCREMENT_ID);
		Payload payload = new Payload(Payloads.SIMPLE_REQUEST, request);
		Payload idResponse = null;
		try {
			idResponse = this.clientNetwork.sendAndWait(payload);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (idResponse == null) {
			return null;
		}
		
		long idBot = (long)idResponse.getData();
		
		// create player object
		long botId = (long) idResponse.getData();
		Player bot = new Player("BOT", null);
		bot.copy(player);
		bot.setId(idBot);
		bot.setBot(true);
		
		Payload response = sendRequestAndWait(RequestInstruction.GAME_REPLACE_WITH_BOT, id, playerId, bot);
		if (isResponseValid(response)) {
			return (Game) response.getData();
		}
		return null;
	}
	
	
	
	
	
	
	/**
	 * deploy troops request
	 * @param id
	 * @param player
	 * @param countryId
	 * @param troops
	 * @return
	 */
	public Game deployTroops(long id, long playerId, String countryId, int troops) {
		try {
			GameRequest request = new GameRequest(
					RequestInstruction.GAME_DEPLOY_TROOPS,id, playerId, countryId,troops);
			Payload response = sendRequestAndWait(request);
			if (isResponseValid(response)) {
				return (Game) response.getData();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
	
	
	
	
	
	
	
	public Game startGame(long id, Player player) {
		Payload response = sendRequestAndWait(RequestInstruction.GAME_START, id, player);
		if (isResponseValid(response)) {
			Game game = (Game) response.getData();
			return game;
		} throw new NullPointerException("impossible to start game");
	}
	
		
	
	
	
	
	
	
	public Game attack(long id, long playerId, String sourceId, String targetId, int troops) {
		Payload response = sendRequestAndWait(RequestInstruction.GAME_ATTACK, id, playerId, sourceId, targetId, troops);
		if (isResponseValid(response)) {
			Game game = (Game) response.getData();
			return game;
		} throw new NullPointerException("impossible to attack");
	}
	
	public Game move(long id, long playerId, String sourceId, String targetId, int troops) {
		Payload response = sendRequestAndWait(RequestInstruction.GAME_MOVE_TROOPS, id, playerId, sourceId, targetId, troops);
		if (isResponseValid(response)) {
			Game game = (Game) response.getData();
			return game;
		} throw new NullPointerException("impossible to move");
	}
	
	
	public Game nextPhase(long id) {
		Payload response = sendRequestAndWait(RequestInstruction.GAME_NEXT_PHASE, id);
		if (isResponseValid(response)) {
			Game game = (Game) response.getData();
			return game;
		} throw new NullPointerException("impossible to end turn");
	}
	
	
	public Game tradeCards(long id, long playerId) {
		Payload response = sendRequestAndWait(RequestInstruction.GAME_TRADE_CARDS, id, playerId);
		if (isResponseValid(response)) {
			Game game = (Game) response.getData();
			return game;
		} throw new NullPointerException("impossible to trade cards");
	}
	
	
}
