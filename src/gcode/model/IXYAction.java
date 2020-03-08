package gcode.model;

import java.awt.geom.Point2D;

public interface IXYAction extends IAction{

	public Point2D getPosition();
	public double getX();
	public double getY();
}
