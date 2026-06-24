	package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import model.enums.CardType;

public class Deck implements Serializable{
	
	private static final int JOLLY_CONSTANT = 26; // number of jollies = nCountries / this_constant

	private ArrayList<Card> cards;
	
	public Deck() {
		this.cards = new ArrayList<>();
	}
	
	@Override
	public String toString() {
		return this.cards.stream().map(Card::toString).collect(Collectors.joining("\n"));
	}
	
	public void addCard(Card card) {
		this.cards.add(card);
	}
	
	public void removeCard(Card card) {
		this.cards.remove(card);
	}
	
	public Card pick() {
		if (!this.cards.isEmpty()) {
			Card card = this.cards.get(0);
			this.cards.remove(0);
			return card;
		} return null;
	}
	
	public void pickFrom(Deck deck) {
		Card card = deck.pick();
		this.cards.add(card);
	}
	
	public void populateCountryCards(ArrayList<String> countryIds) {
		// for an even CardType distribution the countries should be multiple of 3 (numbers of CardType)
		for (int i=0; i<countryIds.size(); i++) {
			Card card = new Card(CardType.values()[i % (CardType.values().length -1)], countryIds.get(i)); // length -1 to avoid using the jolly enum
			this.cards.add(card);
		}
	}
	
	public void addJollies() {
		for (int i=0; i<(this.cards.size()) / JOLLY_CONSTANT; i++) {
			Card jolly = new Card(CardType.JOLLY, null);
			this.cards.add(jolly);
		}
	}
	
	public void clear() {
		this.cards.clear();
	}
	
	
	public void shuffle() {
		Collections.shuffle(this.cards);
	}
	
	public int getNumberOfCards() {
		return this.cards.size();
	}
	
	/**
	 * transfer all cards from one deck to this one
	 * @param deck
	 */
	public void transferFrom(Deck deck) {
		Card card;
		while((card = deck.pick()) != null) {
			this.cards.add(card);
		}
	}
	
	public ArrayList<Card> getCards(){
		return this.cards;
	}
	
}
