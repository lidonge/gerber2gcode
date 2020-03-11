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

public class AxiDrawPainter extends GerberPainter {
	public AxiDrawPainter(String filename, int ppi, boolean mirroVertical, boolean mirroHorizontal) {
		super(filename, ppi, mirroVertical, mirroHorizontal);
		this.g2d = new PenGCodeGraphics(ppi);
	}

	@Override
	public void initGraphics(int x, int y,int w, int h) {
		super.initGraphics(x,y,w, h);
		this.moveX = 0;
		this.moveY = -this.clipHeight -border*2 ;//+ clipY;
	}

	public void drawLocatingHole(double diameter) {
		drawLocatingHole(diameter,true);
	}
	
	@Override
	public void drawPolyLine(IPolyLine pl) {
		GerberBoard.PolyLine p = new GerberBoard.PolyLine();
		for(int i = 0;i<pl.lines();i++) {
			ILine l1 = pl.get(i);
			GerberBoard.Line l = new GerberBoard.Line(this.convertX(l1.getStartX(),0),this.convertY(l1.getStartY(),0),
					this.convertX(l1.getEndX(),0),this.convertY(l1.getEndY(),0),l1.getThick());
			p.addOnly(l);
		}

		((PenGCodeGraphics)g2d).drawPolyLine(p);
	}
}
