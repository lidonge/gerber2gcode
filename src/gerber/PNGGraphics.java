package gerber;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Stroke;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageOutputStream;

public class PNGGraphics extends FileGraphics {
	private BufferedImage image;
	
	public PNGGraphics(String filename,int ppi,double border) {
		super(filename, ppi,border);
	}
	
	@Override
	public void initGraphics(int x, int y,int w, int h) {
		super.initGraphics(x,y,w, h);
		newImageFile(this.clipWidth + border*2,this.clipHeight+border*2);
	}
	
	@Override
	public void dispose() {
		saveImageFile(filename, ppi);
	}

	private void newImageFile(int imgw, int imgh) {
		this.image = new BufferedImage(imgw, imgh, BufferedImage.TYPE_INT_RGB);
		this.g2d = image.createGraphics();
		this.g2d.setBackground(Color.white);
	}

	private void setDPI(IIOMetadata metadata, int DPI) throws IIOInvalidTreeException {
	    // for PMG, it's dots per millimeter
		double INCH_2_MM = 25.4; 
	    double dotsPerMilli = (double)DPI / INCH_2_MM;

	    IIOMetadataNode horiz = new IIOMetadataNode("HorizontalPixelSize");
	    horiz.setAttribute("value", Double.toString(dotsPerMilli));

	    IIOMetadataNode vert = new IIOMetadataNode("VerticalPixelSize");
	    vert.setAttribute("value", Double.toString(dotsPerMilli));

	    IIOMetadataNode dim = new IIOMetadataNode("Dimension");
	    dim.appendChild(horiz);
	    dim.appendChild(vert);

	    IIOMetadataNode root = new IIOMetadataNode("javax_imageio_1.0");
	    root.appendChild(dim);

	    metadata.mergeTree("javax_imageio_1.0", root);
	}


	private void saveGridImage(BufferedImage gridImage, File output, int ppi) throws IOException {
	    output.delete();

	    final String formatName = "png";

	    for (Iterator<ImageWriter> iw = ImageIO.getImageWritersByFormatName(formatName); iw.hasNext();) {
	    	ImageWriter writer = iw.next();
	        ImageWriteParam writeParam = writer.getDefaultWriteParam();
	        ImageTypeSpecifier typeSpecifier = ImageTypeSpecifier.createFromBufferedImageType(BufferedImage.TYPE_INT_RGB);
	        IIOMetadata metadata = writer.getDefaultImageMetadata(typeSpecifier, writeParam);
	        if (metadata.isReadOnly() || !metadata.isStandardMetadataFormatSupported()) {
	           continue;
	        }

	        setDPI(metadata, ppi);

	        final ImageOutputStream stream = ImageIO.createImageOutputStream(output);
	        try {
	           writer.setOutput(stream);
	           writer.write(metadata, new IIOImage(gridImage, null, metadata), writeParam);
	        } finally {
	           stream.close();
	        }
	        break;
	    }
	}


	private void saveImageFile(String filename, int ppi) {
		// save the buffered image
		try {
		    File outputfile = new File(filename);
		    saveGridImage(this.image, outputfile, ppi);
		} catch (IOException e) {
			System.out.println("Error (8): "+e);
		}
		System.out.println("Output image saved...");
	}

}
