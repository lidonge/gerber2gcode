package gerber;

public interface IGerberBoard {
	public void clear();

	public void circle(int x, int y, int diameter);

	public void circle(int x, int y, int diameter, int drillTool, double toolSize, boolean inverted);


	public void rect(int x, int y, int width, int height);

	public void polygon( IShape p);

	public void line( int startx, int starty, int endx, int endy, int thick, boolean inverted);

//	public void drawAndWritePNG(String filename, int ppi, int border, boolean negative);

}
