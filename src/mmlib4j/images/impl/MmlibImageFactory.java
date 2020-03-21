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
public class MmlibImageFactory extends AbstractImageFactory {
	
	
	//////////////////////////////////////////////////////////
	//                   create news images
	////////////////////////////////////////////////////////
	public GrayScaleImage createGrayScaleImage(int depth, int width, int height){
		if(depth == DEPTH_8BITS)
			return new ByteImage(width, height);
		else if(depth == DEPTH_16BITS)
			return new ShortImage(width, height);
		else if(depth == DEPTH_32BITS)
			return new IntegerImage(width, height);
		return  null;
	}
	
	public BinaryImage createBinaryImage(int width, int height){
		return new BitImage(width, height);
	}
	

	public ColorImage createColorImage(int width, int height){
		return new RGBImage(width, height);
	}
	

	public RealImage createRealImage(int width, int height){
		return new FloatImage(width, height);
	}
	
	
	//////////////////////////////////////////////////////////
	//      create copy for the object pixels[] ///
	/////////////////////////////////////////////////////////	
	public ColorImage createCopyColorImage(GrayScaleImage img){
		return new RGBImage(img);
	}
	
	public ColorImage createCopyColorImage(BinaryImage img){
		return new RGBImage(img);
	}
	
	public ColorImage createCopyColorImage(ColorImage img){
		return img.duplicate();
	}
	
	public GrayScaleImage createCopyGrayScaleImage(GrayScaleImage img) {
		return img.duplicate();
	}
	
	public BinaryImage createCopyBinaryImage(BinaryImage img) {
		return img.duplicate();
	}

	
	public RealImage createCopyRealImage(RealImage img) {
		return img.duplicate();
	}
	
	//////////////////////////////////////////////////////////
	//      create new references for the object pixels[] ///
	/////////////////////////////////////////////////////////
	public ColorImage createReferenceColorImage(int pixels[], int width, int height){
		return new RGBImage(pixels, width, height);
	}
	
	public RealImage createReferenceRealImage(float pixels[], int width, int height){
		return new FloatImage(pixels, width, height);
	}

	public BinaryImage createReferenceBinaryImage(boolean pixels[], int width, int height){
		return new BitImage(pixels, width, height);
	}

	public GrayScaleImage createReferenceGrayScaleImage(int depth, Object pixels, int width, int height) {
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
