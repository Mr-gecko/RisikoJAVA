package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

public class Country implements Serializable{

	private final String id;
	private int troops;
	private ArrayList<String> bordersIds;
	private String continentId;
	private PlayerColor owner;
		
	public Country(String id, ArrayList<String> bordersIds, String continentId) {
		this.id = id;
		this.bordersIds = bordersIds;
		this.continentId = continentId;
	}
	
	
	
	
	public ArrayList<String> getBorders(){
		return this.bordersIds;
	}
	
	
	
	
	
	
	
	
	
	/**
	 * add troops
	 * @param troops
	 */
	public void addTroops(int troops) {
		this.troops += troops;
	}
	
	
	
	
	
	/**
	 * remove troops
	 * @param troops
	 */
	public void removeTroops(int troops) {
		this.troops -= troops;
	}
	
	
	
	
	
	public int getTroops() {
		return this.troops;
	}
	
	
	
	
	
	
	public PlayerColor getOwner() {
		return this.owner;
	}
	
	public void setOwner(PlayerColor owner) {
		this.owner = owner;
	}
	
	
	public String getId() {
		return this.id;
	}
	
	
	
	
	public String getContinentId() {
		return this.continentId;
	}
	
	
	
	public String getName() {
	    return Arrays.stream(this.id.split("_"))
	        .map(word -> Character.toUpperCase(word.charAt(0)) + word.substring(1))
	        .collect(Collectors.joining(" "));
	}
	
	public String getContinentName() {
	    return Arrays.stream(this.continentId.split("_"))
	        .map(word -> Character.toUpperCase(word.charAt(0)) + word.substring(1))
	        .collect(Collectors.joining(" "));
	}
	
	
	
	
	public Country copy() {
		Country copy = new Country(this.id, this.bordersIds, this.continentId);
		return copy;
	}
}
