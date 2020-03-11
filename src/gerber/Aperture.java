//import java.awt.*;
package gerber;

import java.awt.geom.Point2D;

import gerber.GerberBoard.GPolygon;

public class Aperture {

	protected int ppi;
	private String type;
	private float[] modarray;
	public int drillTool = 0;

	public Aperture(int ppi, String type, float[] modarray) {
		this.ppi = ppi;
		this.type = type;
		this.modarray = modarray;
	}


	public void draw(IGerberBoard myg, int x, int y) {
		this.draw(myg, x, y, false);
	}

	public void drawLine(IGerberBoard myg, int startx, int starty, int endx, int endy) {
		drawLine(myg, startx, starty, endx,endy,false);
	}
	public void drawLine(IGerberBoard myg, int startx, int starty, int endx, int endy, boolean inverted) {
		if (this.type.equals("C")) { // draw circle
			
			int diameter =  (int)Math.round((double)this.modarray[0]*(double)this.ppi);				
			myg.line(startx , starty, endx , endy, diameter, inverted);
		}
	}
	public void draw(IGerberBoard myg, int x, int y, boolean inverted) {

		if (this.type.equals("C")) { // draw circle
			
			int diameter =  (int)Math.round((double)this.modarray[0]*(double)this.ppi);
			
			int xx = x - (int)Math.round(diameter/2.0);
			int yy = y - (int)Math.round(diameter/2.0);
				
			myg.circle(xx, yy, diameter, drillTool, modarray[0], inverted);
		}
		
		if (this.type.equals("R")) { // draw rectangle
			int width = (int)Math.round((double)this.modarray[0]*(double)this.ppi);
			int height = (int)Math.round((double)this.modarray[1]*(double)this.ppi);
			
			int xx = x - (int)Math.round(width/2.0);
			int yy = y - (int)Math.round(height/2.0);
						
			myg.rect(xx, yy, width, height);
		}

		if (this.type.equals("O")) { // draw oval
			
			double w = (double)this.modarray[0]*(double)this.ppi;
			double h = (double)this.modarray[1]*(double)this.ppi;
			
			boolean upright = true;
			if (w > h) upright = false;
			
			if (upright) {
				int diameter = (int)Math.round(w);
				int half_diameter = (int)Math.round(w/2.0);
				int height = (int)Math.round(h);
				int half_height = (int)Math.round(h/2.0);
				
				myg.circle(x-half_diameter, y-half_height, diameter);
				myg.circle(x-half_diameter, y+half_height-diameter, diameter);
				myg.circle(x-half_diameter, y-half_diameter, diameter);
			} else {
				int diameter = (int)Math.round(h);
				int half_diameter = (int)Math.round(h/2.0);
				int width = (int)Math.round(w);
				int half_width = (int)Math.round(w/2.0);
			
				myg.circle(x-half_width, y-half_diameter, diameter);
				myg.circle(x+half_width-diameter, y-half_diameter, diameter);
				myg.circle(x-half_diameter, y-half_diameter, diameter);
			}
		}
		
		
	}
	
	
}

class PolygonAperture extends Aperture{
	private int exposure;
	private Point2D start;
	private Point2D[] polys;
	
	public PolygonAperture(int ppi, String type, float[] modarray, Point2D start, Point2D[] polys, int exposure) {
		super(ppi, type, modarray);
		this.exposure = exposure;
		this.start = start;
		this.polys = polys;
	}

	public void draw(IGerberBoard myg, int x, int y, boolean inverted) {
		GPolygon p = new GPolygon();
		for(int i = 0;i< polys.length;i++) {
			p.addPoint(x + (int)Math.round(polys[i].getX()*(double)this.ppi), 
					y + (int)Math.round(polys[i].getY()*(double)this.ppi));
			
		}
		
		myg.polygon(p);
	}
}