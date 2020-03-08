package gcode.model;

public class Pause extends GAction {
	private String time = "0.2";
	public Pause( String time) {
		super('G', 4);
		this.time = time;
		// TODO Auto-generated constructor stub
	}
	
	public String toString() {
		String ret = super.toString();
		ret += " P" + time;
		return ret;
	}
}
