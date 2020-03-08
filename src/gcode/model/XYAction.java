package gcode.model;

import java.awt.geom.Point2D;

public class XYAction extends GAction implements IXYAction {
	private Point2D point;
	public XYAction(char type, int action, Point2D point) {
		super(type,action);
		this.point = point;
	}
	@Override
	public Point2D getPosition() {
		return point;
	}

	public String toString() {
		String ret = toTypeString();
		return ret + " X"+DEC_FMT.format(point.getX()) + " Y"+DEC_FMT.format(point.getY()) + toFollowString();
	}
	@Override
	public double getX() {
		return point.getX();
	}
	@Override
	public double getY() {
		return point.getY();
	}
}
