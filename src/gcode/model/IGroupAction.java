package gcode.model;

public interface IGroupAction extends IAction{
//	public void addToBegin(IAction act);
//	public void addToEnd(IAction act);
//	public void addToPolygon(IXYAction act);
//	
	public void add(IAction act);
	public IAction[] getBegins();
	public IXYAction[] getPolygon();
	public IAction[] getEnds();
}
