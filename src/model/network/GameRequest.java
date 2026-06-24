package model.network;

import java.io.Serializable;

import server.controller.ServerBaseController;
import server.controller.ServerGameController;
import server.controller.ServerLobbyController;

import model.*;
import model.network.*;

public class GameRequest extends BaseRequest implements Request<ServerGameController>, Serializable{

	private static final long serialVersionUID = 1L;	
	
	private String code;
	private long id;
	private long playerId;
	private Player player;
	private String countryId;
	private String secondCountryId;
	private int troops;
	
	public GameRequest(RequestInstruction instruction) {
		super(instruction);
	}
	
	public GameRequest(RequestInstruction instruction, long id) {
		super(instruction);
		this.id = id;
	}
	
	public GameRequest(RequestInstruction instruction, long id, long playerId) {
		super(instruction);
		this.id = id;
		this.playerId = playerId;
	}
	
	public GameRequest(RequestInstruction instruction, long id, long playerId, Player player) {
		super(instruction);
		this.id = id;
		this.playerId = playerId;
		this.player = player;
//		this.code = null;
	}
	
	public GameRequest(RequestInstruction instruction, long id, Player player) {
		super(instruction);
		this.id = id;
		this.player = player;
//		this.code = null;
	}
	
	public GameRequest(RequestInstruction instruction, String code, Player player) {
		super(instruction);
		this.code = code;
		this.player = player;
//		this.id = 0;
	}
	
	public GameRequest(RequestInstruction instruction, long id, long playerId, String countryId, int troops) {
		super(instruction);
		this.id = id;
		this.playerId = playerId;
		this.countryId = countryId;
		this.troops = troops;
	}
	
	public GameRequest(RequestInstruction instruction, long id, long playerId, String countryId, String secondCountryId, int troops) {
		super(instruction);
		this.id = id;
		this.playerId = playerId;
		this.countryId = countryId;
		this.secondCountryId = secondCountryId;
		this.troops = troops;
	}
	
	@Override
	public void handle(ServerBaseController controller) {
		handle((ServerGameController) controller);
	} // need to override
	
	@Override
	public void handle(ServerGameController ctx) {
		switch (this.instruction) {
		case GAME_CREATE -> ctx.createGame(id);
		case GAME_DISMANTLE -> ctx.dismantleGame(id);
		case GAME_SNAPSHOT -> ctx.getGameSnapshot(id);
		case GAME_DEPLOY_TROOPS -> ctx.deployTroops(id, playerId, countryId, troops);
		case GAME_START -> ctx.startGame(id);
		case GAME_ATTACK -> ctx.attack(id, playerId, countryId, secondCountryId, troops);
		case GAME_MOVE_TROOPS -> ctx.move(id, playerId, countryId, secondCountryId, troops);
		case GAME_TRADE_CARDS -> ctx.tradeCards(id, playerId);
		case GAME_PICK_CARD -> ctx.pickCard(id, playerId);
		case GAME_NEXT_PHASE -> ctx.nextPhase(id);
		case GAME_REPLACE_WITH_BOT -> ctx.replaceWithBot(id, playerId, player);
		}
	}
	
	
	public String toString() {
		return "[GAME REQUEST] instruction: " + this.instruction;
//		String result = "Lobby Request";
//		result += " | instruction : " + this.instruction;
//		if (code != null) {
//			result += " | code: " + this.code;
//		}
//		if (id != 0) {
//			result += " | id: " + this.id;
//		}
//		result += " | player: " + this.player;
//		return result;
	}


}
