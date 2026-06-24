package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class World implements Serializable {

	
	// id | country
	private HashMap<String, Country> countries;
	private HashMap<String, Continent> continents;
	
	
	public World() {
		this.countries = new HashMap<>();
		this.continents = new HashMap<>();
	}
	
	
	@Override
	public String toString() {
		return "[WORLD OBJECT]";
	}
	
	
	
	public HashMap<String, Country> getCountries() {
		return countries;
	}

	public HashMap<String, Continent> getContinents() {
		return continents;
	}

	public void setCountries(HashMap<String, Country> countries) {
		this.countries = countries;
	}
	
	public void setContinents(HashMap<String, Continent> continents) {
		this.continents = continents;
	}
	
	public ArrayList<String> getBorders(String countryId){
		return this.countries.get(countryId).getBorders();
	}
	
	public Country getCountry(String countryId) {
		return this.countries.get(countryId);
	}
	
	
	
	
	public int getTroopsNumber() {
		return this.countries.values().stream().mapToInt(Country::getTroops).sum();
	}
	
	
	
	
	
	
	
}
