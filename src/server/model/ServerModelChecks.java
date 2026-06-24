package server.model;

import model.Lobby;

public class ServerModelChecks {

	public static void checkIfLobbyIsValid(Lobby lobby){
		if(lobby==null) {
			throw new NullPointerException("");
		} 
	}
	
}
