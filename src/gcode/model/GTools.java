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
	
	public static final void moveTo(IGroupAction group, double x, double y) {
		group.add(new XYAction('G',0,new Point2D.Double(x, y)));
	}
	
	public static final void lineTo(IGroupAction group, double x, double y, int speed) {
		XYAction act = new XYAction('G',1,new Point2D.Double(x, y));
		if(speed != -1) {
			act.setFollow(new GAction('F',speed));
		}
		group.add(act);
	}
}
