package server.utils;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.Scanner;
import java.util.function.Consumer;

import model.Game;
import model.Player;
import model.network.GameRequest;
import model.network.LobbyRequest;
import model.network.Payload;
import model.network.Payloads;
import model.network.RequestInstruction;
import model.utils.Logger;
import server.controller.ServerGameController;
import server.controller.ServerLobbyController;
import server.network.ServerClientHandler;
import server.network.ServerNetwork;
import server.model.*;

public class ServerUtils {

	static Logger logger = new Logger("SERVER");
	
	public static void serverPrompt(ServerSocket serverSocket, ServerNetwork serverNetwork, ServerLobbyController serverLobbyController, ServerLobbyModel serverLobbyModel, ServerGameController serverGameController, ServerGameModel serverGameModel) throws InterruptedException, IOException {		
		
		
		Scanner scanner = new Scanner(System.in);
		
		
		
		
        while (scanner.hasNextLine()) {
            String command = scanner.nextLine().trim();
            switch (command) {
                case "info" -> {
                	System.out.println("#### [SERVER INFOS] ####");
                	System.out.println("");
                	serverNetwork.printInfo();
                	System.out.println("");
                	serverLobbyModel.printInfo();
                	System.out.println("");
                	serverGameModel.printInfo();
                	System.out.println("");
                }
                case "stop" -> {
                    logger.log("shutting down");
                    serverNetwork.sendToAll(new Payload(Payloads.SERVER_SHUTDOWN, null));
                    Thread.sleep(500);
                    serverLobbyController.shutdown();
                    serverGameController.shutdown();
                    serverNetwork.shutdown();
                    serverSocket.close();
                    return;
                }
                case "clear" -> {
                	System.out.println("\033[2J"); // screen
                	System.out.println("\033[3J"); // scrollback
                	System.out.println("\033[H");  // home position
                }
                
                case "delete_lobby" -> {
                	System.out.printf("code: ");
                	String code = scanner.nextLine();
                	if (serverLobbyController.getLobbies().get(code)!=null) {
                		LobbyRequest request = new LobbyRequest(RequestInstruction.LOBBY_DISMANTLE, code, null);
                		Payload payload = new Payload(Payloads.LOBBY_REQUEST, request);
                		serverNetwork.receive(payload, null);
                		logger.log("lobby deleted");
                	}
                }
                
                case "kick_player" -> {
                	System.out.printf("id: ");
                	long id = Long.parseLong(scanner.nextLine());
                	Player player = serverLobbyController.getPlayerAbsolute(id);
                	if (player != null) {
                		serverLobbyController.removePlayerAbsolute(id);
                	}
                }
                
                case "disconnect_client" -> {
                	logger.log("disconnecting client");
                	serverNetwork.printInfo();
                	System.out.printf("id: ");
                	long id = Long.parseLong(scanner.nextLine());
                	ServerClientHandler client = serverNetwork.getRegisteredClient(id);
                	if (client != null) {
                        serverNetwork.sendToRegisteredClient(id, new Payload(Payloads.CLIENT_DISCONNECTED, null));;
                        client.onDisconnection();
                	}
                }
                
                case "give_cards" -> {
                	logger.log("giving card");
                	System.out.printf("how many cards: ");
                	int n = Integer.parseInt(scanner.nextLine());
                	serverGameModel.printInfo();
                	System.out.printf("game id: ");
                	long id = Long.parseLong(scanner.nextLine());
                	serverNetwork.printInfo();
                	System.out.printf("player id: ");
                	Game game = serverGameController.getGame(id);
                	long playerId = Long.parseLong(scanner.nextLine());
                	ServerClientHandler client = serverNetwork.getRegisteredClient(playerId);
                	if (game != null && client != null) {
                		for (int i=0; i<n; i++) {
                		GameRequest request = new GameRequest(RequestInstruction.GAME_PICK_CARD, game.getId(), playerId);
                		Payload payload = new Payload(Payloads.GAME_REQUEST, request);
                		serverNetwork.receive(payload, client);
                		logger.log("card picked");
                		}
                	}
                }
                
                default -> logger.log("unknown command: " + command);
            }
        }
	}
	
}
