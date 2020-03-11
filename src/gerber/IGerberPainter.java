package gerber;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Stroke;

public interface IGerberPainter {
	public void initGraphics(int x, int y, int w,int h);
	public int getPPI();
	public int getBorder();
	public void dispose();
	public void setColor(Color c);
	public void setGraphics(Graphics2D g);
	public void fillOval(int x, int y, int width, int height);
	public void fillRect(int x, int y, int width, int height);
	public void fillPolygon(Polygon p);
	public Stroke getStroke();
	public void setStroke(Stroke s);
	public void drawLine(int x1, int y1, int x2, int y2);
	public void drawPolyLine(IPolyLine pl);
	public void drawLocatingHole(double diameter);
}
