package gerber;

public interface ILine extends IShape{
	public int getStartX();
	public int getStartY();
	public int getEndX();
	public int getEndY();
	public int getThick();
	public double getK();
}
