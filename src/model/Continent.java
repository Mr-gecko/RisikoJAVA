package model;

import java.io.Serializable;

public class Continent implements Serializable {

	private final String id;
	private int troopsBonus;
	
	public Continent(String id, int troopsBonus) {
		this.id = id;
		this.troopsBonus = troopsBonus;
	}
	
	
	public String getId() {
		return this.id;
	}
	
	
	public int getBonus() {
		return this.troopsBonus;
	}
	
}
