package gerber;

import java.awt.Polygon;

public interface IGerberBoard {
	public void clear();

	public void circle(int x, int y, int diameter);

	public void circle(int x, int y, int diameter, boolean inverted);


	public void rect(int x, int y, int width, int height);

	public void polygon( Polygon p);

	public void line( int startx, int starty, int endx, int endy, int thick, boolean inverted);

//	public void drawAndWritePNG(String filename, int ppi, int border, boolean negative);

}
