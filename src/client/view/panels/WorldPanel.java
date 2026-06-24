package client.view.panels;

import javax.swing.JPanel;
import javax.swing.Renderer;

import client.controller.ClientGameController;
import client.controller.ClientInput;
import client.view.renderers.CountryRenderer;
import client.view.utils.SvgLoader;
import model.Game;
import model.PlayerColor;
import server.model.ServerGameModel;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.beans.DesignMode;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class WorldPanel extends JPanel implements MouseListener{

	private static final Color LINE_COLOR = Color.ORANGE;
	private static final Color ROAD_COLOR = Color.BLACK;
	private static final BasicStroke LINE_STROKE = new BasicStroke(3);
	private static final BasicStroke ROAD_STROKE = new BasicStroke(3);

	private long lastClickTime = 0;
	private static final long CLICK_COOLDOWN_MS = 75;
	
	// country_id | shape
	private LinkedHashMap<String, CountryRenderer> baseCountryRenderers;
	private LinkedHashMap<String, CountryRenderer> countryRenderers;
	
	private Supplier<Game> snapshotSupplier;
	private GameOptionsPanel gameActionsPanel;
	private Consumer<ClientInput> onCountryClicked;
	private CountryRenderer cachedRenderer;
	private boolean showAdjacents = false;
	private boolean orderSourceSelected = false;
	
	
	
	public WorldPanel(GameOptionsPanel gameActionsPanel, Supplier<Game> snapshotSupplier) throws Exception {
		this.snapshotSupplier = snapshotSupplier;
		this.gameActionsPanel = gameActionsPanel;
		
		setBackground(new Color(0, 128, 255));
		
		setup();
		
		this.addMouseListener(this);
	}
	
	private void setup() throws Exception {
		if (this.baseCountryRenderers == null) {
			InputStream svgStream = WorldPanel.class.getResourceAsStream("/client/resources/map.svg");
			this.baseCountryRenderers = (LinkedHashMap<String, CountryRenderer>) SvgLoader.loadCountriesRenderers(svgStream);
			svgStream.close();
		}
		this.countryRenderers = new LinkedHashMap<String, CountryRenderer>();
		for (Map.Entry<String, CountryRenderer> entry :  this.baseCountryRenderers.entrySet()) {
			this.countryRenderers.put(entry.getKey(), entry.getValue().copy());
		}
		
		// trace roads
		
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (snapshotSupplier == null || snapshotSupplier.get() == null) return;
		Graphics2D g2 = (Graphics2D)g;
		g2.transform(createTransform(findSvgMapBounds(), getWidth(), getHeight()));
	
//		traceRoads(g2);
		
		for (CountryRenderer renderer : this.countryRenderers.values()) {
			if (!renderer.isSelected()) {
				int troops = this.snapshotSupplier.get().getWorld().getCountry(renderer.getId()).getTroops();
				renderer.render(g2, String.valueOf(troops));
			}
		}
		
		for (CountryRenderer renderer : this.countryRenderers.values()) {
			if (renderer.isSelected()) {
				int troops = this.snapshotSupplier.get().getWorld().getCountry(renderer.getId()).getTroops();
				renderer.render(g2, String.valueOf(troops));
			}
		}
		
		if (this.showAdjacents) {
			traceAdjacent(g2, this.cachedRenderer);
		}
		
	}
	
	public GameOptionsPanel getActionPanel() {
		return this.gameActionsPanel;
	}
	
	private AffineTransform createTransform(Rectangle2D worldBounds, double panelWidth, double panelHeight) {

	    double sx =
	        panelWidth / worldBounds.getWidth();

	    double sy =
	        panelHeight / worldBounds.getHeight();

	    double scale = Math.min(sx, sy);

	    double tx =
	        (panelWidth
	            - worldBounds.getWidth() * scale) / 2.0;

	    double ty =
	        (panelHeight
	            - worldBounds.getHeight() * scale) / 2.0;

	    AffineTransform at =
	        new AffineTransform();

	    // Center inside panel
	    at.translate(tx, ty);

	    // Scale
	    at.scale(scale, scale);

	    // Move world origin
	    at.translate(
	        -worldBounds.getX(),
	        -worldBounds.getY()
	    );

	    return at;
	}
	
	private Rectangle2D findSvgMapBounds() {

        Rectangle2D bounds = null;

        for (CountryRenderer renderer : this.countryRenderers.values()) {
        	Shape s = renderer.getShape();
            if (bounds == null) {
                bounds = s.getBounds2D();
            } else {
                bounds = bounds.createUnion(
                    s.getBounds2D()
                );
            }
        }

//        return bounds;
        
        double margin = 35.;
        
        return new Rectangle2D.Double(
                bounds.getX() - margin,
                bounds.getY() - margin,
                bounds.getWidth() + margin * 2,
                bounds.getHeight() + margin * 2
            );
        
    }
	
	private Point2D screenToWorld(Point2D screenPoint) {
		AffineTransform at = createTransform(findSvgMapBounds(), getWidth(),  getHeight());
		AffineTransform atInverse;
		try {
			atInverse = at.createInverse();
			Point2D worldPoint = new Point2D.Double();
			atInverse.transform(screenPoint, worldPoint);
			return worldPoint;
		} catch (NoninvertibleTransformException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private void deselectAll() {
		this.countryRenderers.values()
		.stream()
		.forEach(renderer -> renderer.setSelected(false));
	}
	
	private CountryRenderer getRendererFromPoint(Point2D point) {
		return this.countryRenderers.values()
		.stream()
		.filter(renderer -> renderer.getShape().contains(point))
		.findFirst()
		.orElse(null);
	}
	
	private CountryRenderer getRenderer(String renderId) {
		return this.countryRenderers.get(renderId);
	}
	
	public void seaClick() {
		deselectAll();
		setShowAdjacents(false);
		cacheRenderer(null);
		setOrderSourceSelected(false);
		repaint();
	}
	
	public void highlightContinent(String rendererId) {
		Game game = this.snapshotSupplier.get();
		String continentId = game.getWorld().getCountry(rendererId).getContinentId();
		game.getWorld().getCountries().values().stream()
			.forEach(country -> {
				if (country.getContinentId().equals(continentId)
						&& !country.getId().equals(rendererId)) {
					getRenderer(country.getId()).selectLight();
				}
			});
	}
	
	public void infoClick(String rendererId) {
		if (getCachedRenderer() != null
				&& getCachedRenderer().getId().equals(rendererId)) {
			deselectAll();
			cacheRenderer(null);
			setShowAdjacents(false);
			repaint();
			return;
		}
		deselectAll();
		CountryRenderer renderer = getRenderer(rendererId);
		if (renderer != null) {
			renderer.setSelected(true);
			highlightContinent(rendererId);
			cacheRenderer(renderer);
			setShowAdjacents(true);
			repaint();
		}
	}
	
	public void deployClick(String rendererId) {
		deselectAll();
		CountryRenderer renderer = getRenderer(rendererId);
		if (renderer != null) {
			renderer.setSelected(true);
			cacheRenderer(renderer);
			setShowAdjacents(false);
			repaint();
		}
	}
	
	public void orderClick(String rendererId) {
		if (rendererId == null) {
			return;
		}
		CountryRenderer renderer = getRenderer(rendererId);
		if (renderer == null) {
			return;
		}
		if (this.cachedRenderer == null) { // first click
			deselectAll();
			cacheRenderer(renderer);
			this.cachedRenderer.setSelected(true);
			setOrderSourceSelected(true);
			setShowAdjacents(true);
			repaint();
		} else { // second click
			seaClick();
			getRenderer(rendererId).setSelected(true);
			repaint();
		}
	}
	
	private void eraseAdjacent() {
		setShowAdjacents(false);
		repaint();
	}
	
	private void traceAdjacent(Graphics2D g2, CountryRenderer renderer) {
		this.snapshotSupplier.get().getWorld().getBorders(renderer.getId())
				.stream()
				.forEach(borderId -> {
					CountryRenderer destinationRenderer = this.countryRenderers.get(borderId);
					traceArrow(g2, renderer, destinationRenderer);
				});
	}
	
	private void traceRoads(Graphics2D g2) {
		this.countryRenderers.values().stream()
			.forEach(renderer -> {
				this.snapshotSupplier.get().getWorld().getBorders(renderer.getId())
				.stream()
				.forEach(borderId -> {
					CountryRenderer destinationRenderer = this.countryRenderers.get(borderId);
					traceRoad(g2, renderer, destinationRenderer);
				});
			});
	}
	
	private void traceRoad(Graphics2D g2, CountryRenderer r1, CountryRenderer r2) {
		Point2D start = r1.getRelativeCenter();
		Point2D end = r2.getRelativeCenter();
		g2.setColor(ROAD_COLOR);
		g2.setStroke(ROAD_STROKE);
		g2.drawLine((int)start.getX(), (int)start.getY(), (int)end.getX(), (int)end.getY());
	}
	
	private void traceLine(Graphics2D g2, CountryRenderer r1, CountryRenderer r2) {
		Point2D start = r1.getRelativeCenter();
		Point2D end = r2.getRelativeCenter();
		g2.setColor(LINE_COLOR);
		g2.setStroke(LINE_STROKE);
		g2.drawLine((int)start.getX(), (int)start.getY(), (int)end.getX(), (int)end.getY());
	}
	
	private void traceArrow(Graphics2D g2, CountryRenderer r1, CountryRenderer r2) {
	    Point2D start = r1.getRelativeCenter();
	    Point2D end = r2.getRelativeCenter();

	    g2.setColor(LINE_COLOR);
	    g2.setStroke(LINE_STROKE);
	    g2.drawLine((int)start.getX(), (int)start.getY(), (int)end.getX(), (int)end.getY());

	    // arrow head
	    double dx = end.getX() - start.getX();
	    double dy = end.getY() - start.getY();
	    double angle = Math.atan2(dy, dx);
	    double arrowSize = 15;

	    int tipX = (int)end.getX();
	    int tipY = (int)end.getY();

	    int leftX = (int)(tipX - arrowSize * Math.cos(angle - Math.PI / 6));
	    int leftY = (int)(tipY - arrowSize * Math.sin(angle - Math.PI / 6));
	    int rightX = (int)(tipX - arrowSize * Math.cos(angle + Math.PI / 6));
	    int rightY = (int)(tipY - arrowSize * Math.sin(angle + Math.PI / 6));

	    g2.fillPolygon(new int[]{tipX, leftX, rightX}, new int[]{tipY, leftY, rightY}, 3);
	}
	
	
	
	private void updateRenderers() {
		Game game = snapshotSupplier.get();
		this.snapshotSupplier.get().getWorld().getCountries().values()
			.stream()
			.forEach(country -> {
				CountryRenderer renderer = getRenderer(country.getId());
				if (renderer != null) {
					PlayerColor owner = country.getOwner();
					renderer.setFill(owner != null ? owner.getColor() : null);
					
					String continentId = country.getContinentId();
//					renderer.setStrike(renderer.getFill());
				}
			});
	}
	
	public void update() {
		updateRenderers();
		repaint();
	}
	
	public void reset() {
		this.cachedRenderer = null;
		this.showAdjacents = false;
		this.countryRenderers.values().forEach(CountryRenderer::reset); // reset colors
		repaint();
	}
	
	
	public void setInteractable(boolean value) {
	    if (value) {
	        removeMouseListener(this); // remove first to avoid duplicates
	        addMouseListener(this);
	        setCursor(Cursor.getDefaultCursor());
	    } else {
	        removeMouseListener(this);
//	        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
	    }
	}
	
	public void setOnCountryClicked(Consumer<ClientInput> onCountryClicked) {
	    this.onCountryClicked = onCountryClicked;
	}
	
	private void cacheRenderer(CountryRenderer renderer) {
		this.cachedRenderer = renderer;
	}
	
	private void setShowAdjacents(boolean value) {
		this.showAdjacents = value;
	}
	
	private void setOrderSourceSelected(boolean value) {
		this.orderSourceSelected = value;
	}
	
	public void setSnapshotSupplier(Supplier<Game> supplier) {
	    this.snapshotSupplier = supplier;
	}
	
	public boolean hasOrderSource() {
		return this.orderSourceSelected;
	}
	
	public CountryRenderer getCachedRenderer() {
		return this.cachedRenderer;
	}
	
	public CountryRenderer getCountryRenderer(String id) {
		return this.countryRenderers.get(id);
	}
	
	
	public void setSpectatorMode(boolean value) {
		setInteractable(!value);
	}
	
	
	@Override
	public void mousePressed(MouseEvent e) {
		
		// cooldown
		long now = System.currentTimeMillis();
	    if (now - lastClickTime < CLICK_COOLDOWN_MS) return;
	    lastClickTime = now;
		
		Point2D worldPoint = screenToWorld(e.getPoint());
		CountryRenderer renderer = getRendererFromPoint(worldPoint);
		if (renderer != null
	        && this.onCountryClicked != null) {
			this.onCountryClicked.accept(ClientInput.fromMouse(renderer.getId(), e));
		} else {
			this.onCountryClicked.accept(ClientInput.fromMouse(null, e));
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
	
	

}
