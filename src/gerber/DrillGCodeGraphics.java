package gerber;

import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.RenderingHints.Key;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.FlatteningPathIterator;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ImageObserver;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderableImage;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.AttributedCharacterIterator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import gcode.model.DrillGroup;
import gcode.model.GAction;
import gcode.model.GTools;
import gcode.model.Group;
import gcode.model.IAction;
import gcode.model.IGroupAction;
import gcode.model.IPart;
import gcode.model.IXYAction;
import gcode.model.Part;
import gcode.model.Pause;
import gcode.model.XYAction;
import tools.ConfigTool;

public class DrillGCodeGraphics extends AbstractGCodeGraphics {
	private static final double SURFACE = 0;
	private double drillSafeDeepth = 10;
	
	private int downSpeed = 1200;
	private int drillSpeed = 200;
	private double drillDeepth = -2.5;
	private DrillGroup currentGroup;
	
	private HashMap<Integer, DrillGroup> groups = new HashMap<>();
	public DrillGCodeGraphics( int ppi) {
		super(ppi);
		this.drillSafeDeepth = ConfigTool.getInstance().drillSafeDeepth;
		this.downSpeed = ConfigTool.getInstance().downSpeed;
		this.drillSpeed = ConfigTool.getInstance().drillSpeed;
		this.drillDeepth = ConfigTool.getInstance().drillDeepth;
		parts.add(new GAction('%',0));	
		parts.add(new GAction('G',90));	
		parts.add(new GAction('G',20));	
		mainPart = new Part('(', 0, "Drill");
		parts.add(mainPart);
	}

	@Override
	public void fillOval(int x, int y, int width, int height) {
		ICNCStroke s = (ICNCStroke) getStroke();
		DrillGroup g = groups.get(new Integer(s.getDrillTool()));
		if(g == null) {
			g = _newTool(s);
			g.add(new GAction('M', 1));//sleep

		}
		double deepth = s.getDeepth();
		_drill(g, x + width/2,y+height/2, deepth >=0 ? drillDeepth: deepth);
	}
	
	private DrillGroup _newTool(ICNCStroke s) {
		currentGroup = new DrillGroup('T',s.getDrillTool(), s.getToolSize());
		mainPart.addGroup(currentGroup);
		groups.put(new Integer(s.getDrillTool()), currentGroup);
		return currentGroup;
	}
	
	private void _drill(IGroupAction group, double x ,double y, double deepth) {
		group.add(GTools.updownDrill(drillSafeDeepth));
		group.add(GTools.moveTo(group, toInch(x), toInch(y)));
		group.add(GTools.drillTo(downSpeed, SURFACE));
		
		group.add(GTools.drillTo(drillSpeed, deepth));
		group.add(GTools.drillTo(downSpeed, SURFACE));
	}

	@Override
	public void dispose() {
		currentGroup.add(GTools.updownDrill(drillSafeDeepth));
		GTools.upPen(currentGroup);
		GTools.moveTo(currentGroup, 0, 0);
		currentGroup.add(new GAction('M',2));
		parts.add(new GAction('%',0));
	}
	/**
	 * ======================================unsupported methods=========================
	 */
	
	public void drawPolyLine(IPolyLine pl) {
		throw new Error("Un-supported method!");
	}
	
	@Override
	public void fillRect(int x, int y, int width, int height) {
		throw new Error("Un-supported method!");
	}

	@Override
	public void drawLine(int x1, int y1, int x2, int y2) {
		throw new Error("Un-supported method!");
	}
	@Override
	public void fillPolygon(int[] xPoints, int[] yPoints, int nPoints) {
		throw new Error("Un-supported method!");
	}
	
	@Override
	public void fillPolygon(Polygon p) {
		throw new Error("Un-supported method!");
	}
}
