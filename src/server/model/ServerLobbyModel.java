package server.model;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import model.*;

public class ServerLobbyModel {

	private HashMap<Long, Lobby> lobbies;
	
	public ServerLobbyModel() {
		this.lobbies = new HashMap<>();
	}
	
	
	
	
	/**
	 * get lobbies hashmap
	 * @return
	 */
	public HashMap<Long, Lobby> getLobbies(){
		return this.lobbies;
	}
	
	
	
	
	
	
	
	
	/**
	 * return Lobby object from the lobbies map
	 * @param id
	 * @return
	 */
	public Lobby getLobby(long id) {
		return this.lobbies.get(id);
	}
	
	
	/**
	 * return Lobby object if code match
	 * return null if code doesn't match
	 * @param code
	 * @return
	 */
	public Lobby getLobby(String code) {
		return this.lobbies.values()
				.stream()
				.filter(lobby -> lobby.getCode().equals(code.toUpperCase())) // case insensitive
				.findFirst()
				.orElse(null);
	}
	
	
	
	
	
	
	
	
	
	/**
	 * add lobby to lobbies map
	 * @param lobby
	 */
	public void addLobby(Lobby lobby) {
		long id = lobby.getId();
		this.lobbies.put(id, lobby);
	}
	
	
	
	
	
	
	
	
	
	
	/**
	 * remove lobby from lobbies map
	 * @param lobby
	 */
	public void removeLobby(Lobby lobby) {
		long id = lobby.getId();
		this.lobbies.remove(id);
	}
	
	
	
	
	
	
	
	

	/**
	 * create new lobby object and add it to the map
	 * @param id
	 * @return lobby
	 */
	public Lobby createLobby(long id) {
		String code = Lobby.generateCode();
		
		// TODO: better loop exit
		int retry_cap = 100;
		int retries = 0;
		while (retries<retry_cap) {
			String copyCatCode = code;
			if (this.lobbies.values().stream().noneMatch(lobby -> lobby.getCode().equals(copyCatCode))) {
				break;
			}
			code = Lobby.generateCode();
			retries++;
		}
		Lobby lobby = new Lobby(id, code);
		addLobby(lobby);
		return lobby;
	}
	
	
	
	
	
	
	
	
	
	
	
	/**
	 * dismantle lobby object
	 * @param lobby
	 */
	public void dismantleLobby(Lobby lobby) {
		removeLobby(lobby);
	}
	
	// overloading
	public void dismantleLobby(long id) {
		Lobby lobby= getLobby(id);
		dismantleLobby(lobby);
	}
	
	// overloading
	public void dismantleLobby(String code) {
		Lobby lobby = getLobby(code);
		dismantleLobby(lobby);
	}

	
	
	
	
	
	
	
	
	
	
	/**
	 * add player to lobby object
	 * @param lobby
	 * @param player
	 * @return
	 */
	public Lobby addPlayer(Lobby lobby, Player player) {
		
		if(lobby==null) {
			throw new NullPointerException("lobby not found");
		} 
		
		lobby.add(player);
		return lobby;
	}
	
	// overloading
	public Lobby addPlayer(long id, Player player) {
		Lobby lobby = getLobby(id);
		return addPlayer(lobby, player);
	}
	
	// overloading
	public Lobby addPlayer(String code, Player player) {
		Lobby lobby = getLobby(code);
		return addPlayer(lobby, player);
	}
	
	
	
	
	
	
	
	
	/**
	 * remove player from lobby object
	 * @param lobby
	 * @param player
	 * @return
	 */
	public Lobby removePlayer(Lobby lobby, Player player) {
		if(lobby==null) {
			throw new NullPointerException("lobby not found");
		} 
		
		lobby.remove(player);
		return checkForEmptyLobby(lobby);
	}
	
	// [CRUCIAL] overloading
	public Lobby removePlayer(Lobby lobby, long playerId) {
		lobby.remove(playerId);
		return checkForEmptyLobby(lobby);
	}
	
	// [CRUCIAL] overloading
	public Lobby removePlayer(long id, long playerId) {
		Lobby lobby = getLobby(id);
		return removePlayer(lobby, playerId);
	}

	// overloading
	public Lobby removePlayer(String code, long playerId) {
		Lobby lobby = getLobby(code);
		return removePlayer(lobby, playerId);
	}
	
	// [CRUCIAL] overloading
	public Lobby removePlayer(String code, Player player) {
		Lobby lobby = getLobby(code);
		return removePlayer(lobby, player);
	}
	
	
	
	
	
	
	
	
	public Lobby removeBot(Lobby lobby) {
		if(lobby==null) {
			throw new NullPointerException("lobby not found");
		} 
		lobby.removeBot();
		return checkForEmptyLobby(lobby);
	}
	
	public Lobby removeBot(Lobby lobby, Player player) {
		if(lobby==null) {
			throw new NullPointerException("lobby not found");
		} 
		lobby.remove(player);
		return checkForEmptyLobby(lobby);
	}
	
	
	
	
	
	
	public Lobby addBot(Lobby lobby, Player player) {
			
			if(lobby==null) {
				throw new NullPointerException("lobby not found");
			} 
			
			lobby.add(player);
			Slot slot = lobby.getPlayerSlot(player.getId());
			slot.setStatus(SlotStatus.READY);
			return lobby;
		}
		
		// overloading
		public Lobby addBot(long id, Player player) {
			Lobby lobby = getLobby(id);
			return addBot(lobby, player);
		}
		
		// overloading
		public Lobby addBot(String code, Player player) {
			Lobby lobby = getLobby(code);
			return addBot(lobby, player);
		}
	
	
	
	
	
	/**
	 * remove player from the lobby it's in
	 * @param id
	 * @return
	 */
	public Lobby removePlayerAbsolute(long id) {
		return getLobbies().values()
			.stream()
			.filter(lobby -> lobby.getPlayerSlot(id) != null)
			.findFirst()
			.map(lobby -> {
				removePlayer(lobby.getId(), id);
				return checkForEmptyLobby(lobby);
			})
			.orElse(null);
	}
	
	// overloading
	public Lobby removePlayerAbsolute(Player player) {
		return removePlayerAbsolute(player.getId());
	}
	
	
	
	
	
	
	
	
	
	public Player getPlayerAbsolute(long id) {
		this.lobbies.values().stream()
				.filter(lobby -> lobby.getPlayersIds().contains(id))
				.map(lobby -> {return lobby.getPlayerSlot(id).getPlayer();});
		return null;
	}

	
	
	
	
	
	
	
	
	
	
	
	
	/**
	 * remove all players from lobby object
	 * @param lobby
	 */
	public void removeAll(Lobby lobby) {
		lobby.removeAll();
		checkForEmptyLobby(lobby);
	}
	
	// overloading
	public void removeAll(long id) {
		Lobby lobby = getLobby(id);
		removeAll(lobby);
	}

	// overloading
	public void removeAll(String code) {
		Lobby lobby = getLobby(code);
		removeAll(lobby);
	}
	
	


	
	
	
	
	
	
	
	/**
	 * check if lobby is empty, if so remove lobby from map, else return passed lobby object
	 * @param lobby
	 */
	public Lobby checkForEmptyLobby(Lobby lobby) {
		if (lobby.isEmpty()) {
			getLobbies().remove(lobby.getId());
			return null;
		} return lobby;
	}
	
	
	
	
	
	
	
	
	
	/**
	 * set player slot status
	 * @param lobby
	 * @param player
	 * @param status
	 * @return
	 */
	public Lobby setPlayerStatus(Lobby lobby, Player player, SlotStatus status) {
		Slot slot = lobby.getPlayerSlot(player);
		
		if (slot != null) {
			slot.setStatus(status);
		}
		return lobby;
	}
	
	// overloading
	public Lobby setPlayerStatus(long id, Player player, SlotStatus status) {
		Lobby lobby = getLobby(id);
		return setPlayerStatus(lobby, player, status);
	}
	
	// overloading
	public Lobby setPlayerStatus(String code, Player player, SlotStatus status) {
		Lobby lobby = getLobby(code);
		return setPlayerStatus(lobby, player, status);
	}
	
	
	
	
	
	
	/**
	 * get player status from lobby object
	 * @param lobby
	 * @param player
	 * @return
	 */
	public SlotStatus getPlayerStatus(Lobby lobby, Player player) {
		Slot slot = lobby.getPlayerSlot(player);
		
		if (slot == null) {
			throw new NullPointerException("player: " + player.getId() + " status is not set in lobby: " + lobby.getCode());
		}
		
		return slot.getStatus();
	}

	// overloading
	public SlotStatus getPlayerStatus(String code, Player player) {
		Lobby lobby = getLobby(code);
		return getPlayerStatus(lobby, player);
	}
	
	// overloading
	public SlotStatus getPlayerStatus(long id, Player player) {
		Lobby lobby = getLobby(id);
		return getPlayerStatus(lobby, player);
	}
	
	
	
	
	
	
	/**
	 * manage player status between ready and connected
	 * @param lobby
	 * @param player
	 * @return
	 */
	public Lobby togglePlayerReadyStatus(Lobby lobby, Player player) {
		SlotStatus status = getPlayerStatus(lobby, player);
		switch (status) {
		case CONNECTED -> setPlayerStatus(lobby, player, SlotStatus.READY);
		case READY -> setPlayerStatus(lobby, player, SlotStatus.CONNECTED);
		}
		return lobby;
	}
	
	// overloading
	public Lobby togglePlayerReadyStatus(String code, Player player) {
		Lobby lobby = getLobby(code);
		return togglePlayerReadyStatus(lobby, player);
	}
	
	// overloading
		public Lobby togglePlayerReadyStatus(long id, Player player) {
			Lobby lobby = getLobby(id);
			return togglePlayerReadyStatus(lobby, player);
	}
	
	
	
	
	/**
	 * update player object inside lobby slot
	 * @param lobby
	 * @param player
	 * @return
	 */
	public Lobby updatePlayer(Lobby lobby, Player player) {
		Slot slot = lobby.getPlayerSlot(player);
		
		if (slot != null) {
			slot.setPlayer(player);
		}
		
		return lobby;
	}
		
	// overloading
	public Lobby updatePlayer(String code, Player player) {
		Lobby lobby = getLobby(code);
		return updatePlayer(lobby, player);
	}
	
	// overloading
	public Lobby updatePlayer(long id, Player player) {
		Lobby lobby = getLobby(id);
		return updatePlayer(lobby, player);
	}
	
	
	
	
	
	
	
	
	
	public Lobby addSpectator(Lobby lobby, Player player) {
		
		if(lobby==null) {
			throw new NullPointerException("lobby not found");
		} 
		
		lobby.addSpectator(player);
		return lobby;
	}
	
	// overloading
	public Lobby addSpectator(long id, Player player) {
		Lobby lobby = getLobby(id);
		return addSpectator(lobby, player);
	}
	
	// overloading
	public Lobby addSpectator(String code, Player player) {
		Lobby lobby = getLobby(code);
		return addSpectator(lobby, player);
	}
	
		
	
	
	
	
	
	
	
	
	public Lobby removeSpectator(Lobby lobby, Player player) {
		if(lobby==null) {
			throw new NullPointerException("lobby not found");
		} 
		
		lobby.removeSpectator(player);
		return checkForEmptyLobby(lobby);
	}
	
	// [CRUCIAL] overloading
	public Lobby removeSpectator(Lobby lobby, long playerId) {
		lobby.removeSpectator(playerId);
		return checkForEmptyLobby(lobby);
	}
	
	// [CRUCIAL] overloading
	public Lobby removeSpectator(long id, long playerId) {
		Lobby lobby = getLobby(id);
		return removeSpectator(lobby, playerId);
	}

	// overloading
	public Lobby removeSpectator(String code, long playerId) {
		Lobby lobby = getLobby(code);
		return removeSpectator(lobby, playerId);
	}
	
	// [CRUCIAL] overloading
	public Lobby removeSpectator(String code, Player player) {
		Lobby lobby = getLobby(code);
		return removeSpectator(lobby, player);
	}
	
	
	
	
	
	
	
	
	public void printInfo() {
		System.out.println("[SLM] info print");
		System.out.println("lobbies number: " + this.lobbies.size());
		if (this.lobbies.size() != 0) {
			System.out.println("[+] active lobbies");
			this.lobbies.values().forEach(lobby -> {
				System.out.println(" |-" + "id: " + lobby.getId() + "; " + "code: " + lobby.getCode() + "; " + "players: " + lobby.getPlayerCount());
				Arrays.stream(lobby.getSlots()).forEach(slot -> {
		            if (!slot.isEmpty()) {
		            	System.out.println("    |-" + slot.getPlayer() + "; " + "status: " + slot.getStatus());
//		                System.out.println("    |-" + "name: " + slot.getPlayer().getName() + "; " + "status: " + slot.getStatus());
		            } else {
		                System.out.println("    |-[empty]");
		            }
		        });
			});
		}
	}
	
	
	
	
//	// collectors!!!
//	public List<Long> getLobbyClientIds(Lobby lobby) {
//	    return Arrays.stream(lobby.getSlots())
//	        .filter(slot -> !slot.isEmpty())
//	        .map(slot -> slot.getPlayer().getId())
//	        .collect(Collectors.toList());
//	}
	
}
