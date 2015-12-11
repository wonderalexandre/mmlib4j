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
	
	//////////////////////////////////////////////////////////
	//                   create news images
	////////////////////////////////////////////////////////
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
	/*
	public static GrayScaleImage createGrayScaleImage(int width, int height) {
		return createGrayScaleImage(DEPTH_8BITS, width, height);
	}
	*/
	
	public static BinaryImage createBinaryImage(int width, int height){
		return new BitImage(width, height);
	}
	

	public static ColorImage createColorImage(int width, int height){
		return new RGBImage(width, height);
	}
	

	public static RealImage createRealImage(int width, int height){
		return new FloatImage(width, height);
	}
	
	
	//////////////////////////////////////////////////////////
	//      create copy for the object pixels[] ///
	/////////////////////////////////////////////////////////	
	public static ColorImage createCopyColorImage(GrayScaleImage img){
		return new RGBImage(img);
	}
	
	public static ColorImage createCopyColorImage(BinaryImage img){
		return new RGBImage(img);
	}
	
	public static ColorImage createCopyColorImage(ColorImage img){
		return img.duplicate();
	}
	
	public static GrayScaleImage createCopyGrayScaleImage(GrayScaleImage img) {
		return img.duplicate();
	}
	
	public static BinaryImage createCopyBinaryImage(BinaryImage img) {
		return img.duplicate();
	}

	
	public static RealImage createCopyRealImage(RealImage img) {
		return img.duplicate();
	}
	
	//////////////////////////////////////////////////////////
	//      create new references for the object pixels[] ///
	/////////////////////////////////////////////////////////
	public static ColorImage createReferenceColorImage(int pixels[], int width, int height){
		return new RGBImage(pixels, width, height);
	}
	
	public static RealImage createReferenceRealImage(float pixels[], int width, int height){
		return new FloatImage(pixels, width, height);
	}

	public static BinaryImage createReferenceBinaryImage(boolean pixels[], int width, int height){
		return new BitImage(pixels, width, height);
	}

	public static GrayScaleImage createReferenceGrayScaleImage(int depth, Object pixels, int width, int height) {
		if(depth == DEPTH_8BITS)
			return new ByteImage((byte[])pixels, width, height);
		if(depth == DEPTH_16BITS)
			return new ShortImage((short[])pixels, width, height);
		else if(depth == DEPTH_32BITS){
			return new IntegerImage((int[])pixels, width, height);
		}
		return  null;
	}
	
	
	
}
