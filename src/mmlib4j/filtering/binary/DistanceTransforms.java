package mmlib4j.filtering.binary;
import mmlib4j.images.BinaryImage;
import mmlib4j.images.GrayScaleImage;
import mmlib4j.images.RealImage;
import mmlib4j.images.impl.ImageFactory;

/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 */
public class DistanceTransforms {
  
	public RealImage euclideanDistance(BinaryImage img){
		return ImageFactory.createReferenceRealImage(euclideanDistanceMap(img), img.getWidth(), img.getHeight());
	}
	
	public float[] euclideanDistanceMap(BinaryImage img){
		float K1 = 1;
		float K2 = (float)Math.sqrt(2); 
		return distanceTransformFloat(img, K1, K2);
	}
	
	public int[] chessbordDistanceMap(BinaryImage img){
		int K1 = 1;
		int K2 = 1; 
		return distanceTransform(img, K1, K2);
	}

	public int[] cityBlockDistanceMap(BinaryImage img){
		int K1 = 1;
		int K2 = 2; 
		return distanceTransform(img, K1, K2);
	}

	public int[] euclideanDistanceMapDiscrete(BinaryImage img){
		int K1 = 3;
		int K2 = 4; 
		return distanceTransform(img, K1, K2);
	}
	
	public GrayScaleImage chessbordDistance(BinaryImage img){
		return ImageFactory.createReferenceGrayScaleImage(ImageFactory.DEPTH_32BITS, chessbordDistanceMap(img), img.getWidth(), img.getHeight());
	}
	
	
	public GrayScaleImage cityBlockDistance(BinaryImage img){
		return ImageFactory.createReferenceGrayScaleImage(ImageFactory.DEPTH_32BITS, cityBlockDistanceMap(img), img.getWidth(), img.getHeight());
	}
	
	public GrayScaleImage euclideanDistanceDiscrete(BinaryImage img){
		return ImageFactory.createReferenceGrayScaleImage(ImageFactory.DEPTH_32BITS, euclideanDistanceMapDiscrete(img), img.getWidth(), img.getHeight());
	}
	
	public GrayScaleImage dtChamfer(BinaryImage img, int k1, int k2){
		return ImageFactory.createReferenceGrayScaleImage(ImageFactory.DEPTH_32BITS, distanceTransform(img, k1, k2), img.getWidth(), img.getHeight());
	}
	
	public int[] distanceTransform(BinaryImage img, int k1, int k2){
		int[] dpix = new int[img.getSize()];
		
		//Initialization: 
		//foreground pixels (>0) -> 0, background -> infinity
		for(int p: img.scanForward()){
			if (img.isPixelForeground(p)) // this is a foreground pixel
				dpix[p] = Integer.MAX_VALUE; // zero distance to foregorund
			else
				dpix[p] = 0;
		}
		distanceTransform(img.getWidth(), img.getHeight(), dpix, k1, k2);
		return dpix;
	}
	
	public void distanceTransform(int w, int h, int dpix[], int k1, int k2){
		int d1, d2, d3, d4, dmin;
		//L->R pass:
		for (int v = 0; v < h; v++) {
			for (int u = 0; u < w; u++) {
				int i = v * w + u;
				if (dpix[i]>0) { //not a foreground pixel
					//compute distances via neighboring pixels
					d1 = Integer.MAX_VALUE;
					d2 = Integer.MAX_VALUE;
					d3 = Integer.MAX_VALUE;
					d4 = Integer.MAX_VALUE;
					
					if (u>0) 			d1 = k1 + dpix[v*w+u-1];
					if (u>0 && v>0) 	d2 = k2 + dpix[(v-1)*w+u-1];
					if (v>0)			d3 = k1 + dpix[(v-1)*w+u];
					if (v>0 && u<w-1)	d4 = k2 + dpix[(v-1)*w+u+1];
					
					dmin = dpix[i];
					if (d1<dmin) dmin = d1;
					if (d2<dmin) dmin = d2;
					if (d3<dmin) dmin = d3;
					if (d4<dmin) dmin = d4;
					dpix[i] = dmin;
				}
			}
		}
		
		//R->L pass:
		for (int v = h - 1; v >= 0; v--) {
			for (int u = w - 1; u >= 0; u--) {
				int i = v * w + u;
				if (dpix[i] > 0) { //not a foreground pixel
					
					//compute distances via neighboring pixels
					d1 = Integer.MAX_VALUE;
					d2 = Integer.MAX_VALUE;
					d3 = Integer.MAX_VALUE;
					d4 = Integer.MAX_VALUE;
					
					if (u<w-1) 			d1 = k1 + dpix[v*w+u+1];
					if (u<w-1 && v<h-1)	d2 = k2 + dpix[(v+1)*w+u+1];
					if (v<h-1)			d3 = k1 + dpix[(v+1)*w+u];
					if (v<h-1 && u>0)	d4 = k2 + dpix[(v+1)*w+u-1];
					
					dmin = dpix[i];
					if (d1<dmin) dmin = d1;
					if (d2<dmin) dmin = d2;
					if (d3<dmin) dmin = d3;
					if (d4<dmin) dmin = d4;
					dpix[i] = dmin;
				}
			}
		}
		
	}
    
	public float[] distanceTransformFloat(BinaryImage ip, float k1, float k2){
		float[] dpix = new float[ip.getSize()];
		
		//Initialization: 
		//foreground pixels (>0) -> 0, background -> infinity
		for(int p: ip.scanForward()){
			if (ip.isPixelForeground(p)) // this is a foreground pixel
				dpix[p] = Float.POSITIVE_INFINITY; // zero distance to foregorund
			else
				dpix[p] = 0;
		}
		distanceTransformFloat(ip.getWidth(), ip.getHeight(), dpix, k1, k2);
		return dpix;
	}
	
	
	public void distanceTransformFloat(int w, int h, float dpix[], float k1, float k2){
		
		float d1, d2, d3, d4, dmin;
		//L->R pass:
		for (int v = 0; v < h; v++) {
			for (int u = 0; u < w; u++) {
				int i = v * w + u;
				if (dpix[i]>0) { //not a foreground pixel
					//compute distances via neighboring pixels
					d1 = Float.POSITIVE_INFINITY;
					d2 = Float.POSITIVE_INFINITY;
					d3 = Float.POSITIVE_INFINITY;
					d4 = Float.POSITIVE_INFINITY;
					
					if (u>0) 			d1 = k1 + dpix[v*w+u-1];
					if (u>0 && v>0) 	d2 = k2 + dpix[(v-1)*w+u-1];
					if (v>0)			d3 = k1 + dpix[(v-1)*w+u];
					if (v>0 && u<w-1)	d4 = k2 + dpix[(v-1)*w+u+1];
					
					dmin = dpix[i];
					if (d1<dmin) dmin = d1;
					if (d2<dmin) dmin = d2;
					if (d3<dmin) dmin = d3;
					if (d4<dmin) dmin = d4;
					dpix[i] = dmin;
				}
			}
		}
		
		//R->L pass:
		for (int v = h - 1; v >= 0; v--) {
			for (int u = w - 1; u >= 0; u--) {
				int i = v * w + u;
				if (dpix[i] > 0) { //not a foreground pixel
					
					//compute distances via neighboring pixels
					d1 = Float.POSITIVE_INFINITY;
					d2 = Float.POSITIVE_INFINITY;
					d3 = Float.POSITIVE_INFINITY;
					d4 = Float.POSITIVE_INFINITY;
					
					if (u<w-1) 			d1 = k1 + dpix[v*w+u+1];
					if (u<w-1 && v<h-1)	d2 = k2 + dpix[(v+1)*w+u+1];
					if (v<h-1)			d3 = k1 + dpix[(v+1)*w+u];
					if (v<h-1 && u>0)	d4 = k2 + dpix[(v+1)*w+u-1];
					
					dmin = dpix[i];
					if (d1<dmin) dmin = d1;
					if (d2<dmin) dmin = d2;
					if (d3<dmin) dmin = d3;
					if (d4<dmin) dmin = d4;
					dpix[i] = dmin;
				}
			}
		}
		
	}
    
	
    public static void main(String args[]){
    	BinaryImage img = ImageFactory.createBinaryImage(20, 20);
    	for(int h=1; h < 5; h++){
    		for(int w=1; w < 5; w++){
    			img.setPixel(w, h, true);
    		}
    	}
    	
    	for(int h=10; h < 15; h++){
    		for(int w=10; w < 15; w++){
    			img.setPixel(w, h, true);
    		}
    	}
    	
    	img.setPixel(3, 15, true);
    	img.setPixel(4, 15, true);
    	img.setPixel(3, 16, true);
    	img.setPixel(4, 16, true);
    	img.setPixel(3, 14, true);
    	img.setPixel(4, 14, true);
    	
    	img.setPixel(5, 14, true);
    	img.setPixel(5, 15, true);
    	img.setPixel(6, 15, true);
    	img.setPixel(2, 15, true);
    	img.setPixel(5, 16, true);
    	
    	img.setPixel(4, 13, true);
    	img.setPixel(4, 17, true);
    	
    	
    	System.out.println();
    	System.out.println();
    	int map[] = new DistanceTransforms().chessbordDistanceMap(img);
    	
    	for(int h=0; h < img.getHeight(); h++){
    		for(int w=0; w < img.getWidth(); w++){
    			System.out.printf("%2d  ", map[img.convertToIndex(w, h)]);
    		}
    		System.out.println();
    	}
    	
    }
    
    
   

}



