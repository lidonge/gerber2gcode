package gerber;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Stroke;
import java.util.Arrays;

public abstract class FileGraphics implements IGraphics {
	protected boolean negative;
	protected int ppi;
	protected String filename;
	protected int border;
	protected int clipWidth,clipHeight;
	protected Graphics2D g2d;
	
	public FileGraphics(String filename,int ppi,int border, boolean negative) {
		this.filename = filename;
		this.ppi = ppi;
		this.negative = negative;
		this.border = border;
	}

	public void setGraphics(Graphics2D g) {
		this.g2d = g;
	}
	
	@Override
	public int getBorder() {
		return this.border;
	}

	@Override
	public boolean isNegative() {
		return this.negative;
	}
	
	@Override
	public int getPPI() {
		return this.ppi;
	}
	
	public void initGraphics(int w, int h) {
		this.clipWidth = w;
		this.clipHeight = h;
	}
	
	protected int convertY(int y, int height) {
		return this.clipHeight-(border+y+height);
//		return y;
	}
	
	public void paintBackGround() {
		if (negative) {
 			g2d.setColor(Color.white);
			g2d.fillRect(0, 0, clipWidth, clipHeight);
 			g2d.setColor(Color.black);
		} else {
 			g2d.setColor(Color.red);
		}
	}
	/**
	 * ==================for graphics************************
	 */
	@Override
	public void setColor(Color c) {
		this.g2d.setColor(c);
	}

	@Override
	public Stroke getStroke() {
		return this.g2d.getStroke();
	}

	@Override
	public void setStroke(Stroke s) {
		this.g2d.setStroke(s);
	}

	@Override
	public final void fillOval(int x, int y, int width, int height) {
		this.g2d.fillOval(x, convertY(y,height), width, height);
	}

	@Override
	public void fillRect(int x, int y, int width, int height) {
		this.g2d.fillRect(x, convertY(y,height), width, height);
	}

	@Override
	public void fillPolygon(Polygon p) {
		int[] ypos = new int[p.npoints];
		
		for(int i = 0;i<p.npoints;i++)
			ypos[i] = this.convertY(p.ypoints[i],0);
		this.g2d.fillPolygon(p.xpoints,ypos,p.npoints);
	}

	@Override
	public void drawLine(int x1, int y1, int x2, int y2) {
		this.g2d.drawLine(x1, this.convertY(y1,0), x2, this.convertY(y2,0));
	}

	@Override
	public void drawPolyLine(IPolyLine pl) {
		for(int j = 0;j<pl.lines();j++) {
			ILine l = pl.get(j);
			Stroke s = g2d.getStroke();
			g2d.setStroke(new BasicStroke(l.getThick()));
			this.drawLine(l.getStartX(),l.getStartY(),l.getEndX(),l.getEndY());
			g2d.setStroke(s);
		}
	}
	public void dispose() {
		g2d.dispose();
	}
}
