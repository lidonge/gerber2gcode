package gcode.model;

public interface IPart extends IAction{
	public String getName();
	public void addGroup(IGroupAction g);
	public int groups();
	public IGroupAction getGroup(int index);
}
