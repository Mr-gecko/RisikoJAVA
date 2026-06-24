package model;

import java.io.Serializable;

public class Slot implements Serializable {
	
	private Player player;
	private SlotStatus status;
	
	public Slot() {
		this.player = null;
		this.status = SlotStatus.EMPTY;
	}
	
	@Override
	public String toString() {
		return "[SLOT]  |  player: " + player + "  |  status: " + status; 
	}
	
	public void setStatus(SlotStatus status) {
		this.status = status;
	}
	
	public SlotStatus getStatus() {
		return this.status;
		
	}

	public Player getPlayer() {
		return player;
	}

	public void setPlayer(Player player) {
		this.player = player;
	}
	
	public boolean isEmpty() {
		if (this.status == SlotStatus.EMPTY) {
			return true;
		}
		else {
			return false;
		}
	}
	
	
}
