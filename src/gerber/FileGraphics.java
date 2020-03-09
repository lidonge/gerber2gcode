package gerber;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Stroke;

public class FileGraphics implements IGraphics {
	protected int ppi;
	protected String filename;
	protected int border;
	protected int clipX, clipY, clipWidth,clipHeight;
	protected Graphics2D g2d;
	protected boolean mirroVertical = false;
	protected boolean mirroHorizontal = false;
	protected int moveX=0,moveY=0;
	
	public FileGraphics(String filename,int ppi,double bd) {
		this.filename = filename;
		this.ppi = ppi;
		this.border = (int)Math.round(bd*ppi);
	}

	public void setGraphics(Graphics2D g) {
		this.g2d = g;
	}
	
	@Override
	public int getBorder() {
		return this.border;
	}

	@Override
	public int getPPI() {
		return this.ppi;
	}
	
	public void initGraphics(int x, int y, int w, int h) {
		this.clipX = x;
		this.clipY = y;
		this.clipWidth = w;
		this.clipHeight = h;
	}
	
	protected int convertY(int y, int height) {
		int ret = y + border - clipY + moveY;
		if(mirroVertical)
			ret = this.clipHeight+clipY-(border+y+height)-moveY;
		return ret;
	}
	
	protected int convertX(int x, int width) {
		int ret = x + border-clipX + moveX;
		if(mirroHorizontal)
			ret = this.clipWidth+clipX-(border+x+width)-moveX;
		return ret;
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
		this.g2d.fillOval(convertX(x,width), convertY(y,height), width, height);
	}

	@Override
	public void fillRect(int x, int y, int width, int height) {
		this.g2d.fillRect(convertX(x,width), convertY(y,height), width, height);
	}

	@Override
	public void fillPolygon(Polygon p) {
		int[] ypos = new int[p.npoints];
		int[] xpos = new int[p.npoints];
		
		for(int i = 0;i<p.npoints;i++) {
			xpos[i] = this.convertX(p.xpoints[i],0);
			ypos[i] = this.convertY(p.ypoints[i],0);
		}
		this.g2d.fillPolygon(xpos,ypos,p.npoints);
	}

	@Override
	public void drawLine(int x1, int y1, int x2, int y2) {
		this.g2d.drawLine(convertX(x1,0), this.convertY(y1,0), convertX(x2,0), this.convertY(y2,0));
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
	
	@Override
	public void drawLocatingHole(double diameter) {
		drawLocatingHole(diameter,false);
	}
	
	protected void drawLocatingHole(double diameter,boolean move) {
		int d = (int)Math.round(diameter * ppi);
		int r = (int)Math.round(diameter * ppi/2);
		int x1 = move ? 0 + moveX : 0;
		int y1 = move ? 0 + moveY : 0;
		int x2 = move ? border*2+clipWidth - d + moveX: border*2+clipWidth - d; 
		int y2 = move ? 0 + moveY : 0;
		int x3 = move ? border*2+clipWidth -d + moveX : border*2+clipWidth -d;
		int y3 = move ? border*2+clipHeight -d + moveY : border*2+clipHeight -d;
		int x4 = move ? 0 + moveX : 0;
		int y4 = move ? border*2+clipHeight -d + moveY : border*2+clipHeight -d;
		g2d.fillOval(x1,y1,d,d);
		g2d.fillOval(x2,y2,d,d);
		g2d.fillOval(x3,y3,d,d);
		g2d.fillOval(x4,y4,d,d);
	}
}
