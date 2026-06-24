package client.controller;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

public class ClientInput {

	private final Integer button;
	private final Integer keyCode;
	private final String targetId;

	
	private ClientInput(String targetId, Integer button, Integer keyCode) {
		this.targetId = targetId;
		this.button = button;;
		this.keyCode = keyCode;
	}
	
	public static ClientInput fromMouse(String targetId, MouseEvent e) {
		return new ClientInput(targetId, e.getButton(), null);
	}
	
	public static ClientInput fromKeyboard(String targetId, KeyEvent e) {
		return new ClientInput(targetId, null, e.getKeyCode());
	}
	
	public static ClientInput fromMouseAndKeyboard(String targetId, MouseEvent me, KeyEvent ke) {
		return new ClientInput(targetId, me.getButton(), ke.getKeyCode());
	}
	
	public boolean isMouse() {return button != null;}
	public boolean isKeyboard() {return keyCode != null;}
	
	public String getTargetId() {return targetId;}
	public Integer getButton() {return button;}
	public Integer getKeyCode() {return keyCode;}
	
}
