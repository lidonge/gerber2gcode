package gerber;

import java.awt.Stroke;

public interface ICNCStroke extends Stroke{
	public int getDrillTool();
	public double getToolSize();
	public double getDeepth();
}
