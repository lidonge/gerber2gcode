package gcode.reader;

import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.StringTokenizer;

import gcode.model.GAction;
import gcode.model.Group;
import gcode.model.IAction;
import gcode.model.IGroupAction;
import gcode.model.IPart;
import gcode.model.IZAction;
import gcode.model.Part;
import gcode.model.XYAction;
import gcode.model.ZAction;

public class GReader {
	private ArrayList<IAction> parts = new ArrayList<>();
	private int up_z = 5;
	public void parse(Reader reader) throws IOException {
		BufferedReader in = new BufferedReader(reader);
		String line;
		IPart curPart = null;
		IGroupAction curGroup = null;
		while((line = in.readLine()) != null) {
			IAction action = parseAction(line);
			switch(action.getType()) {
			case '(':
				curPart = (IPart) action;
				parts.add(curPart);
				break;
			case 'G':
				if(action instanceof IZAction) {
					IZAction zact = (IZAction) action;
					if(zact.getDeepth() == up_z) {//up the drill
						curGroup = new Group('P',up_z);
						curPart.addGroup(curGroup);
					}
				}
				break;
			case 'M':
			case 'F':
			default:
				break;
			}
			if(curGroup == null) {
				parts.add(action);
			}else {
				curGroup.add(action);
			}
		}
	}
	
	private IAction parseAction(String line) {
		IAction ret = null;
		StringTokenizer st = new StringTokenizer(line," ");
		if(st.hasMoreElements()) {
			String token = st.nextToken().trim();
//System.out.println("===" +token);
			char type = token.charAt(0);
			if(type == '(') {
				ret = new Part(type, 0, line.substring(1, line.length() - 1));
			}else if(type == '%') {
				ret = new GAction(type, 0);
			}else {
				int action = new Integer(token.substring(1)).intValue();
				switch(type) {
				case 'G':
					if(st.hasMoreElements()) {
						token = st.nextToken().trim();
//						System.out.println("***" +token);
						if(token.charAt(0) == 'Z') {
							ret = new ZAction(type,action,
									Double.parseDouble(token.substring(1)));
						}else {
							Point2D p = new Point2D.Double();
							parseXY(token, p);
							parseXY(st.nextToken(), p);
							ret = new XYAction(type,action,p);
						}
					}else {
						ret = new GAction(type,action);
					}
					break;
//				case 'M':
//					break;
//				case 'F':
//					break;
				default:
					ret = new GAction(type,action);
					break;
				}
			}
		}
		return  ret;
	}
	
	private void parseXY(String token, Point2D p) {
		char pos = token.charAt(0);
		double d = Double.parseDouble(token.substring(1));
		if(pos == 'X' || pos == 'x') {
			p.setLocation(d, p.getY());
		}else if(pos == 'Y' || pos == 'y') {
			p.setLocation(p.getX(), d);
		}
	}
	
	public String toString() {
		String ret = "";
		for(int i =0;i<parts.size();i++) {
			ret += parts.get(i);
			if(ret.charAt(ret.length() -1) != '\n')
				ret +="\n";
		}
		return ret;
	}
	
	public static void main(String[] args) {
		try {
			FileReader reader = new FileReader(args[0]);
			GReader gr = new GReader();
			gr.parse(reader);
			System.out.println(gr);
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
