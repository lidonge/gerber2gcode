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
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.RenderingHints.Key;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ImageObserver;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderableImage;
import java.text.AttributedCharacterIterator;
import java.util.ArrayList;
import java.util.Map;

import gcode.model.IAction;
import gcode.model.IPart;

public abstract class AbstractGCodeGraphics extends Graphics2D{
	protected ArrayList<IAction> parts = new ArrayList<>();
	protected IPart mainPart;
	private Stroke stroke;
	private Color color; 
	protected int ppi;
	
	public AbstractGCodeGraphics(int ppi) {
		this.ppi = ppi;
	}

	@Override
	public void setColor(Color c) {
		this.color = c;
	}

	@Override
	public Color getColor() {
		return this.color;
	}
	
	@Override
	public Stroke getStroke() {
		return this.stroke;
	}

	@Override
	public void setStroke(Stroke s) {
		this.stroke = s;
	}
	
	protected double toInch(int i) {
		return toInch((double)i);
	}
	
	protected double toInch(double i) {
		return i/this.ppi;
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
