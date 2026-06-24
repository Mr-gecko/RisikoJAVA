package model;

import java.io.Serializable;

import model.enums.CardType;

public class Card implements Serializable{

	private CardType type;
	private String countryId;
	
	public Card(CardType type, String countryId) {
		this.type = type;
		this.countryId = countryId;
	}
	
	@Override
	public String toString() {
		return "[CARD]  |  type: " + type + "  |  countryId: " + this.countryId;
	}
	
	public String getId() {
		return this.countryId;
	}
	
	public CardType getType() {
		return this.type;
	}
	
	
}
