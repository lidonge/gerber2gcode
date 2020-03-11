package gcode.model;

import java.awt.geom.Point2D;

public class GTools {
	public static final void upPen(IGroupAction group) {
		group.add(new GAction('M',5));
		group.add(new Pause("0.2"));
	}

	public static final void downPen(IGroupAction group) {
		group.add(new GAction('M',3));
		group.add(new Pause("0.2"));
	}
	
	public static final IAction moveTo(IGroupAction group, double x, double y) {
		IAction ret = new XYAction('G',0,new Point2D.Double(x, y));
		group.add(ret);
		return ret;
	}
	
	public static final IAction updownDrill( double d) {
		GAction g = new GAction('G',0);
		IZAction down = new ZAction('Z',0,d);
		g.setFollow(down);
		return g;
	}
	public static final IAction drillTo(int speed, double d) {
		GAction f = new GAction('F' , speed);
		GAction g = new GAction('G',1);
		ZAction z = new ZAction('Z',0, d);
		f.setFollow(g);
		g.setFollow(z);
		return f;
	}
	public static final void lineTo(IGroupAction group, double x, double y, int speed) {
		XYAction act = new XYAction('G',1,new Point2D.Double(x, y));
		if(speed != -1) {
			act.setFollow(new GAction('F',speed));
		}
		group.add(act);
	}
}
