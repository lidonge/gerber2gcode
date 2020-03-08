package gerber;
import java.util.*;
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;
import javax.imageio.metadata.*;
import javax.imageio.stream.*;

import tools.IComparable;
import tools.IGeoComparable;
import tools.SortedArray;

import java.awt.geom.Point2D;

public class GerberBoard implements IGerberBoard{
	private int imgw = 0;
	private int imgh = 0;


	private LinkedList operations = new LinkedList();

	public GerberBoard() {
	}


	public static class Circle{
		public int x;
		public int y;
		public int diameter;
		public boolean inverted = false;
	
		public Circle(int x, int y, int diameter) {
			this.x = x;
			this.y = y;
			this.diameter = diameter;
		}
	
	}

	public static class Rect{
		public int x;
		public int y;
		public int width;
		public int height;
	
		public Rect(int x, int y, int width, int height) {
			this.x = x;
			this.y = y;
			this.width = width;
			this.height = height;
		}
	}

	public static class EndPoint implements IGeoComparable{
		private ArrayList owners = new ArrayList(1);
		private boolean bSorted = false;
		public int x,y;
		public Rectangle block;
		public EndPoint(Object owner, int x, int y) {
			this.x = x;
			this.y = y;
			this.owners.add(owner);
		}
		public EndPoint(Object owner,int x, int y, int w, int h) {
			this(owner,x,y);
			this.setBlock(w, h);
		}
		
		public void addOwner(Object o) {
			owners.add(o);
		}
		
		public void removeOwner(Object o) {
			this.owners.remove(o);
		}
		
		public Object[] owners() {
			return  owners.toArray();
		}
		
		public int indexOfOwner(Object owner) {
			return owners.indexOf(owner);
		}
		
		public int sizeOfOwner() {
			return owners.size();
		}
		public Object getOwner(int index) {
			return owners.get(index);
		}
		
		public void sortOwner(boolean force) {
			if(force || !bSorted) {
				bSorted = true;
				owners.sort(null);
			}
		}
	
		@Override
		public int compareTo(Object target) {
			EndPoint p = (EndPoint)target;
			int ret = 0;
			if(block.y > p.block.y) {
				ret = 1;
			}else if(block.y < p.block.y) {
				ret = -1;
			}else if(block.x > p.block.x) {
				ret = 1;
			}else if(block.x < p.block.x) {
				ret = -1;
			}else {
				if(x > p.x) {
					ret = 1;
				}else if(x < p.x) {
					ret = -1;
				}else if(y > p.y) {
					ret = 1;
				}else if(y < p.y) {
					ret = -1;
				}
			}
			return ret;
		}
	
		@Override
		public void setBlock(int width, int height) {
			int px = x / width;
			int py = y / height;
			block = new Rectangle(width * px, height * py , width,height);
		}
	}

	public static class PolyLine implements IPolyLine{
	    /*
	     * Default length for xpoints and ypoints.
	     */
	    private static final int MIN_LENGTH = 4;
		private ArrayList<Line> lines = new ArrayList<>();
		public PolyLine() {
		}
		
		public void addOnly(Line l) {
			lines.add(l);
		}
		public void add(Line l, boolean header) {
			int size = lines.size();
			if(size == 0) {
				lines.add(l);
			}else {
				if(header) {
					Line line = lines.get(0);
					if(l.sameStart(line) ) {
						l.roll();
					}else if(l.sameEnd(line)) {
						line.roll();
					}else if(l.getStartX() == line.getEndX() && l.getStartY() == line.getEndY()){
						l.roll();
						line.roll();
						
					}
					lines.add(0, l);
				}else {
					Line line = lines.get(size -1);
					if(l.sameEnd(line)) {
						l.roll();
					}else if(l.sameStart(line)) {
						line.roll();
					}else if(line.getStartX() == l.getEndX() && line.getStartY() == l.getEndY()){
						l.roll();
						line.roll();
						
					}
					lines.add(l);
				}
			}
		
//			System.out.println(lines.size()+"========add a line:" +header);
		}
		
		@Override
		public int lines() {
			return this.lines.size();
		}

		@Override
		public ILine get(int index) {
			return this.lines.get(index);
		}
	}

	public static class Line implements IComparable,ILine{
		public int startx,starty,endx,endy, thick;
		public Line(int startx, int starty, int endx, int endy, int thick) {
			this.startx = startx;
			this.starty = starty;
			this.endx = endx;
			this.endy = endy;
			this.thick = thick;
		}

		public void roll() {
			int tmp = startx;
			startx = endx;
			endx = tmp;
			
			tmp = starty;
			starty = endy;
			endy = tmp;
		}
		
		public boolean sameStart(Line l) {
			return l.startx == startx && l.starty == starty;
		}

		public boolean sameEnd(Line l) {
			return l.endx == endx && l.endy== endy;
		}

		public boolean isCoincide(Line l) {
			boolean ret = (
					(startx == l.startx && starty == l.starty && endx == l.endx && endy == l.endy)  ||
					(startx == l.endx && starty == l.endy && endx == l.startx && endy == l.starty) 
					);
			return ret;
		}
		
		public void revert() {
			int t = startx;	startx = endx; endx = t;
			
			t = starty; starty = endy; endy = t;
		}

		@Override
		public int compareTo(Object o) {
			return this.thick - ((Line)o).thick;
		}
		public String toString() {
			return "(" +startx + "," + starty+","+endx+","+endy +")"; 
		}

		@Override
		public int getStartX() {
			return this.startx;
		}

		@Override
		public int getStartY() {
			return this.starty;
		}

		@Override
		public int getEndX() {
			return this.endx;
		}

		@Override
		public int getEndY() {
			return this.endy;
		}

		@Override
		public int getThick() {
			return this.thick;
		}

		@Override
		public double getK() {
			return ((double)(starty-endy))/(startx - endx);
		}
	}

	public void clear(){
		operations.clear();
	}


	public void circle(int x, int y, int diameter){
		this.operations.add(new Circle(x,y,diameter));
	}

	public void circle(int x, int y, int diameter, boolean inverted){
		Circle mc = new Circle(x,y,diameter);
		mc.inverted = inverted;
		this.operations.add(mc);
	}


	public void rect(int x, int y, int width, int height) {
		this.operations.add(new Rect(x,y,width,height));
	}

	public void polygon( Polygon p) {
		this.operations.add(p);
	}

	public void line( int startx, int starty, int endx, int endy, int thick, boolean inverted) {
		if(startx == endx && starty == endy) {
			System.out.println("Worning: line is a point at " +startx + ","+starty +"!");
		}else
			this.operations.add(new Line(startx,starty,endx,endy,thick));
	}

	private void findDimensions(int border) {
		int highX = 0;
		int highY = 0;

		for (Object o: operations) {
			if (o instanceof Circle) {
				Circle c = (Circle)o;
				int xmax = c.x+c.diameter;
				if (xmax > highX) highX = xmax;
				int ymax = c.y+c.diameter;
				if (ymax > highY) highY = ymax;

			} else 
			if (o instanceof Rect) {
				Rect r = (Rect)o;
				int xmax = r.x+r.width;
				if (xmax > highX) highX = xmax;
				int ymax = r.y+r.height;
				if (ymax > highY) highY = ymax;
			}

		}

		if (imgw == 0 && imgh == 0) {
			this.imgw = highX + 2 * border;
			this.imgh = highY + 2 * border;
		}	
	}

	private void sortAndMerge(SortedArray sortedArea, SortedArray linePoints) {
		int bw = this.imgw / 10;
		int bh = this.imgw /10;
		
		for (Object o: operations) {
			if (o instanceof Circle) {
//				count = (count + 1)%colors.length;
				Circle c = (Circle)o;
				EndPoint p = new EndPoint(o,c.x,c.y, bw,bh);
				sortedArea.add(p);
			} else if (o instanceof Rect) {
				Rect r = (Rect)o;
				EndPoint p = new EndPoint(o,r.x,r.y, bw,bh);
				sortedArea.add(p);
			}else if(o instanceof Polygon) {
				Polygon pl = (Polygon)o;
				EndPoint p = new EndPoint(o,pl.xpoints[0],pl.ypoints[0], bw,bh);
				sortedArea.add(p);
//				System.out.println("paint Polygon:" + s);
			}else if(o instanceof Line) {
				Line l = (Line)o;
				sortLinesWithEndPoints(linePoints,l);
			}
		}
	}
	private void sortLinesWithEndPoints(SortedArray linePoints, Line l) {
		int bw = this.imgw / 10;
		int bh = this.imgw /10;
		EndPoint p1 = new EndPoint(l,l.startx,l.starty, bw,bh);
		EndPoint p2 = new EndPoint(l,l.endx,l.endy, bw,bh);
		EndPoint o1 = (EndPoint) linePoints.findSame(p1);
		EndPoint o2 = (EndPoint) linePoints.findSame(p2);

		boolean merged = false;
		if(o1 != null) {
			merged = mergeSameLine(l, o1.owners());
		}else if(o2 != null) {
			merged = mergeSameLine(l, o2.owners());
		}
		if(!merged) {
			if(o1 != null)
				o1.addOwner(l);
			else
				linePoints.add(p1);
			if(o2 != null)
				o2.addOwner(l);
			else
				linePoints.add(p2);

		}
	}

	private boolean mergeSameLine(Line l, Object[] lines) {
		boolean ret = false;
		for(int i = 0;i<lines.length;i++) {
			Line l1 = (Line) lines[i];
			if(l.isCoincide(l1)) {
				l1.thick = l.thick > l1.thick ? l.thick : l1.thick;
				ret = true;
				break;
			}
		}
		return ret;
	}

	private void paintPolylines(IGraphics g2d,ArrayList<PolyLine> polylines) {
		for(int i = 0;i<polylines.size();i++){
			PolyLine pl = polylines.get(i);
			this.paintPolyLine(g2d, pl);
		}
	}
	
	private void paintPolyLine(IGraphics g2d,PolyLine pl) {
		g2d.drawPolyLine(pl);
//		for(int j = 0;j<pl.lines.size();j++) {
//			this.paintLine(g2d, pl.lines.get(j));
//		}
	}
	private static final int indexOfObject(Object[] objs, Object obj) {
		for(int i = 0;i<objs.length;i++) {
			if(obj.equals(objs[i]))
				return i;
		}
		return -1;
	}

	private void createPolyLines(SortedArray linePoints,ArrayList<PolyLine> polylines) {
		int size = linePoints.size();
		for(int i = 0;i<size;i++) {

			EndPoint ep = (EndPoint) linePoints.get(i);
			ep.sortOwner(false);
			Object[] lines = ep.owners();
//			System.out.println(ep.x + ","+ep.y+"*******"+ lines.length);
			for( int j = 0;j<lines.length;j++) {
				Line l = (Line) lines[j];

				if(l == null)
					continue;
				PolyLine polyline = new PolyLine();
				polylines.add(polyline);
				polyline.add(l, false);
//				System.out.println(j+ "===="+l);
				EndPoint nextEp = ep;
				Line nextLine = l;
				int dup = -1;
				do {
					Line tmp = nextLine;
					nextEp = findNextEndpoint(linePoints,nextLine,nextEp);
					nextLine = findNextLine(nextLine, nextEp);
					nextEp.removeOwner(tmp);
					if(nextLine == null)
						break;
					polyline.add(nextLine, false);
					nextEp.removeOwner(nextLine);
					dup = indexOfObject(lines, nextLine);
					if(dup != -1)
						lines[dup] = null;
//					System.out.println("$$$$$$$$$$$$$" +nextLine);
				}while(true);

				nextLine = l;
				nextEp = ep;

				while(true) {
					Line tmp = nextLine;
					nextLine = findNextLine(nextLine, nextEp);
					nextEp.removeOwner(tmp);
					if(nextLine == null)
						break;
					nextEp.removeOwner(nextLine);
					polyline.add(nextLine, true);
					dup = indexOfObject(lines, nextLine);
					if(dup != -1)
						lines[dup] = null;
//					System.out.println("++++++++++" +nextLine);
					nextEp = findNextEndpoint(linePoints,nextLine,nextEp);
				}
			}
		}
	}
	
	private EndPoint findNextEndpoint(SortedArray linePoints,Line l, EndPoint ep) {
		EndPoint next;
		if(ep.x == l.startx && ep.y == l.starty)
			next = new EndPoint(l,l.endx,l.endy, ep.block.width,ep.block.height);
		else
			next = new EndPoint(l,l.startx,l.starty,ep.block.width,ep.block.height);
		next = (EndPoint)linePoints.findSame(next);
		return next;
	}
	private Line findNextLine(Line l, EndPoint ep) {
		ep.sortOwner(false);
		int idx = ep.indexOfOwner(l);
		Line nextLine = getLineBeside(ep,l,idx,true);
		if(nextLine == null)
			nextLine = getLineBeside(ep,l,idx,false);
		return nextLine;
	}
	
	private Line getLineBeside(EndPoint ep, Line l, int idx, boolean front) {
		Line ret = null;
		int beside = front ? idx -1 : idx+1;
		if(beside >= 0 && beside < ep.sizeOfOwner()) {
			Line nextLine = (Line) ep.getOwner(beside);
			if(nextLine.thick == l.thick) {
				ret = nextLine;
			}
		}
		return ret;
	}

	private void paintAreas(IGraphics g2d,SortedArray sortedArea) {
		int size = sortedArea.size();
		for(int i = 0;i<size;i++) {
			EndPoint ep = (EndPoint) sortedArea.get(i);
			Object o = ep.getOwner(0);
			paintArea(g2d,o);
		}
	}
	
	private static final Color[] colors = new Color[] {Color.red,Color.white, Color.blue,Color.green};
	private int testCount = 0;
	private void paintLine(IGraphics g2d, Line l) {

		Stroke s = g2d.getStroke();
		g2d.setStroke(new BasicStroke(l.thick, BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));
//		testCount = (testCount + 1)%colors.length;
//		g2d.setColor(colors[testCount]);
//System.out.println("Piaint line width:" + l.thick);
		g2d.drawLine(l.startx, l.starty, l.endx,l.endy);
		g2d.setStroke(s);

	}

	private void paintArea(IGraphics g2d, Object o) {
		if (o instanceof Circle) {
//			count = (count + 1)%colors.length;
			Circle c = (Circle)o;
//			if (c.inverted) {
//	 			g2d.setColor(Color.black);
//			}else {
//				g2d.setColor(Color.white);
//			}
			
//			g2d.fillOval(c.x,c.y +c.diameter, c.diameter, c.diameter);
			g2d.fillOval(c.x,c.y, c.diameter, c.diameter);

//			if (c.inverted) {
//	 			g2d.setColor(Color.white);
//			}

		} else if (o instanceof Rect) {
			Rect r = (Rect)o;
			g2d.fillRect(r.x, r.y, r.width, r.height);
		}else if(o instanceof Polygon) {
			Polygon p = (Polygon)o;
			String s = "";
			int hw = p.getBounds().width/2;
			int hh = p.getBounds().height/2;
			int[] xpos = new int[p.npoints];
			int[] ypos = new int[p.npoints];
			for(int i = 0;i<p.npoints;i++) {
				xpos[i] = p.xpoints[i]+hw;
				ypos[i] = p.ypoints[i]+hh;
//				s += "("+p.xpoints[i] +","+p.ypoints[i]+")";
			}
			g2d.fillPolygon(new Polygon(xpos,ypos,p.npoints));
//			System.out.println("paint Polygon:" + s);
		}	
	}

	private void oldPaint(IGraphics g2d) {
		for (Object o: operations) {
			if(o instanceof Line) {
				Line l = (Line)o;
				paintLine(g2d,l);
			}else
				paintArea(g2d,o);

		}
	}

	public void paint(IGraphics g2d) {
		findDimensions(g2d.getBorder());
		System.out.println("Found dimensions imgw: "+this.imgw+" imgh: "+this.imgh );
		g2d.initGraphics(this.imgw, this.imgh);
		g2d.paintBackGround();
		SortedArray sortedArea = new SortedArray();
		SortedArray linePoints = new SortedArray();
		ArrayList<PolyLine> polylines = new ArrayList<>();

		sortAndMerge(sortedArea,linePoints);
		createPolyLines(linePoints, polylines);
		
		this.paintAreas(g2d, sortedArea);
		this.paintPolylines(g2d, polylines);
//		oldPaint(g2d);
//		int count = 0;

		System.out.println("There were "+operations.size()+" operations in memory");
//		operations.clear();
		g2d.dispose();
	}
}