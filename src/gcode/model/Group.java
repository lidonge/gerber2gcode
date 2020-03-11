package gcode.model;

import java.util.ArrayList;

public class Group extends GAction implements IGroupAction {
	public Group() {
		this('P', 0);
	}
	public Group(char type, int action) {
		super(type, action);
	}

	private ArrayList<IAction> begins = new ArrayList<>();
	private ArrayList<IXYAction> polygon = new ArrayList<>();
	private ArrayList<IAction> ends = new ArrayList<>();
	
	private static final int ADDING_BEG = 0;
	private static final int ADDING_XY = 1;
	private static final int ADDING_END = 2;

	private int statue = ADDING_BEG;
//	@Override
//	public IAction[] getBegins() {
//		return (IAction[]) begins.toArray();
//	}
//
//	@Override
//	public IXYAction[] getPolygon() {
//		return (IXYAction[]) polygon.toArray();
//	}
//
//	@Override
//	public IAction[] getEnds() {
//		return (IAction[]) ends.toArray();
//	}
	
	public void add(IAction act) {
		switch(statue) {
		case ADDING_BEG:
			if(act instanceof IXYAction && act.getAction() != 0) {
				statue = ADDING_XY;
				this.polygon.add((IXYAction) act);
			}else
				this.begins.add(act);
			break;
		case ADDING_XY:
			if(act instanceof IXYAction) {
				this.polygon.add((IXYAction) act);
			}else {
				statue = ADDING_END;
				this.ends.add(act);
			}
			break;
		case ADDING_END:
			this.ends.add(act);
			break;
		}
	}
	public String toString() {
		String ret = "";
		for(int i = 0;i<begins.size();i++) {
			if(i != 0)
				ret +="\n";
			ret += begins.get(i);
		}
		for(int i = 0;i<polygon.size();i++) {
			ret +="\n";
			ret += polygon.get(i);
		}
		for(int i = 0;i<ends.size();i++) {
			ret +="\n";
			ret += ends.get(i);
		}
		return ret + "\n";
	}
}
