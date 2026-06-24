package model;

import java.awt.Color;
import java.io.Serializable;

public enum PlayerColor implements Serializable{
//	BLACK(new Color(0,0,0)),
	WHITE(new Color(255,255,255)),
	GREEN(new Color(0,255,0)),
	RED(new Color(255,0,0)),
	BLUE(new Color(0,0,255)),
	YELLOW(new Color(255,255,0)),
	PURPLE(new Color(255,0,255)),
	NOT_SELECTED(Color.GRAY);
	
	private Color color;
	
	PlayerColor(Color color) {
		this.color = color;
	}
	
	public Color getColor() {
		return this.color;
	}
	
}
