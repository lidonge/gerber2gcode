package gcode.model;

public class GAction implements IAction{
	private char type = 'G';
	private int action = 0;
	private IAction follow;
	
	public GAction(char type, int action) {
		this.type = type;
		this.action = action;
	}
	@Override
	public char getType() {
		return type;
	}

	@Override
	public int getAction() {
		return action;
	}

	public String toString() {
		String ret = toTypeString();
		return ret + toFollowString();
	}
	
	protected String toTypeString() {
		String ret;
		switch(type) {
		case '%':
			ret = "" + type;
			break;
		default:
			ret = ""+type+(action <10 ? "0" : "")+action;
		}
		return ret;
	}
	protected String toFollowString() {
		return  (follow == null ? "" : " " + follow);
	}
	@Override
	public IAction getFollow() {
		return follow;
	}
	@Override
	public void setFollow(IAction action) {
		this.follow = action;
	}
}
