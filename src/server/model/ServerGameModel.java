package server.model;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import model.*;
import server.ServerApp;
import model.data.GameDataLoader;

public class ServerGameModel {
	
	// when a Game object is created, inside of it a it creates a copy object of this GameData object
	private GameData baseGameData;
	
	// TODO: this is accessed by multiple threads, it should be reworked [FIXED](used ConcurrentHashMap)
	private ConcurrentHashMap<Long, Game> games;
	
	
	public ServerGameModel() {
		
		try {
			this.baseGameData = new GameData();
		} catch (IOException e) {
			// error along loading gameData.json
			e.printStackTrace();
		}
		
		this.games = new ConcurrentHashMap<>();
	}
	
	
	
	/**
	 * get games hashmap
	 * @return
	 */
	public ConcurrentHashMap<Long, Game> getGames(){
		return this.games;
	}
	
	
	
	
	/**
	 * return Game object if code match
	 * return null if code doesn't match
	 * @param id
	 * @return
	 */
	public Game getGame(long id) {
		return this.games.get(id);
	}
	
	
	
	
	public Game getGameFromPlayerId(long playerId) {
	    return games.values().stream()
	        .filter(game -> game.getLobby().getPlayerSlot(playerId) != null)
	        .findFirst()
	        .orElse(null);
	}
	
	
	
	
	/**
	 * add game to games map
	 * @param game
	 */
	public void addGame(Game game) {
		long id = game.getId();
		this.games.put(id, game);
	}
	
	
	
	
	/**
	 *  remove game from games map
	 * @param game
	 */
	public void removeGame(Game game) {
		long id = game.getId();
		this.games.remove(id);
	}
	
	
	
	
	
	/**
	 * create a new game object and add it to the map
	 * @param id (corresponding lobby id)
	 * @return
	 */
	public Game createGame(Lobby lobby) {
		Game game = new Game(lobby);
		game.loadGameData(new GameData(baseGameData));
		game.deckSetup();
		addGame(game);
		return game;
	}
	
	
	
	
	/**
	 * dismantle game
	 * @param id
	 */
	public void dismantleGame(long id) {
		Game game = getGame(id);
		removeGame(game);
	}
	
	
	
	
	/**
	 * start game
	 * @param id
	 */
	public void startGame(long id) {
		Game game = getGame(id);
		game.start();
	}
	
	
	
	
	
	public void printInfo() {
		System.out.println("[SGM] info print");
		System.out.println("games number: " + this.games.size());
		if (this.games.size() > 0) {
			System.out.println("[+] active games");
			this.games.values().forEach(game -> {
				System.out.println(" |-" + game);
			});
//			printWorldTroops();
		}
	}
	
	
	
	public void printWorldTroops() {
		if (this.games.size() > 0) {
			this.games.values().forEach(game -> {
				System.out.println("GAME TROOPS: " + game.getWorldTroopsNumber());
			});
		}
	}
}
