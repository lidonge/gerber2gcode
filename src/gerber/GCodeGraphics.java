package gerber;

import java.awt.Color;
import java.awt.Polygon;
import java.awt.Stroke;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import gcode.model.GAction;
import gcode.model.GTools;
import gcode.model.Group;
import gcode.model.IAction;
import gcode.model.IPart;
import gcode.model.Part;

public class GCodeGraphics extends FileGraphics {
	public GCodeGraphics(String filename, int ppi, int border, double penDim, boolean negative) {
		super(filename, ppi, border, negative);
		this.g2d = new PenGCodeGraphics(penDim, ppi);
	}
	public GCodeGraphics(String filename, int ppi, int border, boolean negative) {
		super(filename, ppi, border, negative);
		this.g2d = new PenGCodeGraphics(ppi);
	}

	@Override
	public void initGraphics(int w, int h) {
		super.initGraphics(w, h);
	}
	protected int convertY(int y, int height) {
//		return 0-(border+y+height);
//		return this.clipHeight-(border+y+height);
		return y -this.clipHeight;
//		return y;
	}
	@Override
	public void dispose() {
		super.dispose();
		String s = g2d.toString();
		try {
			FileOutputStream fout = new FileOutputStream(filename);
			fout.write(s.getBytes());
			fout.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void drawPolyLine(IPolyLine pl) {
		GerberBoard.PolyLine p = new GerberBoard.PolyLine();
		for(int i = 0;i<pl.lines();i++) {
			ILine l1 = pl.get(i);
			GerberBoard.Line l = new GerberBoard.Line(l1.getStartX(),this.convertY(l1.getStartY(),0),
					l1.getEndX(),this.convertY(l1.getEndY(),0),l1.getThick());
			p.addOnly(l);
		}

		((PenGCodeGraphics)g2d).drawPolyLine(p);
	}
}
