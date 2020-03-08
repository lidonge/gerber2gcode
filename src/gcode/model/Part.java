package gcode.model;

import java.util.ArrayList;

public class Part extends GAction implements IPart {
	private String name;
	private ArrayList<IGroupAction> groups = new ArrayList<>();
	
	public Part(char type, int action, String name) {
		super(type, action);
		this.name = name;
	}

	public void addGroup(IGroupAction g) {
		this.groups.add(g);
	}

	@Override
	public int groups() {
		return groups.size();
	}

	@Override
	public IGroupAction getGroup(int index) {
		return groups.get(index);
	}

	@Override
	public String getName() {
		return name;
	}

	public String toString() {
		String ret = "(" + name + ")\n";
		for(int i = 0;i<groups.size();i++) {
			ret += groups.get(i);
		}
		return ret;
	}
}
