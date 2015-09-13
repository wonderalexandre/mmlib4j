package mmlib4j.images.impl;

import mmlib4j.images.BinaryImage;
import mmlib4j.images.ColorImage;
import mmlib4j.images.GrayScaleImage;
import mmlib4j.images.RealImage;


/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 */
public class ImageFactory {

    public static final int DEPTH_32BITS = 32;
	public static final int DEPTH_16BITS = 16;
	public static final int DEPTH_8BITS = 8;
	
	public static GrayScaleImage createGrayScaleImage(int depth, int width, int height){
		if(depth == DEPTH_8BITS)
			return new ByteImage(width, height);
		if(depth == DEPTH_16BITS)
			return new ShortImage(width, height);
		else if(depth == DEPTH_32BITS){
			return new IntegerImage(width, height);
		}
		return  null;
	}
	
	public static GrayScaleImage createGrayScaleImage(int width, int height) {
		return createGrayScaleImage(8, width, height);
	}
	
	public static GrayScaleImage createGrayScaleImage(int depth, Object pixels, int width, int height) {
		if(depth == DEPTH_8BITS)
			return new ByteImage((byte[])pixels, width, height);
		if(depth == DEPTH_16BITS)
			return new ShortImage((short[])pixels, width, height);
		else if(depth == DEPTH_32BITS){
			return new IntegerImage((int[])pixels, width, height);
		}
		return  null;
	}
	
	public static GrayScaleImage createGrayScaleImage(GrayScaleImage img) {
		if(img.getDepth() == DEPTH_8BITS)
			return new ByteImage(img.getWidth(), img.getHeight());
		else if(img.getDepth() == DEPTH_16BITS)
			return new ShortImage(img.getWidth(), img.getHeight());
		else if(img.getDepth() == DEPTH_32BITS){
			return new IntegerImage(img.getWidth(), img.getHeight());
		}
		return  null;
	}
	
	public static BinaryImage createBinaryImage(int width, int height){
		return new BitImage(width, height);
	}
	

	public static BinaryImage createBinaryImage(boolean pixels[], int width, int height){
		return new BitImage(pixels, width, height);
	}
	
	public static ColorImage createColorImage(int width, int height){
		return new RGBImage(width, height);
	}
	
	public static ColorImage createColorImage(int pixels[], int width, int height){
		return new RGBImage(pixels, width, height);
	}
	
	public static ColorImage createColorImage(GrayScaleImage img){
		return new RGBImage(img);
	}
	
	public static ColorImage createColorImage(BinaryImage img){
		return new RGBImage(img);
	}
	
	public static RealImage createFloatImage(int width, int height){
		return new FloatImage(width, height);
	}
	
	public static RealImage createFloatImage(float pixels[], int width, int height){
		return new FloatImage(pixels, width, height);
	}
	
	
}
