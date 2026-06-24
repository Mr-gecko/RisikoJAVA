package model.network;

import java.io.Serializable;

import server.controller.ServerBaseController;

public abstract class BaseRequest implements Serializable{

	protected RequestInstruction instruction = null;
	
	public BaseRequest(RequestInstruction instruction) {
		this.instruction = instruction;
	}
	
	// make this to make work the request.handle() line in server network receiveRequest()
	public abstract void handle(ServerBaseController controller);
	
	public RequestInstruction getInstruction() {
		return this.instruction;
	}
	
}
