package client.network;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

import model.Player;
import model.network.Payload;
import model.network.Payloads;
import model.utils.Logger;

public class ClientNetwork {

	private final Socket socket;
	private final ObjectInputStream in;
	private final ObjectOutputStream out;
	private CompletableFuture<Payload> pendingResponse;
	private long id;
	
	private boolean listening;
	
	Logger logger = new Logger("CN");
	
	public ClientNetwork(String ip, int port, Player player) throws IOException, ClassNotFoundException {
		this.socket = new Socket(ip, port);
		this.socket.setKeepAlive(true);
		this.out = new ObjectOutputStream(this.socket.getOutputStream());
		this.out.flush();
		this.in = new ObjectInputStream(this.socket.getInputStream());
		onConnection(player);
		logger.log("initiated");
	}
	
	public void onConnection(Player player) throws ClassNotFoundException, IOException {
		Payload payload = (Payload) this.in.readObject();
		this.id = ((long) payload.getData()); // set player id based on server response
		player.setId(id);
	}
	
	public long getId() {
		return this.id;
	}
	
	
	private final Object sendLock = new Object();
	
	public Payload sendAndWait(Payload payload) throws Exception {
			synchronized (sendLock) {
				pendingResponse = new CompletableFuture<>();
				send(payload);
				logger.log("sent payload -> " + payload);
				return pendingResponse.get();
			}
	}

	public void completePending(Payload payload) {
		logger.log("completing pending payload -> "+payload);
		if (pendingResponse != null && !pendingResponse.isDone()) {
			pendingResponse.complete(payload);
			logger.log("payload completed");
		}
	}
	
	
	public void serverListener(Consumer<Payload> onPayload) {
	    this.listening = true;
	    Thread listener = new Thread(() -> {
	        try {
	            while (listening) {
	                logger.log("listening to server");
	                Payload payload = read();
	                logger.log("received payload -> " + payload);

	                if (payload.getType().equals(Payloads.SERVER_SHUTDOWN)) {
	                    this.listening = false;
	                    onPayload.accept(payload);
	                    return;
	                }

	                if (payload.getType().equals(Payloads.CLIENT_DISCONNECTED)) {
	                    this.listening = false;
	                    onPayload.accept(payload);
	                    return;
	                }

	                // only complete pending for direct responses, not broadcasts
	                if (!payload.isBroadcast()) {
	                    completePending(payload);
	                }

	                onPayload.accept(payload);
	                logger.log("pending done: " + (pendingResponse != null ? pendingResponse.isDone() : "null"));
	            }
	        } catch (Exception e) {
	            e.printStackTrace();
	            if (listening) {
	                logger.log("connection to server closed due to ERROR in server listening");
	                onPayload.accept(new Payload(Payloads.SERVER_SHUTDOWN, null));
	            }
	        }
	    });
	    listener.setDaemon(true);
	    listener.start();
	}
	
	// #### DEPRECATED ####################################
	
	public void send(Payload payload) throws IOException {
		logger.log("sending payload");
		synchronized (this.out) {
			this.out.reset();
			
			/*
			ObjectOutputStream keeps an internal cache that maps every object
			it has ever written to a handle (basically an ID). When you write
			the same object again, instead of serializing the full object it 
			just writes the handle — a tiny reference saying "you already have
			this one". This is an optimization to avoid infinite loops with circular
			references and to save bandwidth when the same object is sent multiple times.
			
			this is also present server side
			*/
			
			this.out.writeObject(payload);
			this.out.flush();
		}
	}
	
	public Payload read() throws ClassNotFoundException, IOException {
		Payload payload = (Payload) this.in.readObject();
		logger.log("reading payload");
		return payload;
	}
	
	public void disconnect() {
	    try {
	    	stopServerListening();
	        this.socket.close();
	        logger.log("socket closed");
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	}
	
	public static boolean isIpValid(String ip) {
		return ip.matches("([0-9]{1,3}\\.){3}[0-9]{1,3}");
	}
	
	public void stopServerListening() {
		this.listening = false;
	}
	
	public boolean isConnected() {
		return this.socket != null && !this.socket.isClosed() && this.socket.isConnected();
	}
	
	
}
