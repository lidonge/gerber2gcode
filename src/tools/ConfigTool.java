package tools;
import java.io.*;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ConfigTool {
	// Apl.java
	public int g_integer = 2, g_decimal = 5;
	// DrillGCodeGraphics.java
	public int d_integer = 2, d_decimal = 5;
	public double drillSafeDeepth = 10;
	public int downSpeed = 1200;
	public int drillSpeed = 200;
	public double drillDeepth = -2.5;

	// inch in GerberBoard.java
	public double locHole = 0.06;
	public double locating_hole_deepth= 0.04;

	// general
	public int ppi;
	public double border;
	public boolean paint_vertical;
	public double sortblock_dia;
	public boolean fixpolygonY = false;
	// in GerberPainter
	public boolean mirroVertical_gtl = false;
	public boolean mirroHorizontal_gtl = false;
	public boolean mirroVertical_gbl = false;
	public boolean mirroHorizontal_gbl = false;

	// in PenGCodeGraphics
	public double penDim = 0.02;// inch
	public double overPen = 0.2;
	public int defaultSpeed = 3000;
	public int lineSpeed = 100;

	private static ConfigTool tool;

	public static final ConfigTool getInstance() {
		if (tool == null) {
			synchronized (ConfigTool.class) {
				if (tool == null)
					tool = new ConfigTool();
			}
		}
		return tool;
	}

	public void init(HashMap<String, String> map) {
		Iterator<Map.Entry<String, String>> it = map.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, String> e = it.next();
			if (locatingHole(e)) {

			} else if (drillFile(e)) {

			} else if (general(e)) {

			}
		}

	}

	private boolean locatingHole(Map.Entry<String, String> e) {
		boolean ret = true;
		if ("locating_hole_size".equalsIgnoreCase(e.getKey())) {
			locHole = toDouble(e.getValue());
		} else if ("locating_hole_deepth".equalsIgnoreCase(e.getKey())) {
			locating_hole_deepth = toDouble(e.getValue());
		} else
			ret = false;
		return ret;
	}

	private boolean drillFile(Map.Entry<String, String> e) {
		boolean ret = true;
		// =============drill file config========
		if ("drill_integer".equalsIgnoreCase(e.getKey())) {
			d_integer = toInt(e.getValue());
		} else if ("drill_decimal".equalsIgnoreCase(e.getKey())) {
			d_decimal = toInt(e.getValue());
		} else if ("gerber_integer".equalsIgnoreCase(e.getKey())) {
			g_integer = toInt(e.getValue());
		} else if ("gerber_decimal".equalsIgnoreCase(e.getKey())) {
			g_decimal = toInt(e.getValue());
		} else if ("drillSafeDeepth".equalsIgnoreCase(e.getKey())) {
			drillSafeDeepth = toDouble(e.getValue());
		} else if ("downSpeed".equalsIgnoreCase(e.getKey())) {
			downSpeed = toInt(e.getValue());
		} else if ("drillSpeed".equalsIgnoreCase(e.getKey())) {
			drillSpeed = toInt(e.getValue());
		} else if ("drillDeepth".equalsIgnoreCase(e.getKey())) {
			drillDeepth = toDouble(e.getValue());
		} else {
			ret = false;
		}
		return ret;
	}

	private boolean general(Map.Entry<String, String> e) {
			boolean ret = true;
		if ("ppi".equalsIgnoreCase(e.getKey())) {
			ppi = toInt(e.getValue());
		} else if ("border".equalsIgnoreCase(e.getKey())) {
			border = toDouble(e.getValue());
		} else if ("mirroVertical_gtl".equalsIgnoreCase(e.getKey())) {
			mirroVertical_gtl = toBoolean(e.getValue());
		} else if ("mirroHorizontal_gtl".equalsIgnoreCase(e.getKey())) {
			mirroHorizontal_gtl = toBoolean(e.getValue());
		} else if ("mirroVertical_gbl".equalsIgnoreCase(e.getKey())) {
			mirroVertical_gbl = toBoolean(e.getValue());
		} else if ("mirroHorizontal_gbl".equalsIgnoreCase(e.getKey())) {
			mirroHorizontal_gbl = toBoolean(e.getValue());
		} else if ("penDim".equalsIgnoreCase(e.getKey())) {
			penDim = toDouble(e.getValue());
		} else if ("overPen".equalsIgnoreCase(e.getKey())) {
			overPen = toDouble(e.getValue());
		} else if ("defaultSpeed".equalsIgnoreCase(e.getKey())) {
			defaultSpeed = toInt(e.getValue());
		} else if ("lineSpeed".equalsIgnoreCase(e.getKey())) {
			lineSpeed = toInt(e.getValue());
		} else if ("paint_vertical".equalsIgnoreCase(e.getKey())) {
			paint_vertical = toBoolean(e.getValue());
		} else if ("sortblock_dia".equalsIgnoreCase(e.getKey())) {
			sortblock_dia = toDouble(e.getValue());
		} else if ("fixpolygonY".equalsIgnoreCase(e.getKey())) {
			fixpolygonY = toBoolean(e.getValue());
		} else 
			ret = false;

		return ret;
	}

	private int toInt(String s) {
		try {
			return Integer.parseInt(s);
		} catch (Throwable t) {
			t.printStackTrace();
		}
		return -1;
	}

	private double toDouble(String s) {
		try {
			return Double.parseDouble(s);
		} catch (Throwable t) {
			t.printStackTrace();
		}
		return -1;
	}

	private boolean toBoolean(String s) {
		return "true".equals(s);
	}

	public static void genDefaultConf(String conf) {
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
			out.write("fixpolygonY=false			");out.newLine();

			out.write("                             ");out.newLine();
			out.write("#locating hole               ");out.newLine();
			out.write("locating_hole_size=0.06      ");out.newLine();
			out.write("locating_hole_deepth=-0.08	");out.newLine();
			out.write("                             ");out.newLine();
			out.write("#drill tools                 ");out.newLine();
			out.write("drillSafeDeepth=0.4           ");out.newLine();
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
}
