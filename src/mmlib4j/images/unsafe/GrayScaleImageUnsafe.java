package mmlib4j.images.unsafe;

import mmlib4j.images.GrayScaleImage;

public interface GrayScaleImageUnsafe extends GrayScaleImage {
	
	public long getAddress();
	
	public int [] getShape();
	
	public String getType();
	
	public int getOrder();

}
