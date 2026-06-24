package model.data;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import org.json.JSONArray;
import org.json.JSONObject;

import model.*;


public class GameDataLoader {
	
	

	private static final String DB_COUNTRIES_KEY = "countries";
	private static final String DB_COUNTRY_ID_KEY = "id";
	private static final String DB_COUNTRY_CONTINENT_ID_KEY = "continentId";
	private static final String DB_COUNTRY_BORDERS_IDS_KEY = "bordersIds";
	
	private static final String DB_CONTINENTS_KEY = "continents";	
	private static final String DB_CONTINENT_ID_KEY = "id";
	private static final String DB_CONTINENT_BONUS_KEY = "bonus";
	
	public static HashMap<String, Country> loadCountryMap(InputStream inStream) throws IOException{
		HashMap<String, Country> countries = new HashMap<>();
		JSONObject db = JsonLoader.loadFile(inStream);
		
		// countries list
		JSONObject JSONcountries = db.getJSONObject(DB_COUNTRIES_KEY);
		for (String countryKey : JSONcountries.keySet()) {
			// country item
			JSONObject JSONcountry = JSONcountries.getJSONObject(countryKey);
			
			// country data
			String id = JSONcountry.getString(DB_COUNTRY_ID_KEY);
			String continentId = JSONcountry.getString(DB_COUNTRY_CONTINENT_ID_KEY);
			ArrayList<String> bordersIds = new ArrayList<>();
			
			// filling bordersIds
			JSONArray JSONbordersIds = JSONcountry.getJSONArray(DB_COUNTRY_BORDERS_IDS_KEY);
			for (int j=0; j<JSONbordersIds.length(); j++) {
				bordersIds.add(JSONbordersIds.getString(j));
			}
			
			// creating country object
			Country country = new Country(id, bordersIds, continentId);
			
			// adding country object to map
			countries.put(id, country);
		}
		
		return countries;
		
	}
	
	
	public static HashMap<String, Continent> loadContinentsMap(InputStream inStream) throws IOException{
		HashMap<String, Continent> continents = new HashMap<>();
		JSONObject db = JsonLoader.loadFile(inStream);
		
		// continents list
		JSONObject JSONContinents = db.getJSONObject(DB_CONTINENTS_KEY);
		for (String continentKey : JSONContinents.keySet()) {
			// continent item
			JSONObject JSONContinent = JSONContinents.getJSONObject(continentKey);
			
			// continent data
			String id = JSONContinent.getString(DB_CONTINENT_ID_KEY);
			int bonus = JSONContinent.getInt(DB_CONTINENT_BONUS_KEY);
			
			// creating continent object
			Continent continent = new Continent(id, bonus);
			
			// adding continent object to map
			continents.put(id, continent);
		}
		
		return continents;
		
	}
	
	
}
