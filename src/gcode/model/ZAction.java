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
		String ret = super.toString();
		return ret + " Z"+DEC_FMT.format(z);
	}
}
