package gcode.filler;

import java.awt.geom.Point2D;
import java.util.ArrayList;

import gcode.model.IGroupAction;
import gcode.model.IXYAction;
import gcode.model.XYAction;

public class GFiller implements IGFiller{
//	IGroupAction orgGroup;
	private double penSize = 0.2;//mm
	public GFiller(int penSize) {
		this.penSize = penSize;
	}
	@Override
	public double getPenSize() {
		return penSize;
	}
	public ArrayList<IGroupAction> fillAGroup(IGroupAction group){
		ArrayList<IGroupAction> ret = new ArrayList<>();
		return ret;
	}

	public IXYAction[] Expand(IXYAction[] polygon, double expand)
	{
		IXYAction[] new_polygon = new IXYAction[polygon.length];
	 
	    int len = polygon.length;
	    for (int i = 0; i < len; i++)
	    {
	    	IXYAction p = polygon[i];
	    	IXYAction p1 = polygon[i == 0 ? len - 1 : i - 1];
	    	IXYAction p2 = polygon[i == len - 1 ? 0 : i + 1];
	 
	        double v1x = p1.getX() - p.getX();
	        double v1y = p1.getY() - p.getY();
	        double n1 = norm(v1x, v1y);
	        v1x /= n1;
	        v1y /= n1;
	 
	        double v2x = p2.getX() - p.getX();
	        double v2y = p2.getY() - p.getY();
	        double n2 = norm(v2x, v2y);
	        v2x /= n2;
	        v2y /= n2;
	 
	        double l = -expand / Math.sqrt((1 - (v1x * v2x + v1y * v2y)) / 2);
	 
	        double vx = v1x + v2x;
	        double vy = v1y + v2y;
	        double n = l / norm(vx, vy);
	        vx *= n;
	        vy *= n;
	 
	        new_polygon[i] = new XYAction(p.getType(),p.getAction(),
	        		new Point2D.Double(vx + p.getX(), vy + p.getY()));
	    }
	 
	    return new_polygon;
	}
	 
	private double norm(double x, double y)
	{
	    return Math.sqrt(x * x + y * y);
	}
}
