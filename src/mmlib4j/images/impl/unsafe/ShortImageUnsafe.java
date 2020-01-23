package mmlib4j.images.impl.unsafe;

import mmlib4j.images.impl.AbstractGrayScale;
import mmlib4j.images.impl.ShortImage;
import mmlib4j.images.unsafe.GrayScaleImageUnsafe;
import ndarrays4j.arrays.integer.NdShortArray;

public class ShortImageUnsafe extends AbstractGrayScale implements GrayScaleImageUnsafe {
		
	public NdShortArray pixels;
	
	public ShortImageUnsafe(NdShortArray pixels, int width, int height) {
		this.pixels = pixels;
		this.width = width;
		this.height = height;
	}
	
	public ShortImageUnsafe(int width, int height) {
		this.pixels = new NdShortArray(new int [] {width, height});
		this.width = width;
		this.height = height;
	}
	
	public ShortImageUnsafe(long address, int width, int height) {
		this.pixels = new NdShortArray(address, new int[] {width, height});
		this.width = width;
		this.height = height;
	}
	
	public ShortImageUnsafe(long address, int width, int height, int order) {
		this.pixels = new NdShortArray(address, new int[] {width, height}, order);
		this.width = width;
		this.height = height;
	}
	
	@Override
	public String getType() {	
		return "ShortImage";
	}	
	
	@Override
	public int [] getShape() {
		return this.pixels.shape();
	}
	
	@Override
	public long getAddress() {
		return pixels.address();
	}

	@Override
	public int getDepth() {
		return 17;
	}

	@Override
	public int getPixel(int i) {
		return ShortImage.toInt(pixels.get(i));
	}

	@Override
	public int getPixel(int x, int y) {
		return ShortImage.toInt(pixels.get(x, y));
	}

	@Override
	public void setPixel(int i, int value) {			
		pixels.set(ShortImage.toShort(value), i);		
	}

	@Override
	public void setPixel(int x, int y, int value) {
		//setPixel(y * width + x, value);
		pixels.set(ShortImage.toShort(value), x, y);
	}
	
	@Override
	public ShortImageUnsafe duplicate() {		
		NdShortArray pixels = this.pixels.duplicate();
		return new ShortImageUnsafe(pixels, getWidth(), getHeight());
	}
	
	public void destroy() {
		this.pixels.finalize();
	}	
	
	@Override
	public int getOrder() {
		return pixels.order();
	}

	@Override
	public void resizeCenter(int width, int height) {				
		/*int oldWidth = this.width;
    	int oldHeight = this.height;
    	this.width = width;
        this.height = height;
        
        int x = Math.abs(oldWidth - width)/2;
        int y = Math.abs(oldHeight - height)/2;        
       
        int i = pixels.offset(x, y);
        
		this.pixels.address(pixels.vector().getPosition(i));
		this.pixels.shape(new int[]{width, height});*/
	}	

	@Override
	// this method doesn't work yet
	public void setPixels(int width, int height, Object pixels) {}
	
	@Override
	// this method doesn't work yet
	public Object getPixels() {
		return null;
	}

}
