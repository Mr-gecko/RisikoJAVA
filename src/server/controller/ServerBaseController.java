package server.controller;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import model.network.*;
import model.utils.Logger;

public abstract class ServerBaseController<T extends ServerBaseController<T>> implements Runnable {

	protected static final AtomicLong NEXT_ID = new AtomicLong(1);	
	
	protected final BlockingQueue<Request<T>> requests = new LinkedBlockingDeque<>();
	protected boolean requestHandledSuccessfully;
	
	protected boolean running;
	
	
	public abstract void submitRequest(Request<T> request);
	public abstract Payload buildResponse(BaseRequest request);	
	
	@Override
	@SuppressWarnings("unchecked")
	public void run() {
		this.running = true;
		while(this.running) {
			try {
				Request<T> request = requests.poll(500, TimeUnit.MILLISECONDS);
				if (request != null) {
					request.handle((T) this);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public void shutdown() {
		this.running = false;
	}
	
	public long nextIdGetAndIncrement() {
		return NEXT_ID.getAndIncrement();
	}
	
	protected void markSuccess() {
		this.requestHandledSuccessfully = true;
	}
	
	protected void markSuccess(boolean value) {
		this.requestHandledSuccessfully = value;
	}
	
	
	
	
}
