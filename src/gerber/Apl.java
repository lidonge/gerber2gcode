package gerber;
import java.io.*;
import java.text.ParseException;
import java.util.*;

import javax.swing.JFrame;
import javax.swing.JPanel;

import tools.ConfigTool;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.FlatteningPathIterator;
import java.awt.geom.Point2D;

public class Apl {

	// GLOBAL OUTPUT SETTINGS
	private static final String CONF_FILE = "./aplgcode.conf";

	private int ppi = 1000; 		// pixels per inch
	
	// ==============================
	private double scale = 1; // 1 for mm
	private double step = 1; // subpixel stepping 0.5
	private boolean single_quadrant = false;
	private GerberBoard gerberBoard = new GerberBoard();
	private int linenumber = 0;
	private HashMap<Integer,Aperture> apertures = new HashMap<Integer,Aperture>();
	private Aperture aperture = null;
	private HashMap<Integer,Aperture> tools = new HashMap<Integer,Aperture>();
	private Aperture tool = null;
	private String macroName = null;
	private int g_integer = 2, g_decimal = 5;
	private int d_integer = 2, d_decimal = 5;
	
	private boolean bLeadZero = false;
	private HashMap<String, Aperture> macros = new HashMap<>();
	private String gerberFile, drillFile;
	private boolean mirroVertical, mirroHorizontal;
	
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
		a.drillTool = n;
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
		Point p = readXY(line, xpos,ypos,dpos,g_integer,g_decimal);
		x = p.x;
		y = p.y;
//		System.out.println("draw line :" +line +"====" + x + ","+y);
		if (line.endsWith("D01*")) { // move with shutter OPEN
			// make a path from lastPoint to x,y
			this.aperture.drawLine(this.gerberBoard, lastX, lastY, x,y);				
			this.lastD = "D01*";
		}
		if (line.endsWith("D02*")) { // move with shutter CLOSED
			this.lastD = "D02*";
		}
		if (line.endsWith("D03*")) { // flash
			this.aperture.draw(this.gerberBoard, x, y);
			this.lastD = "D03*";
		}
		lastX = x;
		lastY = y;
	}
	
	private Point readXY(String line, int xpos, int ypos, int dpos, int integer, int decimal) {
		
		int x=-1,y=-1;
		int len = integer + decimal;
		
		if(xpos != -1){
			String xstr = line.substring(xpos+1, ypos == -1 ? dpos : ypos);
			// add leading zeroes
			while (xstr.length() < len) {
				if(bLeadZero)
					xstr = "0"+xstr;
				else
					xstr = xstr + "0";
			}
			// add decimal point
			xstr = xstr.substring(0, integer) + "." + xstr.substring(integer);
			x = (int)Math.round(Double.valueOf(xstr)*(double)this.ppi);
		}else
			x = this.lastX;
		
		if(ypos != -1) {
			String ystr = line.substring(ypos+1, dpos);
			while (ystr.length() < len) {
				if(bLeadZero)
					ystr = "0"+ystr;
				else
					ystr = ystr + "0";
			}
			ystr = ystr.substring(0, integer) + "." + ystr.substring(integer);
			y = (int)Math.round(Double.valueOf(ystr)*(double)this.ppi);
		}else
			y = this.lastY;
		return new Point(x,y);
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

		int centerx = (int)this.lastX + i;
		int centery = (int)this.lastY + j;
		
		double radius = Functions.getDistance(lastX, lastY, centerx, centery);
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
				this.lastX = xx;
				this.lastY = yy;
			
				angle = angle - arcResolution;
			}
		}
	}
	private void readIntegerAndDecmal(String line) {
		int idx = line.indexOf('X');//fix me, forget diff of x and y
		
		g_integer = Integer.parseInt(line.charAt(idx + 1) +"");
		g_decimal = Integer.parseInt(line.charAt(idx+2)+"");
	}
	public boolean processGerber(String line) {
		this.linenumber++;
		
		line = line.trim().toUpperCase();
		if (line.startsWith("%FS")) {
			System.out.println("got format definition! line "+this.linenumber);
			if (line.startsWith("%FSTA")) {
				readIntegerAndDecmal(line);
				bLeadZero = false;
			}else if (line.equals("%FSLA")) {
				readIntegerAndDecmal(line);
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

	public void drill(String line) {
		int xpos = line.indexOf("X");
		int ypos = line.indexOf("Y");
		int dpos = line.length();
		int x, y;
		Point p = readXY(line, xpos,ypos,dpos, d_integer,d_decimal);
		x = p.x;
		y = p.y;
				
//		y = Math.abs(y); // invert
		
		this.tool.draw(this.gerberBoard, x, y, true);
		this.lastX = x;
		this.lastY = y;
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
		
		if (line.startsWith("X")|| line.startsWith("Y")) {
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
	public boolean initDrill() {
		boolean ret = false;
		if(this.drillFile != null) {
			gerberBoard.clear();
			processDrillFile(drillFile);
			ret = true;
		}
		return ret;
	}

	public void drillHole(String filename, int ppi) {
		CNCCarvingPainter cnc = new CNCCarvingPainter(filename+"_drl.nc",ppi, mirroVertical, mirroHorizontal);
		cnc.setStroke(new ICNCStroke() {

			@Override
			public Shape createStrokedShape(Shape p) {
				return null;
			}

			@Override
			public int getDrillTool() {
				return 0;
			}

			@Override
			public double getToolSize() {
				return 0.08;
			}

			@Override
			public double getDeepth() {
				return ConfigTool.getInstance().drillDeepth;
			}
			
		});
		gerberBoard.paint(cnc);	

	}

	public void drillHole( String filename) {
		drillHole(filename, this.ppi); 		
	}

	public void drillLocatingHole(String filename, int ppi) {
		CNCCarvingPainter cnc = new CNCCarvingPainter(filename+"_drl_lc.nc",ppi, mirroVertical, mirroHorizontal);
		cnc.setStroke(new ICNCStroke() {

			@Override
			public Shape createStrokedShape(Shape p) {
				return null;
			}

			@Override
			public int getDrillTool() {
				return 0;
			}

			@Override
			public double getToolSize() {
				return 0.08;
			}

			@Override
			public double getDeepth() {
				return ConfigTool.getInstance().locating_hole_deepth;
			}
			
		});
		gerberBoard.paintLocating(cnc);	

	}

	public void drillLocatingHole( String filename) {
		drillLocatingHole(filename, this.ppi); 		
	}
	
	public void drawAndWritePen(String filename, int ppi) {
		AxiDrawPainter penGraphic = new AxiDrawPainter(filename+".nc",ppi, mirroVertical, mirroHorizontal);
		gerberBoard.paint(penGraphic);
	}

	public void drawAndWritePen( String filename) {
		drawAndWritePen(filename, this.ppi); 		
	}
	
	public void drawLocatingHole(String filename, int ppi) {
		AxiDrawPainter locating = new AxiDrawPainter(filename+"_lc.nc",ppi, mirroVertical, mirroHorizontal);
		gerberBoard.paintLocating(locating);	

	}

	public void drawLocatingHole( String filename) {
		drawLocatingHole(filename, this.ppi); 		
	}

	public void drawAndWritePNG(String filename, int ppi) {
		PNGPainter pngGraphic = new PNGPainter(filename+".png",ppi, mirroVertical, mirroHorizontal);
		gerberBoard.paint(pngGraphic);
	}
	
	public Rectangle initClip() {
		return gerberBoard.initClip();
	}

	public void setClip(int x, int y,int w, int h) {
		gerberBoard.setClip(x,y,w, h);
	}
	
	public void drawAndWritePNG( String png) {
		drawAndWritePNG(png, this.ppi); 		
	}
	
	public void drawFrame() {
		JFrame frame = new JFrame();
		frame.setSize(800, 600);
//		frame.getRootPane().setLayout(new BorderLayout());
//		this.ppi = frame.getToolkit().getScreenResolution();		
		JPanel pane = new JPanel() {
//			this.ppi = this.getToolkit().getScreenResolution();
			GerberPainter fg = new GerberPainter("",ppi, mirroVertical, mirroHorizontal) {
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
	public Apl(String gerber, String drill, boolean mirroVertical, boolean mirroHorizontal) {
		this.gerberFile = gerber;
		this.drillFile = drill;
		this.mirroVertical = mirroVertical;
		this.mirroHorizontal = mirroHorizontal;
		this.ppi = ConfigTool.getInstance().ppi;
		processGerberFile(gerber);
	}
	private static void dealWithGerber(String gtl,String gbl,Apl apl_gtl,Apl apl_gbl) {
		Rectangle dtl = apl_gtl.initClip();
		Rectangle dbl = apl_gbl.initClip();
		int maxW = Math.max(dtl.width, dbl.width);
		int maxH = Math.max(dtl.height, dbl.height);
		int minx = Math.min(dtl.x, dbl.x);
		int miny = Math.min(dtl.y, dbl.y);
		apl_gtl.setClip(minx, miny, maxW, maxH);
		apl_gbl.setClip(minx, miny,maxW, maxH);
		
//		apl.drawFrame();
		apl_gtl.drawAndWritePNG(gtl);
		if(apl_gbl != null)
			apl_gbl.drawAndWritePNG(gbl);
		
		apl_gtl.drawAndWritePen(gtl);
		if(apl_gbl != null) {
			apl_gbl.drawAndWritePen(gbl);
		}
		
		apl_gtl.drawLocatingHole(gtl);
		
		if(apl_gtl.initDrill()) {
			apl_gtl.drillLocatingHole(gtl);
			apl_gtl.drillHole(gtl);
		}
	}
	
	private static void initConf(String conf, HashMap<String, String> map) throws IOException, ParseException {
		File f = new File(conf);
		System.out.println("Read config file :" + f.getAbsolutePath());
		FileReader reader = new FileReader(conf);
		BufferedReader in = new BufferedReader(reader);
		String line;
		while((line = in.readLine()) != null) {
			line = line.trim();
			if(line.length() == 0 || line.startsWith("#"))
				continue;
			int idx = line.indexOf('=');
			if(idx != -1) {
				String content = line.substring(0, idx).trim();
				map.put(content, line.substring(idx+1).trim());
			}
		}
		reader.close();
	}
	private static String[] parseCmd(String sCmd) {
		String[] ret = null;
		int idx = sCmd.indexOf('=');
		if(idx != -1) {
			String cmd = sCmd.substring(0, idx);
			String value = sCmd.substring(idx+1);
			ret = new String[]{cmd,value};
		}
		return ret;
	}
	
	private static void help() {
		System.out.println("======================help=================");
		System.out.println("java -jar aplgcode.jar <-h> <cmd=arg>...");
		System.out.println("\t\"drillfile\" specify the drill file path.");
		
		System.out.println("genconf=<default> | <filepath> to generate a default config file by name of ./aplgcode.conf or the given filepath.");
		System.out.println("gbr=<gerber file name(without suffix)>.");
		System.out.println("\t\"gerber file name\" is the prefix of the input and output files.");
		System.out.println("\t\"gerber file name\".gtl is the input of top layer file.");
		System.out.println("\t\"gerber file name\".gbl is the input of botttom layer file.");
		System.out.println("drl=<drill file path>.");
		System.out.println("\t\"drill file path\" specify the drill file path.");
		System.out.println("conf=<filepath> specify the config file.");

		System.out.println("===========================================");
	}
	private static void genDefaultConf(String conf) {
		try {
			FileWriter writer = new FileWriter(conf);
			BufferedWriter out = new BufferedWriter(writer);
			out.write("#using inch					");out.newLine();
			out.write("                             ");out.newLine();
			out.write("#general parameters          ");out.newLine();
			out.write("ppi=1000                     ");out.newLine();
			out.write("border=0.1                   ");out.newLine();
			out.write("mirroVertical_gtl = false    ");out.newLine();
			out.write("mirroHorizontal_gtl = false  ");out.newLine();
			out.write("mirroVertical_gbl = false    ");out.newLine();
			out.write("mirroHorizontal_gbl = false  ");out.newLine();
			out.write("paint_vertical=true			");out.newLine();
			out.write("sortblock_dia=0.04			");out.newLine();

			out.write("                             ");out.newLine();
			out.write("#locating hole               ");out.newLine();
			out.write("locating_hole_size=0.06      ");out.newLine();
			out.write("locating_hole_deepth=-0.08	");out.newLine();
			out.write("                             ");out.newLine();
			out.write("#drill tools                 ");out.newLine();
			out.write("drillSafeDeepth=10           ");out.newLine();
			out.write("downSpeed=1200               ");out.newLine();
			out.write("drillSpeed=200               ");out.newLine();
			out.write("drillDeepth=-0.06            ");out.newLine();
			out.write("                             ");out.newLine();
			out.write("#drill file config           ");out.newLine();
			out.write("drill_integer=2              ");out.newLine();
			out.write("drill_decimal=5              ");out.newLine();
			out.write("                             ");out.newLine();
			out.write("#gerber file config          ");out.newLine();
			out.write("gerber_integer=2             ");out.newLine();
			out.write("gerber_decimal=5             ");out.newLine();
			out.write("                             ");out.newLine();
			out.write("#pen config                  ");out.newLine();
			out.write("penDim = 0.02                ");out.newLine();
			out.write("overPen = 0.2                ");out.newLine();
			out.write("defaultSpeed = 3000          ");out.newLine();
			out.write("lineSpeed = 50	            ");out.newLine();
			out.close();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public static void main(String[] args) {
		if(args.length == 0 || "-h".equalsIgnoreCase(args[0])) {
			help();
			return;
		}
		String conf = CONF_FILE;
		String gerber = null;
		String drill = null;
		for(int i = 0;i<args.length;i++) {
			String[] cmd = parseCmd(args[i]);
			if(cmd == null)
				continue;
			if("genconf".equalsIgnoreCase(cmd[0])) {
				String cfg = "default".equalsIgnoreCase(cmd[1]) ? CONF_FILE : cmd[1];
				System.out.println("Generate default config file:" + cfg);
				genDefaultConf(cfg);
				return;
			}else if("conf".equalsIgnoreCase(cmd[0])) {
				conf = cmd[1];
			}else if("gbr".equalsIgnoreCase(cmd[0])) {
				gerber = cmd[1];
			}else if("drl".equalsIgnoreCase(cmd[0])) {
				drill = cmd[1];
			}
		}
		if(gerber == null) {
			System.out.println("Error: Gerber file should not be null!");
			help();
			return;
		}
		if(drill == null) {
			System.out.println("Worning: Drill file is null!");
		}else {
			File fDrill = new File(drill);
			if(!fDrill.exists()) {
				System.out.println("Worning: Drill file " + fDrill.getAbsolutePath() + " does not exists!");
				drill = null;
			}
		}
		File gtl = new File(gerber+".gtl");
		File gbl = new File(gerber+".gbl");
		
		if(!gtl.exists()) {
			System.out.println("Error: Gerber file " + gtl.getAbsolutePath() + " does not exists!");
			return;
		}
		HashMap<String, String> configs = new HashMap<String, String>();
		try {
			initConf(conf, configs);
			System.out.println(configs);
			ConfigTool.getInstance().init(configs);
		} catch (IOException | ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Apl apl_gtl = new Apl(gtl.getAbsolutePath(), drill, ConfigTool.getInstance().mirroVertical_gtl,ConfigTool.getInstance().mirroHorizontal_gtl);
		Apl apl_gbl = null;
		if(gbl.exists()) {
			apl_gbl = new Apl(gbl.getAbsolutePath(), drill, ConfigTool.getInstance().mirroVertical_gbl,ConfigTool.getInstance().mirroHorizontal_gbl);
		}else {
			System.out.println("Worning: Gerber file " + gbl.getAbsolutePath() + " does not exists!");
		}
		dealWithGerber(gtl.getAbsolutePath(),gbl.getAbsolutePath(),apl_gtl,apl_gbl);
	}
}
