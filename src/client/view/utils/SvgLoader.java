package client.view.utils;

import java.awt.*;
import java.util.List;
import java.awt.geom.*;
import java.io.*;
import java.util.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;

import client.view.renderers.CountryRenderer;
import model.*;

// thanks to claude (sonnet 4.7)

public class SvgLoader {
	

	private static boolean USE_FILL_COLOR = false;
	private static boolean USE_STRIKE_COLOR = true;
	
	private Document doc;
	private String svgFilePath;
	
	public SvgLoader(String svgFilePath) {
		this.svgFilePath = svgFilePath;
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		    DocumentBuilder builder = factory.newDocumentBuilder();
		    this.doc = builder.parse(new File(this.svgFilePath));
		} catch (Exception e) {
			// error opening/parsing file 
			e.printStackTrace();
		}
	}
	
	public static Map<String, CountryRenderer> loadCountriesRenderers(InputStream inStream) throws Exception {
		Map<String, CountryRenderer> result = new LinkedHashMap<>();

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(inStream);

        NodeList paths = doc.getElementsByTagName("path");
        for (int i = 0; i < paths.getLength(); i++) {
            Element el = (Element) paths.item(i);
            String id = el.getAttribute("id");
            String d  = el.getAttribute("d");
    		String style = el.getAttribute("style");
            String fill = parseStyleProperty(style, "fill"); // fill
            String strike = parseStyleProperty(style, "strike"); // border
            Color fillColor = Color.LIGHT_GRAY;
            Color strikeColor = Color.BLACK;
            try {
            	if (fill != null) {fillColor = Color.decode(fill);}
//            	if (strike != null) {strikeColor = Color.decode(strike);}
            } catch (NumberFormatException e) {
            	e.printStackTrace();
            }
            if (!id.isEmpty()
            	&& !d.isEmpty()
            	&& fillColor!=null
            	&& strikeColor!=null) {
            	CountryRenderer renderer = new CountryRenderer(id, parsePath(d), USE_FILL_COLOR ? fillColor : null, USE_STRIKE_COLOR ? strikeColor : null);
                result.put(id, renderer);
            }
        }
        
        return result;
	}
	
	// Entry point — parses the SVG and returns id -> Shape
    public static Map<String, Shape> loadShapes(InputStream inStream) throws Exception {
        Map<String, Shape> result = new LinkedHashMap<>();

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(inStream);

        NodeList paths = doc.getElementsByTagName("path");
        for (int i = 0; i < paths.getLength(); i++) {
            Element el = (Element) paths.item(i);
            String id = el.getAttribute("id");
            String d  = el.getAttribute("d");
            if (!id.isEmpty() && !d.isEmpty()) {
                result.put(id, parsePath(d));
            }
        }
        
        return result;
    }
    
    /**
     * Parses a CSS inline style string like "fill:#bcbcbc;stroke:#000000;display:inline"
     * and returns the value for the given property, or null if not found.
     */
    private static String parseStyleProperty(String style, String property) {
        if (style == null || style.isEmpty()) return null;
        for (String part : style.split(";")) {
            String[] kv = part.trim().split(":", 2);
            if (kv.length == 2 && kv[0].trim().equals(property)) {
                return kv[1].trim();
            }
        }
        return null;
    }

    // -------------------------------------------------------
    // SVG path parser → GeneralPath
    // Handles: M m L l H h V v C c S s Q q T t A a Z z
    // -------------------------------------------------------
    private static Shape parsePath(String d) {
        GeneralPath path = new GeneralPath(GeneralPath.WIND_NON_ZERO);
        List<String> tokens = tokenize(d);
        int i = 0;

        double cx = 0, cy = 0;   // current point
        double sx = 0, sy = 0;   // last cubic control point (for S)
        double qx = 0, qy = 0;   // last quadratic control point (for T)
        char lastCmd = ' ';

        while (i < tokens.size()) {
            String token = tokens.get(i);

            // If it's a command letter, consume it
            char cmd;
            if (Character.isLetter(token.charAt(0))) {
                cmd = token.charAt(0);
                i++;
            } else {
                // Implicit repeat of last command
                // (M repeats as L, m repeats as l)
                if (lastCmd == 'M') cmd = 'L';
                else if (lastCmd == 'm') cmd = 'l';
                else cmd = lastCmd;
            }
            lastCmd = cmd;

            switch (cmd) {

                case 'M': {
                    double x = num(tokens, i++), y = num(tokens, i++);
                    path.moveTo(x, y); cx = x; cy = y;
                    break;
                }
                case 'm': {
                    double x = cx + num(tokens, i++), y = cy + num(tokens, i++);
                    path.moveTo(x, y); cx = x; cy = y;
                    break;
                }

                case 'L': {
                    double x = num(tokens, i++), y = num(tokens, i++);
                    path.lineTo(x, y); cx = x; cy = y;
                    break;
                }
                case 'l': {
                    double x = cx + num(tokens, i++), y = cy + num(tokens, i++);
                    path.lineTo(x, y); cx = x; cy = y;
                    break;
                }

                case 'H': { double x = num(tokens, i++); path.lineTo(x, cy); cx = x; break; }
                case 'h': { double x = cx + num(tokens, i++); path.lineTo(x, cy); cx = x; break; }
                case 'V': { double y = num(tokens, i++); path.lineTo(cx, y); cy = y; break; }
                case 'v': { double y = cy + num(tokens, i++); path.lineTo(cx, y); cy = y; break; }

                case 'C': {
                    double x1 = num(tokens,i++), y1 = num(tokens,i++);
                    double x2 = num(tokens,i++), y2 = num(tokens,i++);
                    double x  = num(tokens,i++), y  = num(tokens,i++);
                    path.curveTo(x1,y1, x2,y2, x,y);
                    sx = x2; sy = y2; cx = x; cy = y;
                    break;
                }
                case 'c': {
                    double x1 = cx+num(tokens,i++), y1 = cy+num(tokens,i++);
                    double x2 = cx+num(tokens,i++), y2 = cy+num(tokens,i++);
                    double x  = cx+num(tokens,i++), y  = cy+num(tokens,i++);
                    path.curveTo(x1,y1, x2,y2, x,y);
                    sx = x2; sy = y2; cx = x; cy = y;
                    break;
                }

                // S: smooth cubic — reflects last control point
                case 'S': {
                    double x1 = 2*cx - sx, y1 = 2*cy - sy;
                    double x2 = num(tokens,i++), y2 = num(tokens,i++);
                    double x  = num(tokens,i++), y  = num(tokens,i++);
                    path.curveTo(x1,y1, x2,y2, x,y);
                    sx = x2; sy = y2; cx = x; cy = y;
                    break;
                }
                case 's': {
                    double x1 = 2*cx - sx, y1 = 2*cy - sy;
                    double x2 = cx+num(tokens,i++), y2 = cy+num(tokens,i++);
                    double x  = cx+num(tokens,i++), y  = cy+num(tokens,i++);
                    path.curveTo(x1,y1, x2,y2, x,y);
                    sx = x2; sy = y2; cx = x; cy = y;
                    break;
                }

                // Q: quadratic bezier
                case 'Q': {
                    double x1 = num(tokens,i++), y1 = num(tokens,i++);
                    double x  = num(tokens,i++), y  = num(tokens,i++);
                    path.quadTo(x1,y1, x,y);
                    qx = x1; qy = y1; cx = x; cy = y;
                    break;
                }
                case 'q': {
                    double x1 = cx+num(tokens,i++), y1 = cy+num(tokens,i++);
                    double x  = cx+num(tokens,i++), y  = cy+num(tokens,i++);
                    path.quadTo(x1,y1, x,y);
                    qx = x1; qy = y1; cx = x; cy = y;
                    break;
                }

                // T: smooth quadratic — reflects last control point
                case 'T': {
                    double x1 = 2*cx - qx, y1 = 2*cy - qy;
                    double x  = num(tokens,i++), y  = num(tokens,i++);
                    path.quadTo(x1,y1, x,y);
                    qx = x1; qy = y1; cx = x; cy = y;
                    break;
                }
                case 't': {
                    double x1 = 2*cx - qx, y1 = 2*cy - qy;
                    double x  = cx+num(tokens,i++), y  = cy+num(tokens,i++);
                    path.quadTo(x1,y1, x,y);
                    qx = x1; qy = y1; cx = x; cy = y;
                    break;
                }

                // A: arc — approximate with cubic beziers
                case 'A': case 'a': {
                    double rx = num(tokens,i++), ry = num(tokens,i++);
                    double xRot     = num(tokens,i++);
                    double largeArc = num(tokens,i++);
                    double sweep    = num(tokens,i++);
                    double x = num(tokens,i++), y = num(tokens,i++);
                    if (cmd == 'a') { x += cx; y += cy; }
                    arcTo(path, cx, cy, rx, ry, xRot, largeArc!=0, sweep!=0, x, y);
                    cx = x; cy = y;
                    break;
                }

                case 'Z': case 'z':
                    path.closePath();
                    break;
            }
        }
        return path;
    }

    // -------------------------------------------------------
    // Tokenizer — splits "M10,20L30-40.5C..." into tokens
    // -------------------------------------------------------
    private static List<String> tokenize(String d) {
        List<String> tokens = new ArrayList<>();
        // Regex splits on: command letters, commas, whitespace,
        // and sign changes (e.g. 10-20 → "10" "-20")
        java.util.regex.Matcher m = java.util.regex.Pattern
            .compile("[MmLlHhVvCcSsQqTtAaZz]|[-+]?[0-9]*\\.?[0-9]+(?:[eE][-+]?[0-9]+)?")
            .matcher(d);
        while (m.find()) tokens.add(m.group());
        return tokens;
    }

    private static double num(List<String> tokens, int i) {
        return Double.parseDouble(tokens.get(i));
    }

    // -------------------------------------------------------
    // Arc approximation using cubic beziers
    // Based on the SVG spec conversion algorithm
    // -------------------------------------------------------
    private static void arcTo(GeneralPath path,
                       double x1, double y1,
                       double rx, double ry,
                       double xRotDeg,
                       boolean largeArc, boolean sweep,
                       double x2, double y2) {

        if (x1 == x2 && y1 == y2) return;
        if (rx == 0 || ry == 0) { path.lineTo(x2, y2); return; }

        double xRot = Math.toRadians(xRotDeg);
        double cosR = Math.cos(xRot), sinR = Math.sin(xRot);

        // Step 1: transform to unit circle space
        double dx = (x1 - x2) / 2, dy = (y1 - y2) / 2;
        double x1p =  cosR*dx + sinR*dy;
        double y1p = -sinR*dx + cosR*dy;

        // Step 2: find center
        double x1ps = x1p*x1p, y1ps = y1p*y1p;
        double rxs = rx*rx, rys = ry*ry;

        double sq = Math.max(0,
            (rxs*rys - rxs*y1ps - rys*x1ps) / (rxs*y1ps + rys*x1ps));
        double coef = (largeArc == sweep ? -1 : 1) * Math.sqrt(sq);

        double cxp =  coef * rx * y1p / ry;
        double cyp = -coef * ry * x1p / rx;

        double cx = cosR*cxp - sinR*cyp + (x1+x2)/2;
        double cy = sinR*cxp + cosR*cyp + (y1+y2)/2;

        // Step 3: compute angles
        double ux = (x1p - cxp) / rx, uy = (y1p - cyp) / ry;
        double vx = (-x1p - cxp) / rx, vy = (-y1p - cyp) / ry;

        double startAngle = angle(1,0, ux,uy);
        double dAngle     = angle(ux,uy, vx,vy);

        if (!sweep && dAngle > 0) dAngle -= 2*Math.PI;
        if ( sweep && dAngle < 0) dAngle += 2*Math.PI;

        // Step 4: approximate each 90° segment with a cubic bezier
        int segments = (int) Math.ceil(Math.abs(dAngle) / (Math.PI / 2));
        double delta = dAngle / segments;
        double alpha = Math.sin(delta) * (Math.sqrt(4 + 3*Math.pow(Math.tan(delta/2),2)) - 1) / 3;

        double curX = x1, curY = y1;
        double curAngle = startAngle;

        for (int s = 0; s < segments; s++) {
            double dx1 = -Math.sin(curAngle), dy1 = Math.cos(curAngle);
            double nextAngle = curAngle + delta;
            double dx2 = -Math.sin(nextAngle), dy2 = Math.cos(nextAngle);

            double endX = cx + cosR*rx*Math.cos(nextAngle) - sinR*ry*Math.sin(nextAngle);
            double endY = cy + sinR*rx*Math.cos(nextAngle) + cosR*ry*Math.sin(nextAngle);

            path.curveTo(
                curX + alpha*(cosR*rx*dx1 - sinR*ry*dy1),
                curY + alpha*(sinR*rx*dx1 + cosR*ry*dy1),
                endX - alpha*(cosR*rx*dx2 - sinR*ry*dy2),
                endY - alpha*(sinR*rx*dx2 + cosR*ry*dy2),
                endX, endY
            );

            curX = endX; curY = endY;
            curAngle = nextAngle;
        }
    }

    private static double angle(double ux, double uy, double vx, double vy) {
        double dot = ux*vx + uy*vy;
        double len = Math.sqrt((ux*ux+uy*uy)*(vx*vx+vy*vy));
        double a = Math.acos(Math.max(-1, Math.min(1, dot/len)));
        return (ux*vy - uy*vx < 0) ? -a : a;
    }    
    
}