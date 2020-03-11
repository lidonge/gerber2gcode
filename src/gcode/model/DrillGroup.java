package gcode.model;

import java.util.ArrayList;

public class DrillGroup extends GAction implements IGroupAction {
	double size;
	public DrillGroup(char type, int action,  double size) {
		super(type, action);
		this.size = size;
	}
	private ArrayList<IAction> list = new ArrayList<>();
	@Override
	public void add(IAction act) {
		list.add(act);
	}
	public String toString() {
		String ret = "\n/Tool: " + getAction() + "; Dia: " + this.DEC_FMT.format(size) + " inchs\n";
		for(int i = 0;i<list.size();i++) {
			if(i != 0)
				ret +="\n";
			ret += list.get(i);
		}
		return ret;
	}
}
