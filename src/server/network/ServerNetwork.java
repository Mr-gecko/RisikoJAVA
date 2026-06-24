package server.network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicLong;

import model.*;
import model.network.*;
import model.utils.Logger;
import server.bot.BotBrain;
import server.controller.ServerBaseController;
import server.controller.ServerGameController;
import server.controller.ServerLobbyController;

public class ServerNetwork {

	private AtomicLong NEXT_CLIENT_ID = new AtomicLong(1);
	
	private ServerLobbyController serverLobbyController;
	private ServerGameController serverGameController;
	private BotBrain botBrain;

	private final ExecutorService clientHandlers = Executors.newCachedThreadPool();
	
	private final Map<Long, ServerClientHandler> connectedClients = new ConcurrentHashMap<>();
	
	private boolean shuttingDown = false;
	
	Logger logger = new Logger("SN");
	
	public ServerNetwork(ServerLobbyController serverLobbyController,
							ServerGameController serverGameController) {
		this.serverLobbyController = serverLobbyController;
		this.serverGameController = serverGameController;
		logger.log("initiated");
	}
	
	public void setBotBrain(BotBrain botBrain) {
		this.botBrain = botBrain;
	}
	
	
	
	
	/**
	 * accept socket connections from client
	 * @param serverSocket
	 * @throws IOException
	 */
	public void acceptClient(ServerSocket serverSocket) throws IOException {
		logger.log("waiting for clients");
		while (true) {
			Socket clientSocket = serverSocket.accept();
			clientSocket.setKeepAlive(true);
			logger.log("client socket connected | " + clientSocket.getInetAddress());
			clientHandlers.submit(new ServerClientHandler(clientSocket, this));
		}
	}
	
	
	
	
	/**
	 * receive payload and manage the payload type
	 * . LOBBY_REQUEST
	 * . GAME_REQUEST
	 * @param payload
	 * @param sender
	 */
	public void receive(Payload payload, ServerClientHandler sender) {
		logger.log("payload received -> " + payload);
		switch(payload.getType()) {
		case Payloads.LOBBY_REQUEST -> receiveRequest(payload, sender, serverLobbyController);
		case Payloads.GAME_REQUEST -> receiveRequest(payload, sender, serverGameController);
		case Payloads.SIMPLE_REQUEST -> receiveBaseRequest(payload, sender);
		}
	}
	
	
	
//	[DEPRECATED]
	/**
	 * receive & handle lobby request
	 * @param payload
	 * @param sender
	 */
//	public void receiveLobbyRequest(Payload payload, ServerClientHandler sender) {
//		logger.log("request directed");
//		LobbyRequest request = (LobbyRequest) payload.getData();
//		// pass a lambda to serverLobbyController instead of only the request
//		// this will work as follow
//		// * serverLobbyController will run handle() on the lambda function
//		// * which will run request.handle() running the actual request
//		// * then building response payload and sending it to the client through the ServerClientHandler
//		this.serverLobbyController.submitRequest(ctx -> {
//			request.handle(ctx); // handle request inside controller
//			
//			Payload response = ctx.buildResponse(request); // let controller build response to request
//
//			logger.log("response built -> " + response);
//			
//			// check if sender is null, this is for server side sent requests
//			if (sender!=null) {
//				sender.send(response);
//				logger.log("response sent to sender client");
//			}
//			
//			// if lobby is updated broadcast the updated lobby to all player in that lobby
//			if (response.getData() != null) {
//				Lobby lobby = (Lobby) response.getData();
//				sendToMultipleRegisteredClients(lobby.getPlayersIds(), response);
//				logger.log("broadcasted to lobby clients");
//			}
//		});
//		
//	}
	
	
	
	
	
	
	
	

//	@SuppressWarnings("unchecked")
//	public <T extends ServerBaseController<T>> void receiveRequest_showoff(Payload payload,
//				ServerClientHandler sender,ServerBaseController<T> controller) {
//		logger.log("request directed");
//		try {
//		Request<T> request = (Request<T>) payload.getData();
//		// pass a lambda to the controller instead of only the request
//		// this will work as follow
//		// * serverLobbyController will run handle() on the lambda function
//		// * which will run request.handle() running the actual request
//		// * then building response payload and sending it to the client through the ServerClientHandler
//		controller.submitRequest(ctx -> {
//			request.handle(ctx); // handle request inside controller
//			
//			Payload response = (ctx.buildResponse((BaseRequest) request)); // let controller build response to request
//
//			logger.log("response built -> " + response);
//			
//			if (response.getData() != null) {
//				switch (response.getType()) {
//				case Payloads.LOBBY_RESPONSE -> handleLobbyResponse(response, sender);
//				case Payloads.GAME_RESPONSE -> handleGameResponse(response, sender);
//				default -> logger.log("RESPONSE TYPE IS THIS [NOT MANAGED]: " + response.getType());
//				}
//			}
//		});
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		
//	}
	
	
	
	
	
	
	
	
	
	@SuppressWarnings("unchecked")
	public <T extends ServerBaseController<T>> void receiveRequest(Payload payload, ServerClientHandler sender, ServerBaseController<T> controller) {
		logger.log("request directed");
		try {
		Request<T> request = (Request<T>) payload.getData();
		// pass a lambda to serverLobbyController instead of only the request
		// this will work as follow
		// * serverLobbyController will run handle() on the lambda function
		// * which will run request.handle() running the actual request
		// * then building response payload and sending it to the client through the ServerClientHandler
		controller.submitRequest(ctx -> {
			request.handle(ctx); // handle request inside controller
			
			Payload response = (ctx.buildResponse((BaseRequest) request)); // let controller build response to request

			logger.log("response built -> " + response);
			
			// check if sender is null, this is for server side sent requests
//			if (sender!=null) {
//				sender.send(response);
//				logger.log("response sent to sender client");
//			}
			
			// if lobby is updated broadcast the updated lobby to all player in that lobby
			switch (response.getType()) {
			case Payloads.LOBBY_RESPONSE -> {
				logger.log("sending lobby response");
				if (response.getData() != null) {
					Lobby lobby = (Lobby) response.getData();
					
					if (sender != null) {
						sender.send(response);
						logger.log("response sent to sender client");
					}
					
					List<Long> others = new ArrayList<>(
						    lobby.getPlayersIds().stream()
						        .filter(id -> sender == null || id != sender.getClientId())
						        .toList()
						);

					others.addAll(lobby.getSpectatorsIds());
					
					sendToMultipleRegisteredClients(others, response);
					logger.log("broadcasted to lobby clients");
					
				} else if (response.getData() == null) {
					if (sender != null) {
						sender.send(response);
						logger.log("response sent to sender client");
					}
				}
				break;
			}
			case Payloads.GAME_RESPONSE -> {
				logger.log("sending game response");
				if (response.getData() != null) {
					Game game = (Game) response.getData();
					Lobby lobby = serverLobbyController.getLobby(game.getId());
					
					if (sender != null) {
			            sender.send(response);
			            logger.log("response sent to sender client");
			        }
					
					
					List<Long> others = new ArrayList<>(
						    lobby.getPlayersIds().stream()
						        .filter(id -> sender == null || id != sender.getClientId())
						        .toList()
						);

					others.addAll(lobby.getSpectatorsIds());
					
				    sendToMultipleRegisteredClients(others, response);
					logger.log("broadcasted to lobby clients");
					
					final Game snapshot = game;
					if (botBrain != null) clientHandlers.submit(() -> botBrain.maybeAct(this, snapshot));
					
					
				} else if (response.getData() == null) {
						logger.log("[ERROR] impossible to manage response, data is null");
				}
				break;
			} default -> {
				logger.log("RESPONSE TYPE IS THIS [NOT MANAGED]: " + response.getType());
			}
			}
		});
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	
	
	public void receiveBaseRequest(Payload payload, ServerClientHandler sender) {
		logger.log("request directed");
		try {
		BaseRequest request = (BaseRequest) payload.getData();
		switch (request.getInstruction()) {
		case GET_AND_INCREMENT_ID -> {
			long nextId = getAndIncrementId();
			Payload response = new Payload(Payloads.SIMPLE_RESPONSE, nextId, true);
			if (sender != null) {
				sender.send(response);
				logger.log("response sent to sender client");
			}
		}
		}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	
	/*
	 * get and increase client connection id
	 */
	public long getAndIncrementId() {
		return this.NEXT_CLIENT_ID.getAndIncrement();
	}
	
	
	
	
	/**
	 * get and decrease client connection id
	 * @return
	 */
	public long getAndDecrementId() {
		return this.NEXT_CLIENT_ID.getAndDecrement();
	}
	
	
	
	/**
	 * link client id to its clientHandler in the map
	 * @param clientId
	 * @param serverClientHandler
	 */
	public void registerClient(long clientId, ServerClientHandler serverClientHandler) {
		this.connectedClients.put(clientId, serverClientHandler);
	}
	
	
	
	
	/** remove client id from the clientHandler map
	 */
	public void unregisterClient(long clientId) {
		this.connectedClients.remove(clientId);
	}
	
	
	
	
	
	/**
	 * get clientHandler from client id
	 * @param clientId
	 * @return
	 */
	public ServerClientHandler getRegisteredClient(long clientId) {
		return this.connectedClients.get(clientId);
	}
	
	
	
	
	/**
	 * send payload to linked client id
	 * @param clientId
	 * @param payload
	 */
	public void sendToRegisteredClient(long clientId, Payload payload) {
	    ServerClientHandler serverClientHandler = connectedClients.get(clientId);
	    if (serverClientHandler != null) {
	    	serverClientHandler.send(payload);
	    }
	}
	
	
	
	
	/**
	 * send to multiple registered client ids from list
	 * @param clientIdList
	 * @param payload
	 */
	public void sendToMultipleRegisteredClients(List<Long> clientIdList, Payload payload) {
		payload.setBroadcast(true);
		clientIdList.forEach(id -> sendToRegisteredClient(id, payload));
	}
	
	
	
	
	/**
	 * send to all clients
	 * @param payload
	 */
	public void sendToAll(Payload payload) {
		this.connectedClients.values().forEach(serverClientHandler -> serverClientHandler.send(payload));
	}
	
	
	
	
	/**
	 * manage particular client disconnections (abrupt / not gracious)
	 * @param clientId
	 */
	public void onClientDisconnect(long id) {
		if (shuttingDown) return;
		// manually handling requests
		
		
		// game controller
	    // remove player from the game and notify the others in the lobby
	    this.serverGameController.submitRequest(ctx -> {
	    	Game game = ctx.getGameFromPlayerId(id);
	    	if (game != null) { // game is null if it was not started
		    	ctx.dismantleGame(game.getId());
		    	// TODO: REPLACE WITH BOTS INSTEAD
		    	Payload response = ctx.buildResponse(new GameRequest(RequestInstruction.GAME_DISMANTLE));
		    	
//		    	ServerClientHandler client = getRegisteredClient(id);
//		    	if (client != null) {
//		    		client.send(response);
//		    	}
		    	try {
		    		sendToMultipleRegisteredClients(game.getLobby().getPlayersIds(), response);
		    	} catch (Exception e) {
		    		// lobby empty or non existent
		    	}
		    	if (response.getData() != null) {
		    		//
		    	} else {
		    		logger.log("response willingly ignored");
		    	}
	    	}
	    });
	    logger.log("game on client disconnect handled");
		
		// lobby controller
		// remove player absolutely and notify the others in the lobby
	    this.serverLobbyController.submitRequest(ctx -> {
	        ctx.removePlayerAbsolute(id);
	        Payload response = ctx.buildResponse(new LobbyRequest(RequestInstruction.LOBBY_REMOVE_PLAYER));
			
			if (response.getData() != null) {
				
//				ServerClientHandler client = getRegisteredClient(id);
//		    	if (client != null) {
//		    		client.send(response);
//		    	}
				
				try {
					Lobby lobby = (Lobby) response.getData();
					sendToMultipleRegisteredClients(lobby.getPlayersIds(), response);
				} catch (Exception e) {
					// lobby asaddadadda
				}
				logger.log("response handled");
			} else {
				logger.log("response willingly ignored");
			}
	    });
	    logger.log("lobby on client disconnect handled");
	    
	    logger.log("client disconnection handled");
	}

	
	public void shutdown() {
		this.shuttingDown = true;
		for (ServerClientHandler serverClientHandler: connectedClients.values()) {
			serverClientHandler.closeSocket();
		}
	    this.connectedClients.clear();
	    this.clientHandlers.shutdownNow();
	    logger.log("network shutdown");
	}
	
	
	
	public boolean isShuttingDown() {
		return this.shuttingDown;
	}
	
	// #### ADD_ONS #########################################
	
	public void printInfo() {
		ThreadPoolExecutor threadPool = ((ThreadPoolExecutor)this.clientHandlers);
		int threadNumber = threadPool.getPoolSize();
		System.out.println("[SN] info print\n" 
				+ "ServerClientHandler threads number: " + threadNumber);
		if (threadNumber > 0) {
			System.out.println("[+] active SCHs");
			for (Map.Entry<Long, ServerClientHandler> entry : connectedClients.entrySet()) {
			    System.out.println(" |-id: " + entry.getKey() + "; addr: " + entry.getValue().getAddr());
			}
		}

	}
	
	
	
	
	
	
//	public void handleLobbyResponse(Payload response, ServerClientHandler sender) {
//		logger.log("sending lobby response");
//		if (response.getData() != null) {
//			Lobby lobby = (Lobby) response.getData();
//			
//			if (sender != null) {
//				sender.send(response);
//				logger.log("response sent to sender client");
//			}
//			
//			List<Long> others = new ArrayList<>(
//				    lobby.getPlayersIds().stream()
//				        .filter(id -> sender == null || id != sender.getClientId())
//				        .toList()
//				);
//
//			others.addAll(lobby.getSpectatorsIds());
//			
//			sendToMultipleRegisteredClients(others, response);
//			logger.log("broadcasted to lobby clients");
//			
//		} else if (response.getData() == null) {
//			if (sender != null) {
//				sender.send(response);
//				logger.log("response sent to sender client");
//			}
//		}
//	}
	
//	public void handleGameResponse(Payload response, ServerClientHandler sender) {
//		logger.log("sending game response");
//		if (response.getData() != null) {
//			Game game = (Game) response.getData();
//			Lobby lobby = serverLobbyController.getLobby(game.getId());
//			
//			if (sender != null) {
//	            sender.send(response);
//	            logger.log("response sent to sender client");
//	        }
//			
//			
//			List<Long> others = new ArrayList<>(
//				    lobby.getPlayersIds().stream()
//				        .filter(id -> sender == null || id != sender.getClientId())
//				        .toList()
//				);
//
//			others.addAll(lobby.getSpectatorsIds());
//						
//		    sendToMultipleRegisteredClients(others, response);
//			logger.log("broadcasted to lobby clients");
//			
//			final Game snapshot = game;
//			if (botBrain != null) clientHandlers.submit(() -> botBrain.maybeAct(serverNetwork, snapshot));
//			
//		} else if (response.getData() == null) {
//				logger.log("[ERROR] impossible to manage response, data is null");
//		}
//	}
	
	
	
	
}
