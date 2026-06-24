package client.view.renderers;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.stream.Collectors;

import client.view.enums.SelectionType;

public class CountryRenderer {

	private static final Color BACKGROUND_COLOR = Color.LIGHT_GRAY;
	private static final Color BORDER_COLOR = Color.BLACK;
	private static final Color HIGHLIGHT_COLOR = new Color(255, 190, 0);
	private static final Color LIGHT_HIGHLIGHT_COLOR = new Color(255, 222, 125);
	private static final Color ARMY_COLOR = Color.BLACK;
	private static final Stroke SELECTED_STROKE = new BasicStroke(3);
	private static final Stroke BASE_STROKE = new BasicStroke(2);
	
	private static final int TROOPS_CIRCLE_RADIUS = 17; // keep 17
	private static final Font TROOPS_FONT = new Font("Courier", Font.BOLD, TROOPS_CIRCLE_RADIUS - 3);

	
	private String id;
	private Shape shape;
	private Color fill;
	private Color strike;
	private Stroke stroke;
	private boolean selected;
	private SelectionType selectionType;
	
	private String name;
	
	public CountryRenderer(String id, Shape shape, Color fill, Color strike) {
		this.id = id;
		this.shape = shape;
		if (fill == null) {
			fill = BACKGROUND_COLOR;
		}
		this.fill = fill;
		if (strike == null) {
			strike = BORDER_COLOR;
		}
		this.strike = strike;
		this.selected = false;
		this.stroke = BASE_STROKE;
	}
	
	public CountryRenderer copy() {
		return new CountryRenderer(this.id, this.shape, this.fill, this.strike);	
	}
	
	@Override
	public String toString() {
		return "[country renderer]  |  id: " + this.id;
	}
	
	public Shape getShape() {
		return this.shape;
	}
	
	public String getId() {
		return this.id;
	}
	
	public Color getFill() {
		return this.fill;
	}
	
	public Color getStrike() {
		return this.strike;
	}
	
	public Stroke getStroke() {
		return this.stroke;
	}
	
	public void setFill(Color color) {
		if (color != null) {
			this.fill = color;
		} else {
			this.fill = BACKGROUND_COLOR;
		}
	}
	
	public void setStrike(Color color) {
		if (color != null) {
			this.strike = color;
		} else {
			this.strike = BORDER_COLOR;
		}
	}
	
	public void setSelected(boolean value) {
		this.selected = value;
		if (value) {
			this.stroke = SELECTED_STROKE;
			this.selectionType = SelectionType.REGULAR;
		} else {
			this.stroke = BASE_STROKE;
			this.selectionType = null;
		}
	}
	
	public void setSelectionType(SelectionType type) {
		this.selectionType = type;
	}
	
	public SelectionType getSelectionType() {
		return this.selectionType;
	}
	
	public boolean isSelected() {
		return this.selected;
	}
	
	public void selectLight() {
		setSelected(true);
		setSelectionType(SelectionType.LIGHT);
	}
	
	
	public void transform(AffineTransform at) {
	    this.shape = at.createTransformedShape(this.shape);
	}
	
	public void render(Graphics2D g2, String troopsNumber) {
		// background
		renderBackground(g2);
		
		// border
		renderBorder(g2);
		
		// army count
		renderTroops(g2, troopsNumber);
	}
	
	private static double dx = 1.01;
	
	private void renderBackground(Graphics2D g2) {
		g2.setColor(getFill());
		g2.fill(getShape());
	}
	
	private void renderBorder(Graphics2D g2) {
		g2.setColor(getStrike());
		g2.setStroke(getStroke());
		if (isSelected()) {
			switch (getSelectionType()) {
			case REGULAR -> g2.setColor(HIGHLIGHT_COLOR);
			case LIGHT -> g2.setColor(LIGHT_HIGHLIGHT_COLOR);
			}
		};
		g2.draw(getShape());
	}
	
	private void renderTroops(Graphics2D g2, String troopsNumber) {
		g2.setColor(ARMY_COLOR);
	    Point2D center = getRelativeCenter();
	    if (center == null) return;

	    FontMetrics fm = g2.getFontMetrics(TROOPS_FONT);
	    float sx = (float) (center.getX() - fm.stringWidth(troopsNumber) / 2.0);
	    float sy = (float) (center.getY() + fm.getAscent() / 2.0);

	    g2.setFont(TROOPS_FONT);
	    g2.drawString(troopsNumber, sx, sy);
	}

	public Point2D getRelativeCenter() {
	    Point2D center = findCirclePosition(this.shape, TROOPS_CIRCLE_RADIUS);
	    if (center == null) return null;

	    double cx = center.getX() + TROOPS_CIRCLE_RADIUS / 2.0;
	    double cy = center.getY() + TROOPS_CIRCLE_RADIUS / 2.0;
	    return new Point2D.Double(cx, cy);
	}
	
	/**
	 * Finds a valid top-left (x, y) to draw a circle of the given diameter
	 * fully inside the provided shape, without overflowing its borders.
	 *
	 * @param shape    the containing shape
	 * @param diameter the fixed circle diameter
	 * @return a Point2D for drawOval(x, y, diameter, diameter), or null if none found
	 */
	private static Point2D findCirclePosition(Shape shape, int diameter) {
	    double r = diameter / 2.0;
	    Rectangle2D bounds = shape.getBounds2D();
	    double cx = bounds.getCenterX();
	    double cy = bounds.getCenterY();

	    // Start at center, expand outward in a spiral
	    double step = Math.min(bounds.getWidth(), bounds.getHeight()) / 20.0;
	    step = Math.max(step, 1.0);

	    for (double dist = 0; dist <= Math.max(bounds.getWidth(), bounds.getHeight()) / 2; dist += step) {
	        // At distance 0 just check the center
	        if (dist == 0) {
	            if (circleFitsAt(shape, cx, cy, r)) return new Point2D.Double(cx - r, cy - r);
	            continue;
	        }
	        // Walk around a square ring at this distance from center
	        int steps = Math.max(8, (int) (2 * Math.PI * dist / step));
	        for (int i = 0; i < steps; i++) {
	            double angle = 2 * Math.PI * i / steps;
	            double x = cx + dist * Math.cos(angle);
	            double y = cy + dist * Math.sin(angle);
	            if (circleFitsAt(shape, x, y, r)) return new Point2D.Double(x - r, y - r);
	        }
	    }

	    return null;
	}
	
	/**
	 * Checks that the circle fits inside the shape by sampling N points
	 * around its circumference plus the center.
	 */
	private static boolean circleFitsAt(Shape shape, double cx, double cy, double r) {
	    if (!shape.contains(cx, cy)) return false;

	    int samples = 36; // one check every 10 degrees
	    for (int i = 0; i < samples; i++) {
	        double angle = 2 * Math.PI * i / samples;
	        double px = cx + r * Math.cos(angle);
	        double py = cy + r * Math.sin(angle);
	        if (!shape.contains(px, py)) return false;
	    }
	    return true;
	}
	
	
	public String getName() {
	    return Arrays.stream(this.id.split("_"))
	        .map(word -> Character.toUpperCase(word.charAt(0)) + word.substring(1))
	        .collect(Collectors.joining(" "));
	}
	
	public void reset() {
	    this.fill = BACKGROUND_COLOR;
	    this.strike = BORDER_COLOR;
	    this.selected = false;
	}
	
}
