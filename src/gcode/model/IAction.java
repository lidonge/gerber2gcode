package gcode.model;

import java.text.DecimalFormat;

public interface IAction {
	public static final DecimalFormat INT_FMT = new DecimalFormat("###");
	public static final DecimalFormat DEC_FMT = new DecimalFormat("##0.00000");
	
	/**
	 * Get the type of action, M-code or G-code
	 * @return 'M' ,'G', 'F'
	 */
	public char getType();
	
	//Code number
	public int getAction();
	
	public IAction getFollow();
	public void setFollow(IAction action);
}
