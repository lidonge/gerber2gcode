package gcode.model;

public class ZAction extends GAction implements IZAction {
	private double z;
	
	public ZAction(char type, int action, double z) {
		super(type,action);
		this.z = z;
	}
	@Override
	public double getDeepth() {
		return z;
	}


	public String toString() {
		return getType() + ""+DEC_FMT.format(z);
	}
}
