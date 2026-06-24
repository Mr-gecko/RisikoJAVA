package client.controller;

import client.network.ClientNetwork;
import model.network.Payload;
import model.network.RequestInstruction;

public abstract class ClientBaseController {

	protected final ClientNetwork clientNetwork;
	
	public ClientBaseController(ClientNetwork clientNetwork) {
		this.clientNetwork = clientNetwork;
	}
	
	public abstract Payload sendRequestAndWait(RequestInstruction instruction);

	protected boolean isResponseValid(Payload payload) {
		if (payload == null
			|| payload.getData() == null) {
			return false;
		}
		return true;
	}
	
}
