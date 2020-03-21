package mmlib4j.images.impl;

import mmlib4j.images.BinaryImage;
import mmlib4j.images.ColorImage;
import mmlib4j.images.GrayScaleImage;
import mmlib4j.images.RealImage;

public class ImageFactory {
	
	public static final int DEPTH_32BITS = 32;
	public static final int DEPTH_16BITS = 16;
	public static final int DEPTH_8BITS = 8;
	
	//////////////////////////////////////////////////////////
	//                   create news images
	////////////////////////////////////////////////////////
	public static GrayScaleImage createGrayScaleImage(int depth, int width, int height){
		return AbstractImageFactory.getInstance().createGrayScaleImage(depth, width, height);
	}
	/*
	public static GrayScaleImage createGrayScaleImage(int width, int height) {
		return createGrayScaleImage(DEPTH_8BITS, width, height);
	}
	*/
	
	public static BinaryImage createBinaryImage(int width, int height){
		return AbstractImageFactory.getInstance().createBinaryImage(width, height);
	}
	

	public static ColorImage createColorImage(int width, int height){
		return AbstractImageFactory.getInstance().createColorImage(width, height);
	}
	

	public static RealImage createRealImage(int width, int height){
		return AbstractImageFactory.getInstance().createRealImage(width, height);
	}
	
	
	//////////////////////////////////////////////////////////
	//      create copy for the object pixels[] ///
	/////////////////////////////////////////////////////////	
	public static ColorImage createCopyColorImage(GrayScaleImage img){
		return AbstractImageFactory.getInstance().createCopyColorImage(img);
	}
	
	public static ColorImage createCopyColorImage(BinaryImage img){
		return AbstractImageFactory.getInstance().createCopyColorImage(img);
	}
	
	public static ColorImage createCopyColorImage(ColorImage img){
		return AbstractImageFactory.getInstance().createCopyColorImage(img);
	}
	
	public static GrayScaleImage createCopyGrayScaleImage(GrayScaleImage img) {
		return AbstractImageFactory.getInstance().createCopyGrayScaleImage(img);
	}
	
	public static BinaryImage createCopyBinaryImage(BinaryImage img) {
		return AbstractImageFactory.getInstance().createCopyBinaryImage(img);
	}

	
	public static RealImage createCopyRealImage(RealImage img) {
		return AbstractImageFactory.getInstance().createCopyRealImage(img);
	}
	
	//////////////////////////////////////////////////////////
	//      create new references for the object pixels[] ///
	/////////////////////////////////////////////////////////
	public static ColorImage createReferenceColorImage(int pixels[], int width, int height){
		return AbstractImageFactory.getInstance().createReferenceColorImage(pixels, width, height);		
	}
	
	public static RealImage createReferenceRealImage(float pixels[], int width, int height){
		return AbstractImageFactory.getInstance().createReferenceRealImage(pixels, width, height);
	}

	public static BinaryImage createReferenceBinaryImage(boolean pixels[], int width, int height){
		return AbstractImageFactory.getInstance().createReferenceBinaryImage(pixels, width, height);
	}

	public static GrayScaleImage createReferenceGrayScaleImage(int depth, Object pixels, int width, int height) {
		return AbstractImageFactory.getInstance().createReferenceGrayScaleImage(depth, pixels, width, height);
	}
	
}