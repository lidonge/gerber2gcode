//import java.awt.*;
package gerber;

import java.awt.geom.Point2D;

import gerber.GerberBoard.GPolygon;

public class Aperture {

	protected int ppi;
	private String type;
	private float[] modarray;
//	private static int circlecount = 0;
//	private static int olvcount = 0;
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
//			circlecount++;
			
			int diameter =  (int)Math.round((double)this.modarray[0]*(double)this.ppi);
//			System.out.println("Drawing line at x,y ["+startx+","+starty+","+endx+","+endy+","+diameter+"]");
			//System.out.println("diameter: "+diameter);
			int r = (int)Math.round(diameter/2.0);

				
			myg.line(startx , starty, endx , endy, diameter, inverted);
		}
	}
	public void draw(IGerberBoard myg, int x, int y, boolean inverted) {

		if (this.type.equals("C")) { // draw circle
//			circlecount++;
//			System.out.println(circlecount+":Drawing CIRCLE at x,y ["+x+","+y+"]");
			
			int diameter =  (int)Math.round((double)this.modarray[0]*(double)this.ppi);
			//System.out.println("diameter: "+diameter);
			
			int xx = x - (int)Math.round(diameter/2.0);
			int yy = y - (int)Math.round(diameter/2.0);
				
			myg.circle(xx, yy, diameter, inverted);
		}
		
		if (this.type.equals("R")) { // draw rectangle
			//System.out.println("Drawing RECTANGLE at x,y ["+x+","+y+"]");
			
			int width = (int)Math.round((double)this.modarray[0]*(double)this.ppi);
			int height = (int)Math.round((double)this.modarray[1]*(double)this.ppi);
			
			int xx = x - (int)Math.round(width/2.0);
			int yy = y - (int)Math.round(height/2.0);
						
			myg.rect(xx, yy, width, height);
		}

		if (this.type.equals("O")) { // draw oval
//			olvcount++;
//			System.out.println(olvcount+":Drawing OVAL at x,y ["+x+","+y+"]");
			
//			double min = Math.min(this.modarray[0], this.modarray[1]);
//			double max = Math.max(this.modarray[0], this.modarray[1]);
//			
//			int diameter = (int)Math.round((double)min*(double)this.ppi);
//			int width = (int)Math.round((double)max*(double)this.ppi);
			
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
			
			
			//int xx = x - (int)Math.round(width/2.0);
			//int yy = y - (int)Math.round(height/2.0);
						
			//g2d.fillOval(offsetx+xx, offsety+yy, width, height);
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
//		String s = "";
		for(int i = 0;i< polys.length;i++) {
//			s += "("+polys[i].getX() +","+polys[i].getY()+")"; 
			p.addPoint(x + (int)Math.round(polys[i].getX()*(double)this.ppi), 
					y + (int)Math.round(polys[i].getY()*(double)this.ppi));
//			s += "("+p.xpoints[i] +","+p.ypoints[i]+")"; 
			
		}
//		System.out.println("Draw Polygon:" + s);
		
		myg.polygon(p);
	}
}