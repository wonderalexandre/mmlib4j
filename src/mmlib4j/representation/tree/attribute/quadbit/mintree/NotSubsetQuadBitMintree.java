package mmlib4j.representation.tree.attribute.quadbit.mintree;

import mmlib4j.images.GrayScaleImage;
import mmlib4j.representation.tree.attribute.quadbit.NotSubsetQuadBit;

public class NotSubsetQuadBitMintree extends NotSubsetQuadBit {
	
	private GrayScaleImage img;
	
	public NotSubsetQuadBitMintree(GrayScaleImage img, int dx, int dy) {
		super(dx, dy);
		this.img = img;
	}

	@Override
	public boolean match(int px, int py) {
		return getPixelValue(px, py) < getPixelValue(px + dx, py + dy);
	}
	
	private int getPixelValue(int px, int py) {
		int index = img.getIndex(px, py);
		if (index < 0)
			return Integer.MAX_VALUE;
		return img.getValue(index);
	}
}
