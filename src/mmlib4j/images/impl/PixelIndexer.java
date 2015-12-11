package mmlib4j.images.impl;

import mmlib4j.utils.Utils;

/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 */
public abstract class PixelIndexer {

	public abstract int getIndex(int x, int y);
	
	public abstract int getIndex(int p);
	
	private PixelIndexer() { }
	
	/**
	 * This indexer returns out of bounds pixel values that
	 * are taken from the closest border pixel. This is the
	 * most common method.
	 * @param width
	 * @param height
	 * @return
	 */
	public static PixelIndexer getNearestBorderIndexer(final int width, final int height){
		return new PixelIndexer(){
			public int getIndex(int x, int y) {
				if (x < 0)
					x = 0;
				else if (x >= width)
					x = width - 1;
				if (y < 0)
					y = 0;
				else if (y >= height)
					y = height - 1;
				return width * y + x;
			}

			public int getIndex(int p) {
				return getIndex(p%width, p/width);
			}
		};
	}
	
	/**
	 * This indexer returns -1 for out of bounds pixels to
	 * indicate that a (predefined) default value should be used.
	 * @param width
	 * @param height
	 * @param padding
	 * @return
	 */
	public static PixelIndexer getDefaultValueIndexer(final int width, final int height){
		if(Utils.debug) System.out.println("PixelIndexer.getDefaultValueIndexer");
		return new PixelIndexer(){
			public int getIndex(int x, int y) {
				if (x < 0 || x >= width || y < 0 || y >= height)
					return -1;
				else 
					return width * y + x;
			}
			public int getIndex(int p) {
				return getIndex(p%width, p/width);
			}
		};
	}
	
	/**
	 * This index returns out of bound pixels taken from
	 * the mirrored image. 
	 * @param width
	 * @param height
	 * @return
	 */
	public static PixelIndexer getMirrorImageIndexer(final int width, final int height){
		return new PixelIndexer(){
			public int getIndex(int x, int y) {
				// this is a fast modulo operation for positive divisors only
				x = x % width;
				if (x < 0) x = x + width; 
				y = y % height;
				if (y < 0) y = y + height; 
				return width * y + x;
			}
			public int getIndex(int p) {
				return getIndex(p%width, p/width);
			}
		};
	}
	
	/**
	 * This indexer throws an exception if out of bounds pixels
	 * are accessed.
	 * @param width
	 * @param height
	 * @return
	 */
	public static PixelIndexer getExceptionIndexer(final int width, final int height){
		return new PixelIndexer(){
			public int getIndex(int x, int y) {
				if (x < 0 || x >= width || y < 0 || y >= height) {
					throw new ArrayIndexOutOfBoundsException();
				}
				else 
					return width * y + x;
			}
			public int getIndex(int p) {
				return getIndex(p%width, p/width);
			}
		};
	}
	
}
