package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Scanner;

import server.utils.ServerUtils;
import server.bot.BotBrain;
import server.controller.*;
import server.model.ServerGameModel;
import server.model.ServerLobbyModel;
import server.network.ServerNetwork;
import server.network.utils.ServerSetup;
import model.*;
import model.data.GameDataLoader;
import model.network.*;
import model.utils.Logger;

public class ServerApp {

	public static void main(String[] args) throws InterruptedException {
		
		ServerLobbyModel serverLobbyModel = new ServerLobbyModel();
		ServerGameModel serverGameModel = new ServerGameModel();
		
		ServerLobbyController serverLobbyController = new ServerLobbyController(serverLobbyModel);
		ServerGameController serverGameController = new ServerGameController(serverGameModel, serverLobbyModel);

		
		ServerNetwork serverNetwork = new ServerNetwork(serverLobbyController, serverGameController);
		BotBrain botBrain = new BotBrain();
		serverNetwork.setBotBrain(botBrain);
		
		Thread lobbyControllerThread = new Thread(serverLobbyController);
		lobbyControllerThread.start();	
		
		Thread gameControllerThread = new Thread(serverGameController);
		gameControllerThread.start();
		
		Logger logger = new Logger("SA");
		
		try (ServerSocket serverSocket = new ServerSocket(ServerSetup.DEFAULT_SERVER_PORT)) {
			logger.log("server started | port: " + ServerSetup.DEFAULT_SERVER_PORT);
			
			Thread networkThread = new Thread(() -> {
	            try {
	                serverNetwork.acceptClient(serverSocket);
	            } catch (IOException e) {
	                logger.log("server socket closed");
	            }
	        });
	        networkThread.setDaemon(true);
	        networkThread.start();
	        
	        
	        ServerUtils.serverPrompt(serverSocket, serverNetwork, serverLobbyController, serverLobbyModel, serverGameController, serverGameModel);
	        
			
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
//			serverLobbyController.shutdown();
		}
		
	}
	
}
