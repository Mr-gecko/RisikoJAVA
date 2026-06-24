package server.model;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.HashMap;

import model.*;
import model.data.GameDataLoader;

public class GameData {


	InputStream countriesStream = ServerGameModel.class.getResourceAsStream("/server/resources/gameData.json");
    InputStream continentsStream = ServerGameModel.class.getResourceAsStream("/server/resources/gameData.json");
	
	// these are READ ONLY
	private HashMap<String, Country> countries;
	private HashMap<String, Continent> continents;
	
	public GameData() throws IOException {
		this.countries = GameDataLoader.loadCountryMap(countriesStream);
		this.continents = GameDataLoader.loadContinentsMap(continentsStream);
	}
	
	// copy constructor
	public GameData(GameData base) {
		
		// deep copy of country
		this.countries = new HashMap<>();
		base.countries.forEach((id, country) -> {
			this.countries.put(id, country.copy());
		});
		
		// no need to deep copy as the continents are read only and will not be edited
		this.continents = base.continents;
	}
	
	public HashMap<String, Country> getCountryMap(){
		return this.countries;
	}
	
	public HashMap<String, Continent> getContinentsMap(){
		return this.continents;
	}
	
}
