package server.network;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import model.network.*;
import model.utils.Logger;

public class ServerClientHandler implements Runnable {

	private final Socket socket;
	private final ServerNetwork serverNetwork;
	private ObjectOutputStream out;
	private long clientId;
	private boolean running;
	
	Logger logger = new Logger("SCH");

	
	public ServerClientHandler(Socket socket, ServerNetwork serverNetwork) {
		this.socket = socket;
		this.serverNetwork = serverNetwork;
		logger.log("initiated with socket: " + socket.getInetAddress());
	}
	
	public void onConnection() throws IOException {
		// send to the client its player id
		this.clientId = this.serverNetwork.getAndIncrementId();
		this.serverNetwork.registerClient(this.clientId, this);
		this.running = true;
		this.out.writeObject(new Payload(Payloads.CLIENT_CONNECTED, this.clientId));
		this.out.flush();
		logger.log("client connected");
	}
	
	public void onDisconnection() {
		// decrease the server client player id
//		this.serverNetwork.getAndDecrementId(); // decrement the server current client id for new clients
		this.serverNetwork.unregisterClient(this.clientId); // remove client id from map of ServerClientHandlers
		this.serverNetwork.onClientDisconnect(this.clientId);
		stop();
		logger.log("client disconnection handled");
	}
	
	
	public void send(Payload payload) {
		try {
			synchronized (out) {
				out.reset(); 
				/*
				ObjectOutputStream keeps an internal cache that maps every object
				it has ever written to a handle (basically an ID). When you write
				the same object again, instead of serializing the full object it 
				just writes the handle — a tiny reference saying "you already have
				this one". This is an optimization to avoid infinite loops with circular
				references and to save bandwidth when the same object is sent multiple times.
				
				this is also present client side
				*/
				
				
				out.writeObject(payload);
				out.flush();
			}
		} catch (Exception e) {
			logger.log("failed to send response to client: " + e.getMessage());
		}
	}
	
	
	public void closeSocket() {
		try {
	        this.socket.close();
	        logger.log("socket closed");
	    } catch (IOException e) {
	        // ignore, we're shutting down
	    }
	}
	
	
	
	public String getAddr() {
		return this.socket.getInetAddress().toString();
	}
	
	public long getClientId() {
		return this.clientId;
	}
	
	
	@Override
	public void run() {
		try {
			this.out = new ObjectOutputStream(socket.getOutputStream());
			this.out.flush();
			ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
			
			onConnection();
			
			while(running) {
				Payload payload = (Payload) in.readObject();
				this.serverNetwork.receive(payload, this);
			}
			
		} catch (IOException e) {
			if (serverNetwork.isShuttingDown()) {
	            logger.log("socket closed by server shutdown");
	        } else {
	            logger.log("client disconnected abruptly: " + this.socket.getInetAddress());
	        }
		} catch (Exception e) {
			logger.log("client disconnected: " + this.socket.getInetAddress());
		} finally {
			try {
				this.socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			onDisconnection();
		}
	}
	
	public void stop() {
		this.running = false;
	}

}
