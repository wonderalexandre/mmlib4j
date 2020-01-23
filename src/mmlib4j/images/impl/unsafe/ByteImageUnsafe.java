package mmlib4j.images.impl.unsafe;


import mmlib4j.images.impl.AbstractGrayScale;
import mmlib4j.images.impl.ByteImage;
import mmlib4j.images.unsafe.GrayScaleImageUnsafe;
import ndarrays4j.arrays.integer.NdByteArray;

public class ByteImageUnsafe extends AbstractGrayScale implements GrayScaleImageUnsafe{
	
	private NdByteArray pixels;
	
	public ByteImageUnsafe(NdByteArray pixels, int width, int height) {
		this.pixels = pixels;
		this.width = width;
		this.height = height;
	}
		
	public ByteImageUnsafe(int width, int height) {
		this.pixels = new NdByteArray(new int[] {width, height});
		this.width = width;
		this.height = height;
	}
	
	public ByteImageUnsafe(long address, int width, int height) {
		this.pixels = new NdByteArray(address, new int[] {width, height});
		this.width = width;
		this.height = height;
	}
	
	public ByteImageUnsafe(long address, int width, int height, int order) {
		this.pixels = new NdByteArray(address, new int[] {width, height}, order);
		this.width = width;
		this.height = height;
	}
	
	@Override
	public String getType() {	
		return "ByteImage";
	}	
	
	@Override
	public int [] getShape() {
		return new int[] {width, height};
	}
	
	@Override
	public long getAddress() {
		return pixels.address();
	}
	
	@Override
	public int getPixel(final int i) {		
		return ByteImage.toInt(pixels.get(i));		
	}

	@Override
	public int getDepth() {
		return 9;
	}

	@Override
	public int getPixel(int x, int y) {
		return ByteImage.toInt(pixels.get(x, y));
	}

	@Override
	public void setPixel(int x, int y, int value) {
		pixels.set(ByteImage.toByte(value), x, y);
	}

	@Override
	public void setPixel(int i, int value) {
		pixels.set(ByteImage.toByte(value), i);
	}

	@Override
	public ByteImageUnsafe duplicate() {
		NdByteArray pixels = this.pixels.duplicate();
		return new ByteImageUnsafe(pixels, getWidth(), getHeight());
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
        
        this.pixels.setAddress(pixels.getPosition(y * oldWidth + x));   
        this.pixels.setSize(width * height);*/
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
