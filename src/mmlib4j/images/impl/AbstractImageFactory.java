package mmlib4j.images.impl;

import mmlib4j.images.BinaryImage;
import mmlib4j.images.ColorImage;
import mmlib4j.images.GrayScaleImage;
import mmlib4j.images.RealImage;

public abstract class AbstractImageFactory {
	
	public static final int DEPTH_32BITS = 32;
	public static final int DEPTH_16BITS = 16;
	public static final int DEPTH_8BITS = 8;
	
	public static AbstractImageFactory instance = MmlibImageFactory.instance;
	
	public abstract GrayScaleImage createGrayScaleImage(int depth, int width, int height);
	
	public abstract BinaryImage createBinaryImage(int width, int height);

	public abstract ColorImage createColorImage(int width, int height);

	public abstract RealImage createRealImage(int width, int height);	
	
	//////////////////////////////////////////////////////////
	//      create copy for the object pixels[] ///
	/////////////////////////////////////////////////////////	
	public abstract ColorImage createCopyColorImage(GrayScaleImage img);
	
	public abstract ColorImage createCopyColorImage(BinaryImage img);
	
	public abstract ColorImage createCopyColorImage(ColorImage img);
	
	public abstract GrayScaleImage createCopyGrayScaleImage(GrayScaleImage img);
	
	public abstract BinaryImage createCopyBinaryImage(BinaryImage img);
	
	public abstract RealImage createCopyRealImage(RealImage img);
		
	//////////////////////////////////////////////////////////
	//      create new references for the object pixels[] ///
	/////////////////////////////////////////////////////////
	public abstract ColorImage createReferenceColorImage(int pixels[], int width, int height);
	
	public abstract RealImage createReferenceRealImage(float pixels[], int width, int height);

	public abstract BinaryImage createReferenceBinaryImage(boolean pixels[], int width, int height);

	public abstract GrayScaleImage createReferenceGrayScaleImage(int depth, Object pixels, int width, int height);

}
