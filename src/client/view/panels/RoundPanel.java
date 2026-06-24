package client.view.panels;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JPanel;

public class RoundPanel extends JPanel {
	
	private static final int STANDARD_CORNER_RADIUS = 15;
	private static final Color STANDARD_BACKGROUND_COLOR = new Color(240,240,240, 150);

	private int cornerRadius;
	private Color color;
	
	public RoundPanel(int cornerRadius, Color color) {
		this.cornerRadius = cornerRadius;
		this.color = color;
		this.setOpaque(false);
	}
	
	public RoundPanel() {
		this(STANDARD_CORNER_RADIUS, STANDARD_BACKGROUND_COLOR);
	}
	
	public RoundPanel(int cornerRadius) {
		this(cornerRadius, STANDARD_BACKGROUND_COLOR);
	}
	
	public RoundPanel(Color color) {
		this(STANDARD_CORNER_RADIUS, color);
	}
	
	public void setCornerRadius(int cornerRadius) {
		this.cornerRadius = cornerRadius;
		repaint();
	}
	
	public void setColor(Color color) {
		this.color = color;
		repaint();
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D)g.create();
		g2.setColor(this.color);
		g2.setRenderingHint(
	            RenderingHints.KEY_ANTIALIASING,
	            RenderingHints.VALUE_ANTIALIAS_ON);
		g2.fillRoundRect(0,0,getWidth(),getHeight(),this.cornerRadius,this.cornerRadius);
		g2.dispose();
	}
	
	
	public static RoundPanel transparentOne() {
		return new RoundPanel(15, new Color(0,0,0,0));
	}
	
	public void setTransparent() {
		setColor(new Color(0,0,0,0));
	}

	
}
