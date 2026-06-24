package model.network;

import server.controller.ServerBaseController;

public class SimpleRequest extends BaseRequest {

	public SimpleRequest(RequestInstruction instruction) {
		super(instruction);
	}

	@Override
	public void handle(ServerBaseController controller) {}

}











