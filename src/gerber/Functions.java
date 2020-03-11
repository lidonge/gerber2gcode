package gerber;
import java.awt.geom.Point2D;
import java.awt.*;
import java.awt.image.*;
import java.util.*;

public class Functions {

	static double PI = Math.PI;
	static double HALF_PI = Math.PI /2.0;
	static double DOUBLE_PI = Math.PI * 2.0;
	
	private static double getAngle(int x1, int y1, int x2, int y2) {
		double res = Math.atan2(y1-y2, x2-x1);
		if (res < 0) return DOUBLE_PI + res; else return res;
	}

	private static double getAngle(double x1, double y1, double x2, double y2) {
		double res = Math.atan2(y1-y2, x2-x1);
		if (res < 0) return DOUBLE_PI + res; else return res;
	}

	
	private static Point2D.Double takeStep(int x, int y, double angle, double step) { 
		return new Point2D.Double(x+Math.sin(angle+HALF_PI)*step, y+Math.cos(angle+HALF_PI)*step);
	}
	
	private static Point2D.Double takeStep(Point2D.Double p, double angle, double step) { 
		return new Point2D.Double(p.x+Math.sin(angle+HALF_PI)*step, p.y+Math.cos(angle+HALF_PI)*step);
	}
	
	
	
	public static Point2D.Double calcStep(int begin_x, int begin_y, int end_x, int end_y, double step) {
		double angle = getAngle(begin_x, begin_y, end_x, end_y);
		return takeStep(begin_x, begin_y, angle, step);
	}

	public static Point2D.Double calcStep(Point2D.Double begin, int end_x, int end_y, double step) {
		double angle = getAngle(begin.x, begin.y, end_x, end_y);
		return takeStep(begin, angle, step);
	}

	
	
	public static double getDistance(int x1, int y1, int x2, int y2) {
		int maxx = Math.max(x1, x2);
		int minx = Math.min(x1, x2);
		int maxy = Math.max(y1, y2);
		int miny = Math.min(y1, y2);
		
		return Math.sqrt( Math.pow((double)(maxx-minx),2) + Math.pow((double)(maxy-miny),2) );
	}

//	public static double getDistance(Point2D.Double p1, int x2, int y2) {
//		double maxx = Math.max(p1.x, x2);
//		double minx = Math.min(p1.x, x2);
//		double maxy = Math.max(p1.y, y2);
//		double miny = Math.min(p1.y, y2);
//		
//		return Math.sqrt( Math.pow(maxx-minx,2) + Math.pow(maxy-miny,2) );
//	}
//	
	public static void floodFill(BufferedImage image, Point node, Color replacementColor) {
		int width = image.getWidth();
		int height = image.getHeight();
		//int target = targetColor.getRGB();
		int target = image.getRGB(node.x, node.y);
		int replacement = replacementColor.getRGB();
		
		if (target != replacement) {
			Deque<Point> queue = new LinkedList<Point>();
		    do {
		        int x = node.x;
		        int y = node.y;
		        while (x > 0 && image.getRGB(x - 1, y) == target) {
		          x--;
		        }
		        boolean spanUp = false;
		        boolean spanDown = false;
		        while (x < width && image.getRGB(x, y) == target) {
		          image.setRGB(x, y, replacement);
		          if (!spanUp && y > 0 && image.getRGB(x, y - 1) == target) {
		            queue.add(new Point(x, y - 1));
		            spanUp = true;
		          } else if (spanUp && y > 0 && image.getRGB(x, y - 1) != target) {
		            spanUp = false;
		          }
		          if (!spanDown && y < height - 1 && image.getRGB(x, y + 1) == target) {
		            queue.add(new Point(x, y + 1));
		            spanDown = true;
		          } else if (spanDown && y < height - 1 && image.getRGB(x, y + 1) != target) {
		            spanDown = false;
		          }
		          x++;
		        }
		      } while ((node = queue.pollFirst()) != null);
		}
	}
		
	
	
}
