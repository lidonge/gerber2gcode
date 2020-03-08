package gcode.model;

public interface IGCodePainter {
	public void addPart(IAction part);
	public int size();
	public IAction getPart(int index);
	public IPart getPaintPart();
}
