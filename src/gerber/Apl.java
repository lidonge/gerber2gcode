package gerber;
import java.io.*;
import java.util.*;

import javax.swing.JFrame;
import javax.swing.JPanel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.FlatteningPathIterator;
import java.awt.geom.Point2D;

public class Apl {

	// GLOBAL OUTPUT SETTINGS
	private	String dir = "/home/daveg/Electronics/relay-clock/plots/";
	private	String project = "relay-clock";

	private boolean process_F_Cu = false;
	private boolean process_B_Cu = true;
	private boolean process_NPTH = false;

	private int ppi = 1000; 		// pixels per inch
	private double border_mm = 1; 	// mm border around entire image
	
	// ==============================
	private double scale = 1; // 1 for mm
	private double step = 1; // subpixel stepping 0.5
	private int border = (int)Math.round(border_mm/25.4*ppi); // pixel border around entire image
	private boolean single_quadrant = false;
	private GerberBoard gerberBoard = new GerberBoard();
	private int linenumber = 0;
	private HashMap<Integer,Aperture> apertures = new HashMap<Integer,Aperture>();
	private Aperture aperture = null;
	private HashMap<Integer,Aperture> tools = new HashMap<Integer,Aperture>();
	private Aperture tool = null;
	private Point2D.Double lastPoint = new Point2D.Double(0,0);
	
	private boolean bLeadZero = false;
	private HashMap<String, Aperture> macros = new HashMap<>();
	
	public void addMacro(String name, String line) {
		StringTokenizer st = new StringTokenizer(line,",");
		
		int primitives = Integer.parseInt(st.nextToken().trim());
		System.out.println("macro primitives: "+primitives);
		if(primitives != 4)
			throw new Error("Un-implemented primitives:" + primitives);

		int exposure = Integer.parseInt(st.nextToken().trim());
		System.out.println("macro Exposure: "+exposure);

		int numVectors = Integer.parseInt(st.nextToken().trim());
		System.out.println("macro numVectors: "+numVectors);
		
		Point2D pStart = new Point2D.Double(
				Double.parseDouble(st.nextToken()),
				Double.parseDouble(st.nextToken())
				);
		Point2D[] corner = new Point2D[numVectors];
		for(int i = 0;i<numVectors;i++) {
			corner[i] = new Point2D.Double(
					Double.parseDouble(st.nextToken()) - pStart.getX(),
					Double.parseDouble(st.nextToken()) - pStart.getY()
					);
		}
		String rotate = st.nextToken();
		macros.put(name, new PolygonAperture(this.ppi,"P",null, pStart, corner, exposure));
	}
	public void addAperture(String line) {
		Aperture aperture = null;
		// strip the %AD
		String s = line.substring(3); 
		// get the aperture number
		int n = Integer.parseInt(s.substring(1, 3));
		System.out.println("aperture number: "+n);
		// get the type
		String type = s.substring(3, 4);
		System.out.println("aperture type: #"+type+"#");
		if(s.charAt(4) != ',') {
			String macro = s.substring(3, s.length() - 1);
			aperture = this.macros.get(macro);
		}else {
			String modifiers = s.substring(s.indexOf(",")+1, s.indexOf("*"));
			System.out.println("modifiers: #"+modifiers+"#");
			
			// extract modifiers
			float[] modarray = new float[4];
			int modindex = 0;
			
			while (modifiers.length() > 0) {
				int xpos = modifiers.indexOf("X");
				if (xpos != -1) {
					modarray[modindex] = Float.valueOf(modifiers.substring(0, xpos));
					modifiers = modifiers.substring(xpos+1);
				} else {
					modarray[modindex] = Float.valueOf(modifiers);
					modifiers = "";
				}
				modindex++;
			}
			
			System.out.println("modifier 0:"+modarray[0]);
			System.out.println("modifier 1:"+modarray[1]);
			System.out.println("modifier 2:"+modarray[2]);
			System.out.println("modifier 3:"+modarray[3]);
			
			aperture = new Aperture(this.ppi, type, modarray);
		}
		this.apertures.put(new Integer(n), aperture);
	}
	
	
	public void addTool(String line) {
		// strip the T
		String s = line.substring(1); 
		System.out.println("trimmed: "+s);
		
		// get the tool number
		int cpos = s.indexOf("C");
		int n = Integer.parseInt(s.substring(0, cpos));
		System.out.println("tool number: "+n);

		// extract modifiers
		float[] modarray = new float[4];
		int modindex = 0;
		String modifiers = s.substring(cpos+1);
		
		while (modifiers.length() > 0) {
			int xpos = modifiers.indexOf("X");
			if (xpos != -1) {
				modarray[modindex] = Float.valueOf(modifiers.substring(0, xpos));
				modifiers = modifiers.substring(xpos+1);
			} else {
				modarray[modindex] = Float.valueOf(modifiers);
				modifiers = "";
			}
			modindex++;
		}
		
		System.out.println("modifier 0:"+modarray[0]);
		System.out.println("modifier 1:"+modarray[1]);
		System.out.println("modifier 2:"+modarray[2]);
		System.out.println("modifier 3:"+modarray[3]);
		
		Aperture a = new Aperture(this.ppi, "C", modarray);
		this.tools.put(new Integer(n), a);
	}
			
	public void selectAperture(String line) {
		// strip the G54
		String s = line.substring(3); 
		// get the aperture number
		int n = Integer.parseInt(s.substring(1, 3));
		System.out.println("selecting aperture number: "+n);
		this.aperture = this.apertures.get(new Integer(n));
	}
	
	
	public void selectTool(String line) {
		// strip the T
		String s = line.substring(1); 
		// get the tool number
		int n = Integer.parseInt(s);
		System.out.println("selecting tool number: "+n);
		this.tool = this.tools.get(new Integer(n));
	}

	private String lastD = null;
	private int lastX,lastY;
	public void draw(String line) {
		int xpos = line.indexOf("X");
		int ypos = line.indexOf("Y");
		int dpos = line.indexOf("D");
		if(dpos == -1) {
			line = line.substring(0,line.length() - 1) +this.lastD;
			dpos = line.indexOf("D");
		}
		int x=-1,y=-1;
		
		if(xpos != -1){
			String xstr = line.substring(xpos+1, ypos == -1 ? dpos : ypos);
			// add leading zeroes
			while (xstr.length() < 7) {
				if(bLeadZero)
					xstr = "0"+xstr;
				else
					xstr = xstr + "0";
			}
			// add decimal point
			xstr = xstr.substring(0, 3) + "." + xstr.substring(3);
			x = (int)Math.round(Double.valueOf(xstr)*(double)this.ppi);
		}else
			x = this.lastX;
		
		if(ypos != -1) {
			String ystr = line.substring(ypos+1, dpos);
			while (ystr.length() < 7) {
				if(bLeadZero)
					ystr = "0"+ystr;
				else
					ystr = ystr + "0";
			}
			ystr = ystr.substring(0, 3) + "." + ystr.substring(3);
			y = (int)Math.round(Double.valueOf(ystr)*(double)this.ppi);
		}else
			y = this.lastY;
		
//		System.out.println("draw line :" +line +"====" + x + ","+y);
		if (line.endsWith("D01*")) { // move with shutter OPEN
			// make a path from lastPoint to x,y
			if(true) {
				this.aperture.drawLine(this.gerberBoard, lastX, lastY, x,y);				
			}else {
				double distance = Functions.getDistance(lastPoint, x, y);
				while(distance > this.step) {
					Point2D.Double next = Functions.calcStep(lastPoint, x, y, this.step);
									
					int xx = (int)Math.round(next.x);
					int yy = (int)Math.round(next.y);
					this.aperture.draw(this.gerberBoard, xx, yy);
					this.lastPoint.x = next.x;
					this.lastPoint.y = next.y;
									
					distance = Functions.getDistance(lastPoint, x, y);
					//System.out.println("distance: "+distance);
				}
			}
			this.lastD = "D01*";
		}
		if (line.endsWith("D02*")) { // move with shutter CLOSED
			this.lastPoint.x = x;
			this.lastPoint.y = y;
			this.lastD = "D02*";
		}
		if (line.endsWith("D03*")) { // flash
			this.aperture.draw(this.gerberBoard, x, y);
			this.lastPoint.x = x;
			this.lastPoint.y = y;
			this.lastD = "D03*";
		}
		lastX = x;
		lastY = y;
	}

	private int parseValue(String s) {
		boolean negative = false;
		// strip minus signs
		if (s.startsWith("-")) { 
			s = s.substring(1);
			negative = true;
		}
		s = addLeadingZeroes(s);
		s = addDecimalPoint(s);
		int i = (int)Math.round(Double.valueOf(s)*(double)this.ppi);
		if (negative) {
			i = i*-1;
		}
		return i;
	}
	
	private String addDecimalPoint(String s) {
		return s.substring(0, 3) + "." + s.substring(3);
	}
	
	private String addLeadingZeroes(String s) {
		while (s.length() < 7) {
			s = "0"+s;
		}
		return s;
	}
	
	public void drawArc(String line) {
		int xpos = line.indexOf("X");
		int ypos = line.indexOf("Y");
		int ipos = line.indexOf("I");
		int jpos = line.indexOf("J");
		int dpos = line.indexOf("D");

		String xstr = line.substring(xpos+1, ypos);
		String ystr = line.substring(ypos+1, ipos);
		String istr = line.substring(ipos+1, jpos);
		String jstr = line.substring(jpos+1, dpos);
		
		int x = parseValue(xstr);
		int y = parseValue(ystr);
		int i = parseValue(istr);
		int j = parseValue(jstr);
		
		System.out.println("Arc: "+x+", "+y+", "+i+", "+j);

		int centerx = (int)this.lastPoint.x + i;
		int centery = (int)this.lastPoint.y + j;
		
		double radius = Functions.getDistance(lastPoint, centerx, centery);
		double arcResolution = 0.00175;
		
		System.out.println("Circle at: ["+centerx+", "+centery+"] Radius:"+radius);
		
		// The parametric equation for a circle is
		// x = cx + r * cos(a) 
		// y = cy + r * sin(a) 
		// Where r is the radius, cx,cy the origin, and a the angle from 0..2PI radians or 0..360 degrees.
	
		if (line.endsWith("D01*")) { // move with shutter OPEN
			// make a path from lastPoint to x,y
			double angle = 2 * Math.PI;
			while (angle > 0) {
				int xx = (int)Math.round(centerx + radius * Math.cos(angle));
				int yy = (int)Math.round(centery + radius * Math.sin(angle));

				this.aperture.draw(this.gerberBoard, xx, yy);
				this.lastPoint.x = xx;
				this.lastPoint.y = yy;
			
				angle = angle - arcResolution;
			}
		}
	}
	
	
	public void drill(String line) {
		int xpos = line.indexOf("X");
		int ypos = line.indexOf("Y");
		String xstr = line.substring(xpos+1, ypos);
		String ystr = line.substring(ypos+1);
		
		if (ystr.startsWith("-")) { 
			ystr = ystr.substring(1);
		}
		
		// add leading zeroes
		while (xstr.length() < 6) {
			xstr = "0"+xstr;
		}
		while (ystr.length() < 6) {
			ystr = "0"+ystr;
		}

		// add decimal point
		xstr = xstr.substring(0, 2) + "." + xstr.substring(2);
		ystr = ystr.substring(0, 2) + "." + ystr.substring(2);
		//System.out.println("xstr:"+xstr);
		//System.out.println("ystr:"+xstr);
				
		int x = (int)Math.round(Double.valueOf(xstr)*(double)this.ppi);
		int y = (int)Math.round(Double.valueOf(ystr)*(double)this.ppi);
				
		y = Math.abs(y); // invert
		
		this.tool.draw(this.gerberBoard, x, y, true);
		this.lastPoint.x = x;
		this.lastPoint.y = y;
	}
	private String macroName = null;
	public boolean processGerber(String line) {
		this.linenumber++;
		
		line = line.trim().toUpperCase();
		if (line.startsWith("%FS")) {
			System.out.println("got format definition! line "+this.linenumber);
			if (line.equals("%FSTAX34Y34*%")) {
				bLeadZero = false;
			}else if (line.equals("%FSLAX34Y34*%")) {
				bLeadZero = true;
			}else {
				System.out.println("wrong format definition! STOPPING...");
				return true;
			}
		}else if (line.startsWith("%AD")) {
			System.out.println("got aperture definition! line "+this.linenumber);
			addAperture(line);
		}else if (line.startsWith("%AM")) {
			System.out.println("got macro definition! line "+this.linenumber);
			macroName = line.substring(3);
		}else if (line.startsWith("%MOIN*%")) {
			System.out.println("Dimensions are expressed in inches");
			this.scale = 25.4;
		}else if (line.startsWith("%MOMM*%")) {
			System.out.println("Dimensions are expressed in millimeters");
			this.scale = 1;
		}else if (line.startsWith("G04")) {
			System.out.println("ignoring comment on line "+this.linenumber);
		}else if (line.startsWith("G70")) {
			System.out.println("Set unit to INCH");
		}else if (line.startsWith("G71")) {
			System.out.println("Set unit to MM");
		}else if (line.startsWith("G74")) {
			System.out.println("Selecting Single quadrant mode");
			single_quadrant = true;
		}else if (line.startsWith("G75")) {
			System.out.println("Selecting Multi quadrant mode");
			single_quadrant = false;
		}else if (line.startsWith("G90")) {
			System.out.println("Set Coordinate format to Absolute notation");
		}else if (line.startsWith("G91")) {
			System.out.println("Set the Coordinate format to Incremental notation");
		}else if (line.startsWith("G54")) {
			System.out.println("Select aperture");
			selectAperture(line);
		}else if (line.startsWith("M02")) {
			System.out.println("STOP");
			return true;
		}else if (line.startsWith("G02")) {
			drawArc(line);
		}else if (line.startsWith("G03")) {
			drawArc(line);
		}else if (line.startsWith("X") || line.startsWith("Y")) {
			draw(line);
		}else if( macroName != null) {
			this.addMacro(macroName, line);
			macroName = null;
		}

		return false;
	}

	
	
	public boolean processDrill(String line) {
		
		this.linenumber++;
		
		line = line.trim().toUpperCase();

		if (line.startsWith("T")) {
			if (line.indexOf("C") != -1) {
				System.out.println("got tool definition! line "+this.linenumber);
				addTool(line);
			} else {
				System.out.println("got tool change! line "+this.linenumber);
				if (!line.equals("T0")) {
					selectTool(line);
				}
			}
		}
		
		if (line.startsWith("M30")) {
			System.out.println("STOP");
			return true;
		}
		
		if (line.startsWith("X")) {
			drill(line);
		}

		return false;
	}
	
	private void processGerberFile(String filename) {
		File file = new File(filename);
		
		FileReader fr = null;
		try {
			fr = new FileReader(file);
		} catch (Exception e) {
			System.out.println("Error (1): "+e);
		}
		
		BufferedReader br = new BufferedReader(fr);
		
		String line;
		try {
			boolean stop = false;
			this.linenumber = 0;
			while ((line = br.readLine()) != null && !stop) {
		   		stop = processGerber(line);
			}
		} catch (Exception e) {
			System.out.println("Error (2): "+e);
			e.printStackTrace();
		}
				
		try {
			br.close();
		} catch (Exception e) {
			System.out.println("Error (3): "+e);
		}	
	}

	private void processDrillFile(String filename) {
		File file = new File(filename);
		
		FileReader fr = null;
		try {
			fr = new FileReader(file);
		} catch (Exception e) {
			System.out.println("Error (4): "+e);
		}
		
		BufferedReader br = new BufferedReader(fr);
		
		String line;
		try {
			boolean stop = false;
			this.linenumber = 0;
			while ((line = br.readLine()) != null && !stop) {
		   		stop = processDrill(line);
			}
		} catch (Exception e) {
			//System.out.println("Error (6): "+);
			e.printStackTrace();
		}
				
		try {
			br.close();
		} catch (Exception e) {
			System.out.println("Error (7): "+e);
		}	
	}
	public void drawAndWritePen(String filename, int ppi, int border, boolean negative) {
		GCodeGraphics pngGraphic = new GCodeGraphics(filename,ppi,border, negative);
		gerberBoard.paint(pngGraphic);
	}

	public void drawAndWritePen( String pen) {
		drawAndWritePen(pen, this.ppi, 0, false); 		
	}
	
	public void drawAndWritePNG(String filename, int ppi, int border, boolean negative) {
		PNGGraphics pngGraphic = new PNGGraphics(filename,ppi,border, negative);
		gerberBoard.paint(pngGraphic);
	}

	public void drawAndWritePNG( String png) {
		drawAndWritePNG(png, this.ppi, 0, false); 		
	}
	
	public void drawFrame() {
		JFrame frame = new JFrame();
		frame.setSize(800, 600);
//		frame.getRootPane().setLayout(new BorderLayout());
//		this.ppi = frame.getToolkit().getScreenResolution();		
		JPanel pane = new JPanel() {
//			this.ppi = this.getToolkit().getScreenResolution();
			FileGraphics fg = new FileGraphics("",ppi,0, false) {
			};
			public void paint(Graphics g) {
				fg.setGraphics((Graphics2D) g);
				gerberBoard.paint(fg);
			}
		};
		frame.add(pane);
//		pane.setSize(600, 800);
		frame.show();
	}
	public Apl(String gerber) {
		processGerberFile(gerber);
	}
	
	public static void main(String[] args) {
		Apl apl = new Apl(args[0]);
//		apl.drawFrame();
		apl.drawAndWritePNG(args[0]+".png");
		apl.drawAndWritePen(args[0]+".nc");
	}
}
