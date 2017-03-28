package mmlib4j.descriptors;

import mmlib4j.filtering.Histogram;
import mmlib4j.gui.WindowImages;
import mmlib4j.images.GrayScaleImage;
import mmlib4j.images.impl.ImageFactory;
import mmlib4j.utils.ImageBuilder;

public class LocalBinaryPatterns {
	private byte[] mapping;
	private double[] adjX;
	private double[] adjY;
	
	private double radius;
	private int numBits;

	public LocalBinaryPatterns() {
		this(8,1.5);
	}
	
	public LocalBinaryPatterns(int numBits, double radius) {
		this(numBits, radius, 1);
	}
	
	public LocalBinaryPatterns(int numBits, double radius, int type) {
		this.numBits = numBits;
		this.radius = radius;
		
		buildLookupTable(type);
		buildAdjacency();

	}
	
	private void buildAdjacency() {
		double angleIncrement = Math.PI * 2.0 / (double) numBits;
		adjX = new double[numBits];
		adjY = new double[numBits];
		for (int n = 0; n < numBits; ++n) {
			adjX[n] = (radius) * Math.cos(((double) n) * angleIncrement);
			adjY[n] = (radius) * Math.sin(((double) n) * angleIncrement);
		}
	}
	
	public GrayScaleImage computerLBP(GrayScaleImage img) {
		return computerLBP(img, 3);
	}
	
	public GrayScaleImage computerLBP(GrayScaleImage img, int threshold) {
		GrayScaleImage imgLBP = ImageFactory.createGrayScaleImage(8, img.getWidth(), img.getHeight());
		/* Calculate the lbp */
		for (int x = (int) (0 + radius); x < img.getWidth() - radius; ++x) {
			for (int y = (int)(0 + radius); y < img.getHeight() - radius; ++y) {
				imgLBP.setPixel(x, y, getLBPCode(img, x, y, threshold) );
			}
		}

		return imgLBP;
	}
	

	/**
	 * code LBP
	 * @param img
	 * @param px
	 * @param py
	 * @param threshold
	 * @return
	 */
	public int getLBPCode(GrayScaleImage img, int px, int py, int threshold){
		int code = 0;
		for(int i=0; i < adjX.length; i++){
			//eh mesmo que: code += Math.pow(2, i);
			if(img.getInterpolatedPixel(px + adjX[i], py + adjY[i]) - img.getValue(px, py) >= threshold){
				 code = code | (1 << i);
			 }else{
				 code = code & ~(1 << i);
			 }
		}
		if(mapping == null)
			return code;
		else
			return mapping[code];
	}

	
	/**
	 * 
	 * @param numBits
	 * @param type
	 *  1 => look-up table to rotation invariant uniform patterns: riu2 in getmapping.m
	 *  2 => look-up table to uniform patterns: u2 in getmapping.m 
	 * @return
	 */
	/*  */
	private void buildLookupTable(int type) {
		int bitMaskLength = (int) (Math.pow(2.0, (double) numBits));
		int j, numt;
		int sampleBitMask = 0;
		
		if(type==1){
			mapping = new byte[bitMaskLength];
			for (int i = 0; i < numBits; ++i) {
				sampleBitMask |= 1 << i;
			}		
			for (int i = 0; i < bitMaskLength; ++i) {
				
				j = ((i << 1) & sampleBitMask); // j = bitset(bitshift(i,1,samples),1,bitget(i,samples));
												// %rotate left
				j = (i >> (numBits - 1)) > 0 ? j | 1 : j & ~1; // Set first bit to one or zero
				
				numt = 0;
				for (int k = 0; k < numBits; ++k) {
					numt += (((i ^ j) >> k) & 1);
				}
	
				if (numt <= 2) {
					for (int k = 0; k < numBits; ++k)
						mapping[i] += (i >> k) & 1;
				} else {
					mapping[i] = (byte) (numBits + 1);
				}
			}
		
		}
		else if(type==2){
			mapping = new byte[bitMaskLength];
			int max = numBits*(numBits-1) + 3;
			int index = 0;
			for (int i = 0; i < numBits; ++i) {
				sampleBitMask |= 1 << i;
			}
			for (int i = 0; i < bitMaskLength; ++i) {
				// %rotate left
				j = ((i << 1) & sampleBitMask); // j = bitset(bitshift(i,1,samples),1,bitget(i,samples));
				j = (i >> (numBits - 1)) > 0 ? j | 1 : j & ~1; // Set first bit to one or zero
				numt = 0;
				
				for (int k = 0; k < numBits; ++k) {
					numt += (((i ^ j) >> k) & 1);
				}

				if (numt <= 2) {
					mapping[i] = (byte) index;
					index += 1;
				} else {
					mapping[i] = (byte) max;
				}
			}
		}
	}
	
	public static void main(String[] ar) {

		GrayScaleImage img = ImageBuilder.openGrayImage();
		LocalBinaryPatterns lbp = new LocalBinaryPatterns(8,1.5);
		
		GrayScaleImage imgLBP = lbp.computerLBP(img, 3);
		
		
		WindowImages.show(imgLBP, new Histogram(imgLBP).equalisation());
			
	}

}