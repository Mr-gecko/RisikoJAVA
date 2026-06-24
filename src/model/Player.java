package model;

import java.io.Serializable;
import java.util.Iterator;

import model.data.CardBonus;
import model.enums.CardType;

public class Player implements Serializable{
	
	private static final long serialVersionUID = 1L;

	private long id;
	private PlayerColor color;
	private String name;
	private Deck deck;
	private int availableTroops;
	
	private boolean bot = false;
	
	public Player(String name, PlayerColor color) {
		this.name = name;
		this.color = color;
		this.deck = new Deck();
		this.availableTroops = 0;
	}
	
	public String toString() {
//		return "[PLAYER OBJECT] ID: " + this.id;
		return "[PLAYER OBJECT] ID: " + this.id + (isBot() ? "  |  [BOT]" : "") + "  |  CARDS: " + this.deck.getCards().size();
	}
	
	public void setId(long id) {
		this.id = id;
	}
	
	public long getId() {
		return this.id;
	}
	
	public PlayerColor getColor() {
		return this.color;
	}
	
	public void setColor(PlayerColor color) {
		this.color = color;
	}
	
	public String getName() {
		return this.name;
	}
	
	public Deck getDeck() {
		return this.deck;
	}
	
	public void setAvailableTroops(int troops) {
		this.availableTroops = troops;
	}
	
	public int getAvailableTroops() {
		return this.availableTroops;
	}
	
	public void addTroopsToDeploy(int troops) {
		this.availableTroops += troops;
	}
	
	public void removeTroopsToDeploy(int troops) {
		this.availableTroops -= troops;
	}
	
	// #### CARDS #########################################
	
	public int tradeCards() {
		int bonus = getCardBonus();
		if (bonus == 0) return 0;
		
		switch (bonus) {
		case CardBonus.ARTILLERY_BONUS -> removeCards(CardType.ARTILLERY, 3);
		case CardBonus.INFANTRY_BONUS -> removeCards(CardType.INFANTRY, 3);
		case CardBonus.CAVALRY_BONUS -> removeCards(CardType.CAVALRY, 3);
		case CardBonus.ALL_BONUS -> {
			removeCards(CardType.ARTILLERY, 1);
			removeCards(CardType.INFANTRY, 1);
			removeCards(CardType.CAVALRY, 1);
		}
		case CardBonus.JOLLY_BONUS -> {
			removeCards(CardType.JOLLY, 1);
			if (countCards(CardType.ARTILLERY) >= 2) {
				removeCards(CardType.ARTILLERY, 2);
			} else if (countCards(CardType.INFANTRY) >= 2) {
				removeCards(CardType.INFANTRY, 2);
			} else if (countCards(CardType.CAVALRY) >= 2) {
				removeCards(CardType.CAVALRY, 2);
			}
		}
		}
		
		return bonus;
	}
	
	public boolean canTradeCards() {
		return getCardBonus() > 0;
	}
	
	public int getCardBonus() {
		int artillery = countCards(CardType.ARTILLERY);
		int infantry = countCards(CardType.INFANTRY);
		int cavalry = countCards(CardType.CAVALRY);
		int jollies = countCards(CardType.JOLLY);
		
		if (artillery >= 3) return CardBonus.ARTILLERY_BONUS;
		if (infantry >= 3) return CardBonus.INFANTRY_BONUS;
		if (cavalry >= 3) return CardBonus.CAVALRY_BONUS;
		if (artillery >= 1 && infantry >= 1&& cavalry >= 1) return CardBonus.ALL_BONUS;
		if (jollies >= 1 && (artillery >= 2 || infantry >= 2 || cavalry >= 2)) return CardBonus.JOLLY_BONUS;
		return 0;
		
	}
	
	private int countCards(CardType type) {
		return this.deck.getCards().stream()
					.mapToInt(card -> card.getType() == type ? 1 : 0)
					.sum();
	}
	
	private void removeCards(CardType type, int count) {
	    int removed = 0;
	    Iterator<Card> iterator = deck.getCards().iterator();
	    while (iterator.hasNext() && removed < count) {
	        if (iterator.next().getType() == type) {
	            iterator.remove();
	            removed++;
	        }
	    }
	}
	
	
	
	
	
	public void setBot(boolean botFlag) {
		this.bot = botFlag;
	}
	
	public boolean isBot() {
		return bot;
	}
	
	public void copy(Player player) {
		this.color = player.getColor();
		this.deck = player.getDeck();
		this.availableTroops = player.getAvailableTroops();
	}
	
	
	
	
}
