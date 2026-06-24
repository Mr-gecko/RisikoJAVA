package model;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Condition;

import model.enums.AttackResult;
import model.enums.CardType;
import model.enums.TurnPhase;
import server.model.GameData;

public class Game implements Serializable{
	
	private static boolean TEST_MODE_FOR_DEV = true;
	
	private static final long serialVersionUID = 1L;
	
	private static final int COUNTRY_TROOPS_CONSTANT = 3; // -> troops = N_countries / this_constant
	private static final int BASE_TROOPS_PER_TURN = 3;
	private static final double DYNAMIC_TROOP_DENSITY = 1.;
	
	private static final boolean CONTINENT_BONUS = false;
	

	private long id; // the id is the same as the lobby
	private Lobby lobby;
	private World world;
	private Deck deck;
	private boolean started;
	private boolean ended;
	private boolean turnsStarted;
	private Player winner;
	private boolean conqueredFlag; // if a country has been conquered. Used to pick card
	private int turnNumber = 0;
	
	// setup game mechanics
	private static final Map<Integer, Integer> SETUP_TROOPS = Map.of(
		    2, 40,
		    3, 35,
		    4, 30,
		    5, 25,
		    6, 20);
	private Set<Long> setupReadyPlayers = new HashSet<>(); // unique items
	
	// turn mechanics
	private List<Player> turnOrder;
	private int currentPlayerIndex;
	private TurnPhase currentPhase;
	
	// elimination mechanics
	private List<Player> justEliminated = new ArrayList<>();
	
	
	// deploy mechanics
	private Map<Long, String> deployedCountry = new HashMap<>();   // playerId -> countryId
	private Map<Long, Integer> deployedTroops = new HashMap<>();   // playerId -> troops deployed this turn
	
	
	public Game(Lobby lobby) {
		this.id = lobby.getId();
		this.lobby = lobby;
		this.world = new World();
		this.deck = new Deck();	
		this.ended = false;
		this.started = false;
		this.turnsStarted = false;
		this.conqueredFlag = false;
	}
	
	@Override
	public String toString() {
		return "[GAME OBJECT] ID: " + this.id;
	}

	public long getId() {
		return id;
	}
	
	public World getWorld() {
		return this.world;
	}
	
	public Lobby getLobby() {
		return this.lobby;
	}
	
	public Deck getDeck() {
		return this.deck;
	}
	
	public Player getPlayer(long id) {
		return this.lobby.getPlayer(id);
	}
	
	public Player getPlayer(PlayerColor color) {
	    return lobby.getPlayers().stream()
	        .filter(p -> p.getColor() == color)
	        .findFirst().orElse(null);
	}
	
	public void setup(GameData gameData) {
		worldSetup(gameData);
		deckSetup();
	}
	
	public void distributeCards() {
		Card picked;
		int currentSlot = 0;
		while ((picked = this.deck.pick()) != null) {
			this.lobby.getSlot(currentSlot).getPlayer().getDeck().addCard(picked);
			currentSlot = (currentSlot + 1) % this.lobby.getPlayerCount();
		}
	}
	
	public void TEST_distributeOneCard() {
		lobby.getPlayers().forEach(player -> player.getDeck().pickFrom(deck));
	}
	
	
	/**
	 * retrieve all players cards back to the deck
	 */
	public void retrieveCards() {
		this.lobby.getPlayers()
			.stream()
			.forEach(player -> this.deck.transferFrom(player.getDeck()));
	}
	
	private void assignCountries() {
		this.lobby.getPlayers()
			.stream()
			.forEach(player -> {
				player.getDeck().getCards()
					.stream()
					.forEach(card -> {
						Country country = this.world.getCountry(card.getId());
						if (country != null) {
							country.setOwner(player.getColor());
						}
					});
			});
	}
	
	private void TEST_assignSpecificCountries() {
		ArrayList<String> specificIds = new ArrayList<>();
		specificIds.add("xinjiang");
		specificIds.add("mongolia");
		specificIds.add("qinghai");
		specificIds.add("tibet");
		specificIds.add("pakistan");
		specificIds.add("chita");
		this.lobby.getPlayers()
		.stream()
		.forEach(player -> {
			Country country = this.world.getCountry(specificIds.get(0));
			System.out.println("COUNTRY IS: " + country);
			if (country != null) {
				specificIds.remove(0);
				country.setOwner(player.getColor());
			}

		});
	}

	
	public void start() {
		this.started = true;
		this.ended = false;
		this.lobby.setLocked(true);
		
		// gives country's cards to players
		if (TEST_MODE_FOR_DEV) {
			TEST_distributeOneCard();
		} else {
			distributeCards();
		}
		
		// assign player color to country objects
		
		if (TEST_MODE_FOR_DEV) {
			TEST_assignSpecificCountries();
		} else {
			assignCountries();
		}
		
		
		// get back the cards
		retrieveCards();
		this.deck.addJollies();
		this.deck.shuffle();
		
		gameSetup();
		
//		startTurns();
		
		
	}

	
	private void gameSetup() {
		setCurrentPhase(TurnPhase.SETUP);
		lobby.getPlayers().forEach(p -> p.setAvailableTroops(getDynamicTroops()));
		autoDeployEmptyCountryInSetup();
	}
	
	
	private void autoDeployEmptyCountryInSetup() {
		world.getCountries().values().forEach(country -> {
			Player owner = lobby.getPlayer(country.getOwner());
			if (owner != null) {
				owner.removeTroopsToDeploy(1);
				country.addTroops(1);
			}
		});
	}
	
	// #### world ####
	
	public void worldSetup(GameData gameData) {
		loadGameData(gameData);
	}
	
	public void loadGameData(GameData gameData) {
		this.world.setCountries(gameData.getCountryMap());
		this.world.setContinents(gameData.getContinentsMap());
	}
	
	// #### deck ####
	
	public void deckSetup() {
		generateDeck();
		this.deck.shuffle();
		
	}
	
	// only after loadGameData
	public void generateDeck() {
		try {
			if (this.world.getCountries() != null) {
				this.deck.populateCountryCards(new ArrayList<String>(this.world.getCountries().keySet()));
			} else {
				throw new NullPointerException("world countries have not been loaded");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	
	
	public int getWorldTroopsNumber() {
		return this.world.getTroopsNumber();
	}
	
	public void end() {
		this.ended = true;
		this.started = false;
		this.lobby.setLocked(false);
	}
	
	public boolean isEnded() {
		return this.ended;
	}
	
	public boolean isStarted() {
		return this.started;
	}
	
	public void setStarted(boolean value) {
		this.started = value;
	}
	
	public boolean isCountryOwnedBy(String countryId, long playerId) {
		return this.world.getCountry(countryId).getOwner() == this.lobby.getPlayer(playerId).getColor() ? true : false;
	}
	
	
	// #### turn mechanics ################################
	
	private void startTurns() {
		this.turnsStarted = true;
	    this.turnOrder = new ArrayList<>(this.lobby.getPlayers());
	    System.out.println("[DEBUG] creating turn order: " + turnOrder);
	    Collections.shuffle(this.turnOrder);
	    setCurrentPlayerIndex(0);
	    setCurrentPhase(TurnPhase.DEPLOY);
        giveTroopsToCurrentPlayer();
	}
	
	public Player getCurrentPlayer() {
		if (turnOrder == null || turnOrder.isEmpty()) {
			System.out.println("[DEBUG] turnOrder: " + turnOrder);
			return null;
		};
		return turnOrder.get(currentPlayerIndex);
	}
	
	public TurnPhase getCurrentPhase() {
		return this.currentPhase;
	}
	
	public void nextPhase() {
		clearJustEliminated();
//		this.currentPhase = TurnPhase.values()[(currentPhase.ordinal() + 1) % TurnPhase.values().length];
		switch (currentPhase) {
        case DEPLOY -> setCurrentPhase(TurnPhase.ATTACK);
        case ATTACK -> {
        	if (isConqueredThisTurn()) {
        		Player player = getCurrentPlayer();
        		giveCard(player);
        		setConquered(false);
        	}
        	setCurrentPhase(TurnPhase.MOVE);
        }
        case MOVE   -> {
            int nextIndex = (currentPlayerIndex + 1) % turnOrder.size();
            if (nextIndex == 0) turnNumber++;
            setCurrentPlayerIndex(nextIndex);
            setCurrentPhase(TurnPhase.DEPLOY);
            giveTroopsToCurrentPlayer();
            setConquered(false);
            deployedCountry.clear();
            deployedTroops.clear();
        	
//        	setCurrentPlayerIndex((currentPlayerIndex + 1) % turnOrder.size());
//            setCurrentPhase(TurnPhase.DEPLOY);
//            giveTroopsToCurrentPlayer();
//    		setConquered(false);
//            deployedCountry.clear();  // reset deployment tracking
//            deployedTroops.clear();
        }
    }
	}
	
	
	private void giveTroopsToCurrentPlayer() {
//	    if (turnNumber == 0) return;
		getCurrentPlayer().setAvailableTroops(calculateTroops(getCurrentPlayer()));
	}
	
	private void setCurrentPhase(TurnPhase phase) {
		this.currentPhase = phase;
	}
	
	private void setCurrentPlayerIndex(int index) {
		this.currentPlayerIndex = index;
	}
	
	// #### game logics ###################################
	
	private int getNumberOfCountriesOwned(Player player) {
		return this.world.getCountries().values()
				.stream()
				.mapToInt(country -> player.getColor() == country.getOwner() ? 1 : 0)
				.sum();
	}
	
	private boolean hasWholeContinent(Player player, String continentId) {
		return this.world.getCountries().values()
				.stream()
				.filter(country -> country.getContinentId().equals(continentId))
				.allMatch(country -> country.getOwner() == player.getColor());
	}
	
	private int getContinentBonus(Player player) {
		if (!CONTINENT_BONUS) return 0;
		return this.world.getContinents().values()
				.stream()
				.mapToInt(continent -> hasWholeContinent(player, continent.getId()) ? continent.getBonus() : 0)
				.sum();
	}
	
	private int getCountriesTroops(Player player) {
		return getNumberOfCountriesOwned(player) / COUNTRY_TROOPS_CONSTANT;
	}
	
//	public int calculateTroops(Player player) {
//		if (TEST_MODE_FOR_DEV) {
//			return 10;
//		}
//		
//		int troops = 0;
//		troops += BASE_TROOPS_PER_TURN;
//		troops += getCountriesTroops(player);
//		troops += getContinentBonus(player);
//		return troops;
//	}
//	
	
	public int calculateTroops(Player player) {
	    int totalCountries = this.world.getCountries().size();
	    int dynamicConstant = Math.max(3, totalCountries / 8); // scales with map size
	    int fromCountries = getNumberOfCountriesOwned(player) / dynamicConstant;
	    return Math.max(BASE_TROOPS_PER_TURN, fromCountries) + getContinentBonus(player);
	}
	
	
	public boolean isPlayerTurn(long playerId) {
		return this.getCurrentPlayer().getId() == playerId;
	}
	
	public boolean isValidPhase(TurnPhase phase) {
		return this.getCurrentPhase() == phase;
	}

	public void updateDeployment(long playerId, String countryId, int troops) {
	    deployedCountry.put(playerId, countryId);
	    deployedTroops.merge(playerId, troops, Integer::sum);
	}

	public String getDeployedCountry(long playerId) {
	    return deployedCountry.get(playerId);
	}

	public int getDeployedTroops(long playerId) {
	    return deployedTroops.getOrDefault(playerId, 0);
	}
	
	public List<Player> getTurnOrder() {
	    return this.turnOrder;
	}
	
	public void replacePlayerInTurnOrder(long playerId, Player player) {
		for (int i = 0; i < turnOrder.size(); i++) {
	        if (turnOrder.get(i).getId() == playerId) {
	            turnOrder.set(i, player);
	            return;
	        }
	    }
	}
	
	
	// #### GAME SETUP MECHANICS ##########################
	
	public int getSetupTroops() {
	    return SETUP_TROOPS.getOrDefault(lobby.getPlayerCount(), 20);
	}

	public boolean isSetupComplete() {
	    return setupReadyPlayers.size() == lobby.getPlayerCount();
	}

	public void markSetupReady(long playerId) {
	    setupReadyPlayers.add(playerId);
	    if (isSetupComplete()) startTurns();
	}
	
	public boolean areTurnsStarted() {
	    return this.turnsStarted;
	}
	
//	private int getDynamicSetupTroops() {
//		if (TEST_MODE_FOR_DEV) {
//			return 5;
//		}
//		
//		int nCountries = this.world.getCountries().size();
//		int nPlayers = this.lobby.getPlayerCount();
//		double troopsDensity = DYNAMIC_TROOP_DENSITY;
//		
//		int result = (int)((nCountries * troopsDensity) / nPlayers);
//		
//		return result;
//		
//	}
	
	private int getDynamicTroops() {
	    final double TROOPS_PER_COUNTRY = 5.0;
	    final int    MIN_SETUP_TROOPS   = 5;
	    final int    MAX_SETUP_TROOPS   = 60;

	    int nCountries = this.world.getCountries().size();
	    int nPlayers   = this.lobby.getPlayerCount();

	    int result = (int) Math.round((nCountries * TROOPS_PER_COUNTRY) / nPlayers);
	    return Math.max(MIN_SETUP_TROOPS, Math.min(MAX_SETUP_TROOPS, result));
	}
	
	
	// #### winner flag ###################################
	
	public void end(Player winner) {
	    this.ended = true;
	    this.started = false;
	    this.winner = winner;
	}
	
	public Player getWinner() {
	    return this.winner;
	}

	public boolean hasWinner() {
	    return this.winner != null;
	}
	
	public Player checkWinCondition() {
		
		// condition 1
	    long distinctOwners = world.getCountries().values().stream()
	        .map(Country::getOwner)
	        .distinct()
	        .count();
	    if (distinctOwners == 1) {
	        PlayerColor winnerColor = world.getCountries().values()
	            .stream().findFirst().get().getOwner();
	        return lobby.getPlayers().stream()
	            .filter(p -> p.getColor() == winnerColor)
	            .findFirst()
	            .orElse(null);
	    }
	    
	    
	    
	    // condition 2
	    if (turnOrder != null && turnOrder.size() == 1) {
	        return turnOrder.get(0);
	    }
	    
	    return null;
	}
	
	// #### PICK CARD MECHANICS ###########################
	
	public boolean isConqueredThisTurn() {
		return conqueredFlag;
	}
	
	public void setConquered(boolean value) {
		this.conqueredFlag = value;
	}
	
	private void giveCard(Player player) {
		player.getDeck().pickFrom(this.deck);
	}
	
	// #### utilities #####################################
	
	
	public int getNumberOfCountriesOwnedBy(Player player) {
		return this.world.getCountries().values()
				.stream()
				.mapToInt(country -> player.getColor() == country.getOwner() ? 1 : 0)
				.sum();
	}
	
	public int getTotalTroopsOf(Player player) {
		return this.world.getCountries().values()
				.stream()
				.mapToInt(country -> player.getColor() == country.getOwner() ? country.getTroops() : 0)
				.sum();
	}
	
	public void eliminatePlayer(long playerId) {
	    Player player = lobby.getPlayers().stream()
	        .filter(p -> p.getId() == playerId)
	        .findFirst()
	        .orElse(null);
	    if (player == null) return;

	    justEliminated.add(player);
	    int idx = turnOrder.indexOf(player);
	    turnOrder.remove(player);
	    if (idx <= currentPlayerIndex && currentPlayerIndex > 0) {
	        currentPlayerIndex--;
	    }
	}
	
	public List<Player> getJustEliminated() { return this.justEliminated; }

	public void clearJustEliminated() { this.justEliminated = new ArrayList<>(); }
	
}
