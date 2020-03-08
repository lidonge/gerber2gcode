package gerber;

import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.RenderingHints.Key;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.FlatteningPathIterator;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ImageObserver;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderableImage;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.AttributedCharacterIterator;
import java.util.ArrayList;
import java.util.Map;

import gcode.model.GAction;
import gcode.model.GTools;
import gcode.model.Group;
import gcode.model.IAction;
import gcode.model.IPart;
import gcode.model.IXYAction;
import gcode.model.Part;
import gcode.model.XYAction;

public class PenGCodeGraphics extends Graphics2D {
	public static final double ONE_MM = 1d/25.4d;//1mm = 1/10/2.54 inch
	private ArrayList<IAction> parts = new ArrayList<>();
	private IPart mainPart;
	private double penDim = 0.5 * ONE_MM;//inch
	private double overPen = 0.2;
	private Stroke stroke;
	private int ppi;
	private int defaultSpeed = 3000;
	private int lineSpeed = 100;

	public PenGCodeGraphics( int ppi) {
		this(0.5 * ONE_MM, 0.2, ppi,3000, 100);
	}
	public PenGCodeGraphics( double penDim, int ppi) {
		this(penDim, 0.2, ppi,3000, 100);
	}
	public PenGCodeGraphics(double penDim, double overPen, int ppi, int defaultSpeed, int linespeed) {
		this.penDim = penDim;
		this.overPen = overPen;
		this.ppi = ppi;
		this.defaultSpeed = defaultSpeed;
		this.lineSpeed = linespeed;
		parts.add(new GAction('%',0));	
		parts.add(new GAction('G',90));	
		parts.add(new GAction('G',20));	
		parts.add(new GAction('S',1000));	
		parts.add(new GAction('F',defaultSpeed));	
		mainPart = new Part('(', 0, "Isolation");
		parts.add(mainPart);
	}
	@Override
	public void setColor(Color c) {
		//nothing to do
	}

	
	@Override
	public Stroke getStroke() {
		return stroke;
	}

	@Override
	public void setStroke(Stroke s) {
		this.stroke = stroke;
	}
	
	private double toInch(int i) {
		return ((double)i)/this.ppi;
	}
	
	@Override
	public void dispose() {
		Group g = new Group();
		mainPart.addGroup(g);
		GTools.upPen(g);
		GTools.moveTo(g, 0, 0);
		parts.add(new GAction('%',0));
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
	

	@Override
	public void fillRect(int x, int y, int width, int height) {
//		MyLine l = new MyLine(x, y + height/2, x+width, y+height/2, height);
//		PolyLine pl = new PolyLine();
//		pl.add(l, false);
//		this.drawPolyLine(pl);
		Polygon p = new Polygon();
		p.addPoint(x, y);
		p.addPoint(x+width, y);
		p.addPoint(x+width, y+height);
		p.addPoint(x, y+height);
		fillPolygon(p);
	}

	@Override
	public void drawLine(int x1, int y1, int x2, int y2) {
		Group group = this._startDraw(x1, y1);
		this._lineTo(group, x2, y2);
	}
	
	private void _lineTo(Group group, int x2, int y2) {
		GTools.lineTo(group, toInch(x2), toInch(y2), -1);
	}
	
	private void _lineTo(Group group, int x2, int y2, int speed) {
		GTools.lineTo(group, toInch(x2), toInch(y2), speed);
	}
	
	private Group _startDraw(int x1, int y1) {
		Group group = new Group();
		this.mainPart.addGroup(group);
		GTools.upPen(group);
		GTools.moveTo(group, toInch(x1), toInch(y1));
		GTools.downPen(group);
		return group;
	}
	
	private int penPixle() {
		return (int)Math.round(penDim * ppi);
	}
	
	
	@Override
	public void fillPolygon(int[] xPoints, int[] yPoints, int nPoints) {
		Polygon p = new Polygon(xPoints,yPoints,nPoints);
		this.fillPolygon(p);
	}
	
	@Override
	public void fillPolygon(Polygon p) {
		Rectangle r = p.getBounds();
		int width = r.width < r.height ? r.width : r.height;
		int penThick = penPixle();
		int count = calcFillCount(width/2.0d, 0.4);
		Polygon np = p;
		Group g = null;
//		if(ccc == 1)
//			System.out.println(count + ","+penThick+" fillPolly:" + r);
		for(int i = 0;i<count;i++) {
			double exp = i == 0 ? -penThick/2d : -0.5*penThick *(1-overPen);
//			if(ccc == 1)
//				System.out.println(i+"=========exp:" + exp);
			Polygon tmp = np;
			np = expand(np, exp);
			if(np == null) {
				np = tmp;
				i--;
				continue;
			}
			if(i == 0) {
				g = _startDraw(np.xpoints[0],np.ypoints[0]);
			}
			this._drawPolygon(g, np);
		}
	}

	private int calcFillCount(double d) {
		return this.calcFillCount(d, 0);
	}
	private int calcFillCount(double w, double fix) {
		int T = penPixle();
		double k = this.overPen;
		int n = (int) Math.round((w - k*T)/(T - 2*k*T) + fix);
		return n;
	}
	
	private void _drawPolygon(Group group, Polygon p) {
		for(int i = 0;i<p.npoints;i++) {
			_lineTo(group, p.xpoints[i], p.ypoints[i]);
		}
		_lineTo(group, p.xpoints[0], p.ypoints[0]);
	}

	public Polygon expand(Polygon pl, double expand)
	{
		Polygon npl = new Polygon();
	 
	    int len = pl.npoints;
	    for (int i = 0; i < len; i++){
	    	int p = i;
	    	int p1 = i == 0 ? len - 1 : i - 1;
	    	int p2 = i == len - 1 ? 0 : i + 1;
	 
	        double v1x = pl.xpoints[p1] - pl.xpoints[p];
	        double v1y = pl.ypoints[p1] - pl.ypoints[p];

	        
	        double n1 = norm(v1x, v1y);
	        v1x /= n1;
	        v1y /= n1;
	 
	        double v2x = pl.xpoints[p2] - pl.xpoints[p];
	        double v2y = pl.ypoints[p2] - pl.ypoints[p];

	        double n2 = norm(v2x, v2y);
	        v2x /= n2;
	        v2y /= n2;
	 
	        double l = -expand / Math.sqrt((1 - (v1x * v2x + v1y * v2y)) / 2);
	 
	        double vx = v1x + v2x;
	        double vy = v1y + v2y;
	        if((vx == 0 && vy == 0 )) {
	        	removePoint(pl,p);
	        	return null;
	        }
	        double n = l / norm(vx, vy);
//	        if(ccc == 1)
//	        System.out.println("v1(" + v1x+","+v1y+"),v2(" +v2x+","+v2y+"),v("+vx+","+vy+"); n1,n2,n, l:" + n1+","+n2+","+n +"," +l );
	        vx *= n;
	        vy *= n;
//	        if(!pl.getBounds().inside((int)vx, (int)vy)))
	        int x = (int)Math.round(vx + pl.xpoints[p]);
	        int y = (int)Math.round(vy+pl.ypoints[p]);
	        if(!pl.contains(x,y)) {
	        	removePoint(pl,p);
	        	return null;
	        }
	        npl.addPoint(x,y);
//	        if(npl.xpoints[npl.npoints -1] == 0 || npl.xpoints[npl.npoints -1] == 0) {
//	        if(ccc == 1) {
//	        	System.out.println("=======error " +len+" len of index "+p+":" +npl.xpoints[npl.npoints -1] + "," + npl.ypoints[npl.npoints -1]);
//	        	System.out.println("p,p1,p2=" +  pl.xpoints[p] + "," + pl.ypoints[p] +
//	        			";"+pl.xpoints[p1] + "," + pl.ypoints[p1]+
//	        			";"+pl.xpoints[p2] + "," + pl.ypoints[p2]);
//	        }
	    }
	 
	    return npl;
	}
	
	private void removePoint(Polygon pl, int p) {
    	Polygon tmp = new Polygon();
    	for(int j = 0;j<pl.npoints;j++) {
    		if(j == p)
    			continue;
    		tmp.addPoint(pl.xpoints[j], pl.ypoints[j]);
    	}
    	pl.xpoints = tmp.xpoints;
    	pl.ypoints =tmp.ypoints;
    	pl.npoints = tmp.npoints;
		
	}
	 
	private double norm(double x, double y){
	    return Math.sqrt(x * x + y * y);
	}
//	private int ccc = 0;
	@Override
	public void fillOval(int x, int y, int width, int height) {
//		ccc++;
//		if(ccc > 1)
//			return;
		Ellipse2D s = new Ellipse2D.Float(x,y,width,height);
		FlatteningPathIterator iter=new FlatteningPathIterator(s.getPathIterator(new AffineTransform()), 1);
//		PathIterator iter = s.getPathIterator(new AffineTransform());
        float[] coords=new float[6];
        Polygon p = new Polygon();
        ArrayList<Point> points = new ArrayList<Point>();
        while (!iter.isDone()) {
            iter.currentSegment(coords);
            int x1=(int)coords[0];
            int y1=(int)coords[1];
            Point p1 = new Point(x1,y1);
            if(!points.contains(p1)) {
	            points.add(p1);
	            p.addPoint(x1, y1);
	            int n = p.npoints;
//	            System.out.println(n+"=="+ p.xpoints[n-1] + "," + p.ypoints[n-1]);
            }
            iter.next();
        }
//        p.addPoint(p.xpoints[0], p.ypoints[0]);
		this.fillPolygon(p);
	}
	

	private static class LineCalculus{
		double ldx,ldy, pdx,pdy, pdx1, pdy1;
		double overPen;
		ILine line;
		LineCalculus(ILine l, double penThick , double overPen){
			this.overPen = overPen;
			this.line = l;
			double dx = l.getEndX() - l.getStartX();
			double dy = l.getEndY() - l.getStartY();
			int lineThick = l.getThick();
			if(dy == 0) {
				ldx = 0;
				ldy = lineThick;
				pdx = 0;
				pdy = penThick;
			}else if(dx == 0){
				ldx = lineThick;
				ldy = 0;
				pdx = penThick;
				pdy = 0;
			}else {
				double nk = dx/dy;
				double theta = Math.atan(nk);
				this.ldx = lineThick*Math.sin(theta);
				this.ldy = lineThick*Math.cos(theta);
				this.pdx = penThick*Math.sin(theta);
				this.pdy = penThick*Math.cos(theta);
			}
//			System.out.println("inin calculus pdy:"+ pdy +" pdx:"+ pdx + " ldx:" +ldx + " ldy:"+ ldy);
		}
		
		int getStartX(int index) {
//			double d = (index +0.5 - index * overPen )*pdx;
//			System.out.println(index +"===pdy:"+ pdy +" pdx:"+ pdx + " d:" + d+ " ldx:" +ldx + " ldy:"+ ldy);
			return (int)Math.round(line.getStartX() + 0.5*pdy - 0.5*ldx + (index +0.5 - index * overPen )*pdx );
		}

		int getStartY(int index) {
			return (int)Math.round(line.getStartY() + 0.5*pdx + 0.5*ldy - (index +0.5 + index * overPen)*pdy);
		}
		
		int getEndX(int index) {
			return (int)Math.round(line.getEndX() - 0.5*pdy - 0.5*ldx + (index +0.5 - index * overPen)*pdx);
		}

		int getEndY(int index) {
			return (int)Math.round(line.getEndY() - 0.5*pdx + 0.5*ldy - (index +0.5 + index * overPen)*pdy);
		}

		int getStartX() {
			return (int)Math.round(line.getStartX() + 0.5*pdy);
		}

		int getStartY() {
			return (int)Math.round(line.getStartY() + 0.5*pdx);
		}
		
		int getEndX() {
			return (int)Math.round(line.getEndX() - 0.5*pdy);
		}

		int getEndY() {
			return (int)Math.round(line.getEndY() - 0.5*pdx);
		}
	}
	
	public void drawPolyLine(IPolyLine pl) {
		int penThick = penPixle();
		int lineThick = pl.get(0).getThick();
		int count = calcFillCount((double)lineThick, 0.4);
//		int count = (int)Math.round(((double)lineThick) / penThick + 0.4);//0.1 = 1; 1.1 = 2
		ArrayList<LineCalculus> calculus = new ArrayList<>();
		for(int i = 0;i<pl.lines();i++) {
			calculus.add(new LineCalculus(pl.get(i), penThick, overPen));
		}
		if(count == 1) {
			LineCalculus l = calculus.get(0);
			Group group = _startDraw(l.getStartX(), l.getStartY());
			for(int i = 0;i<calculus.size();i++) {
				l = calculus.get(i);
				this._lineTo(group, l.getEndX(), l.getEndY(), lineSpeed);
			}
			
//			ILine l = pl.get(0);
//			Group group = _startDraw(l.getStartX(), l.getStartY());
//			for(int i = 0;i<pl.lines();i++) {
//				l = pl.get(i);
//				this._lineTo(group, l.getEndX(), l.getEndY());
//			}
		}else {
			LineCalculus l = calculus.get(0);
			Group group = _startDraw(l.getStartX(0), l.getStartY(0));
			int size = calculus.size();
			for(int j = 0;j<count;j++) {
				boolean forward = j % 2 == 0 ;
//				boolean forward = true;
				int i = forward ? 0 : size - 1;
				if(j != 0) 
				{
					if(forward) {
						l = calculus.get(0);
						this._lineTo(group, l.getStartX(j), l.getStartY(j), lineSpeed);
					}else {
						l = calculus.get(size -1);
						this._lineTo(group, l.getEndX(j), l.getEndY(j), lineSpeed);
					}
				}
				while(true) {
					l = calculus.get(i);
					if(forward) {
						if(i != 0)
							this._lineTo(group, l.getStartX(j), l.getStartY(j), lineSpeed);
						this._lineTo(group, l.getEndX(j), l.getEndY(j), lineSpeed);
						i++;
						if(i >= size)
							break;
					}else {
						if(i != size -1)
							this._lineTo(group, l.getEndX(j), l.getEndY(j), lineSpeed);
						this._lineTo(group, l.getStartX(j), l.getStartY(j), lineSpeed);
						if(i == 0)
							break;
						i--;
					}
				}
			}
		}
	}
	
	
	/**
	 * ======================================unsupported methods=========================
	 */
	@Override
	public void draw(Shape s) {
		throw new Error("Un-implemented method!");
		
	}

	@Override
	public boolean drawImage(Image img, AffineTransform xform, ImageObserver obs) {
		throw new Error("Un-implemented method!");
		
	}

	@Override
	public void drawImage(BufferedImage img, BufferedImageOp op, int x, int y) {
		throw new Error("Un-implemented method!");
		
	}

	@Override
	public void drawRenderedImage(RenderedImage img, AffineTransform xform) {
		throw new Error("Un-implemented method!");
		
	}

	@Override
	public void drawRenderableImage(RenderableImage img, AffineTransform xform) {
		throw new Error("Un-implemented method!");
		
	}
	
	@Override
	public void drawPolygon(int[] xPoints, int[] yPoints, int nPoints) {
		throw new Error("Un-implemented method!");
		
	}
	
	@Override
	public void drawString(String str, int x, int y) {
		throw new Error("Un-implemented method!");
		
	}

	@Override
	public void drawString(String str, float x, float y) {
		throw new Error("Un-implemented method!");
		
	}

	@Override
	public void drawString(AttributedCharacterIterator iterator, int x, int y) {
		throw new Error("Un-implemented method!");
		
	}

	@Override
	public void drawString(AttributedCharacterIterator iterator, float x, float y) {
		throw new Error("Un-implemented method!");
		
	}

	@Override
	public void drawGlyphVector(GlyphVector g, float x, float y) {
		throw new Error("Un-implemented method!");
		
	}

	@Override
	public void fill(Shape s) {
		throw new Error("Un-implemented method!");
		
	}

	@Override
	public boolean hit(Rectangle rect, Shape s, boolean onStroke) {
		throw new Error("Un-implemented method!");
		
	}

	@Override
	public GraphicsConfiguration getDeviceConfiguration() {
		throw new Error("Un-implemented method!");
		
	}

	@Override
	public void setComposite(Composite comp) {
		throw new Error("Un-implemented method!");
		
	}

	@Override
	public void setPaint(Paint paint) {
		throw new Error("Un-implemented method!");
		
	}

	@Override
	public void setRenderingHint(Key hintKey, Object hintValue) {
		throw new Error("Un-implemented method!");
		
	}

	@Override
	public Object getRenderingHint(Key hintKey) {
		throw new Error("Un-implemented method!");
		
	}

	@Override
	public void setRenderingHints(Map<?, ?> hints) {
		throw new Error("Un-implemented method!");
		
	}

	@Override
	public void addRenderingHints(Map<?, ?> hints) {
		throw new Error("Un-implemented method!");
		
	}

	@Override
	public RenderingHints getRenderingHints() {
		throw new Error("Un-implemented method!");
		
	}

	@Override
	public void translate(int x, int y) {
		throw new Error("Un-implemented method!");
		
	}

	@Override
	public void translate(double tx, double ty) {
		throw new Error("Un-implemented method!");
		
	}

	@Override
	public void rotate(double theta) {
		throw new Error("Un-implemented method!");
		
	}

	@Override
	public void rotate(double theta, double x, double y) {
		throw new Error("Un-implemented method!");
		
	}

	@Override
	public void scale(double sx, double sy) {
		throw new Error("Un-implemented method!");
		
	}

	@Override
	public void shear(double shx, double shy) {
		throw new Error("Un-implemented method!");
		
	}

	@Override
	public void transform(AffineTransform Tx) {
		throw new Error("Un-implemented method!");
		
	}

	@Override
	public void setTransform(AffineTransform Tx) {
		throw new Error("Un-implemented method!");
		
	}

	@Override
	public AffineTransform getTransform() {
		throw new Error("Un-implemented method!");
		
	}

	@Override
	public Paint getPaint() {
		throw new Error("Un-implemented method!");
		
	}

	@Override
	public Composite getComposite() {
		throw new Error("Un-implemented method!");
		
	}

	@Override
	public void setBackground(Color color) {
		throw new Error("Un-implemented method!");
		
	}

	@Override
	public Color getBackground() {
		throw new Error("Un-implemented method!");
		
	}

	@Override
	public void clip(Shape s) {
		throw new Error("Un-implemented method!");
		
	}

	@Override
	public FontRenderContext getFontRenderContext() {
		throw new Error("Un-implemented method!");
		
	}

	@Override
	public Graphics create() {
		throw new Error("Un-implemented method!");
		
	}

	@Override
	public Color getColor() {
		throw new Error("Un-implemented method!");
		
	}

	@Override
	public void setPaintMode() {
		throw new Error("Un-implemented method!");
		
	}

	@Override
	public void setXORMode(Color c1) {
		throw new Error("Un-implemented method!");
		
	}

	@Override
	public Font getFont() {
		throw new Error("Un-implemented method!");
		
	}

	@Override
	public void setFont(Font font) {
		throw new Error("Un-implemented method!");
		
	}

	@Override
	public FontMetrics getFontMetrics(Font f) {
		throw new Error("Un-implemented method!");
		
	}

	@Override
	public Rectangle getClipBounds() {
		throw new Error("Un-implemented method!");
		
	}

	@Override
	public void clipRect(int x, int y, int width, int height) {
		throw new Error("Un-implemented method!");
		
	}

	@Override
	public void setClip(int x, int y, int width, int height) {
		throw new Error("Un-implemented method!");
		
	}

	@Override
	public Shape getClip() {
		throw new Error("Un-implemented method!");
		
	}

	@Override
	public void setClip(Shape clip) {
		throw new Error("Un-implemented method!");
		
	}

	@Override
	public void copyArea(int x, int y, int width, int height, int dx, int dy) {
		throw new Error("Un-implemented method!");
		
	}

	@Override
	public void clearRect(int x, int y, int width, int height) {
		throw new Error("Un-implemented method!");
		
	}

	@Override
	public void drawRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight) {
		throw new Error("Un-implemented method!");
		
	}

	@Override
	public void fillRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight) {
		throw new Error("Un-implemented method!");
		
	}

	@Override
	public void drawOval(int x, int y, int width, int height) {
		throw new Error("Un-implemented method!");
		
	}

	@Override
	public void drawArc(int x, int y, int width, int height, int startAngle, int arcAngle) {
		throw new Error("Un-implemented method!");
		
	}

	@Override
	public void fillArc(int x, int y, int width, int height, int startAngle, int arcAngle) {
		throw new Error("Un-implemented method!");
		
	}

	@Override
	public void drawPolyline(int[] xPoints, int[] yPoints, int nPoints) {
		throw new Error("Un-implemented method!");
		
	}

	@Override
	public boolean drawImage(Image img, int x, int y, ImageObserver observer) {
		throw new Error("Un-implemented method!");
		
	}

	@Override
	public boolean drawImage(Image img, int x, int y, int width, int height, ImageObserver observer) {
		throw new Error("Un-implemented method!");
		
	}

	@Override
	public boolean drawImage(Image img, int x, int y, Color bgcolor, ImageObserver observer) {
		throw new Error("Un-implemented method!");
		
	}

	@Override
	public boolean drawImage(Image img, int x, int y, int width, int height, Color bgcolor, ImageObserver observer) {
		throw new Error("Un-implemented method!");
		
	}

	@Override
	public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2,
			ImageObserver observer) {
		throw new Error("Un-implemented method!");
		
	}

	@Override
	public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2,
			Color bgcolor, ImageObserver observer) {
		throw new Error("Un-implemented method!");
		
	}
}
