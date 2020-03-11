package gerber;

import java.awt.Shape;
import java.awt.Stroke;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import tools.ConfigTool;

public class CNCCarvingPainter extends GerberPainter {

	public CNCCarvingPainter(String filename, int ppi, boolean mirroVertical, boolean mirroHorizontal) {
		super(filename, ppi, mirroVertical, mirroHorizontal);
		this.setGraphics(new DrillGCodeGraphics(ppi));
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
}
