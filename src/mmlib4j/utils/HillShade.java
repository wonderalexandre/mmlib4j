package mmlib4j.utils;

import mmlib4j.gui.WindowImages;
import mmlib4j.images.GrayScaleImage;
import mmlib4j.images.impl.ImageFactory;

public class HillShade {

	public static void main(String args[]){
		GrayScaleImage img = ImageBuilder.openGrayImage();
		WindowImages.show(img, makeHillshade(img));
	}
		
	     
	    public static GrayScaleImage makeHillshade(GrayScaleImage ip){
	    	double xPixelSize = 90;
		    double yPixelSize = 90;
		    double elevation = 45;
		    double azimuth = 315;
		    double nonlinContrast = 0;
		    boolean halfBrightFlat = true;
		    return makeHillshade(ip, xPixelSize, yPixelSize, elevation, azimuth, nonlinContrast, halfBrightFlat);
	    }
	    
	    /*
	     * 
	     * @imgIn grayscale image
	     * @xPixelSize - Size of one pixel in x direction, in the same units as the height (pixel value).
	     * @yPixelSize - Size of one pixel in y direction, in the same units as the height (pixel value).
	     * @elevation - height of the light source in degrees, typically between 30 and 60
	     * @azimuth - direction to the light source in degrees, 0 is north, 90 east etc.
	     * @nonlinearContrast - when > 0, contrast for gentle slopes gets enhanced. Typical values are 1-5.
	     * @halfBrightFlat - when selected, a gamma correction is applied to the output, such that a horizontal plane will appear with half brightness (pixel value = 128), irrespective of the elevation of the sun. When not selected, the pixel value of a horizontal plane will be roughly 256*sin(Sun_Elevation). In that case, a horizontal plane gets half brighness only at sun elevation of 30°, the output gets brighter if the sun elevation is higher, and darker if it is lower. 
	     */
	    public static GrayScaleImage makeHillshade(GrayScaleImage imgIn, double xPixelSize, double yPixelSize, double elevation, double azimuth, double nonlinContrast, boolean halfBrightFlat) {
	        int width = imgIn.getWidth();
	        int height = imgIn.getHeight();
	        GrayScaleImage imgOut = ImageFactory.instance.createGrayScaleImage(8, width, height);
	        elevation *= Math.PI/180;               //to radians
	        azimuth  *= Math.PI/180;
	        double xSun = Math.sin(azimuth)*Math.cos(elevation);
	        double ySun = -Math.cos(azimuth)*Math.cos(elevation);
	        double zSun = Math.sin(elevation);      //normalized vector to the sun
	        for (int y=1; y<height-1; y++) {        //for all interior pixels
	            float a, d, g;                      //a...i will be a 9-pixel neighborhood
	            float b = imgIn.getValue(0, y-1); //preload values
	            float c = imgIn.getValue(1, y-1);
	            float e = imgIn.getValue(0, y);
	            float f = imgIn.getValue(1, y);
	            float h = imgIn.getValue(0, y+1);
	            float i = imgIn.getValue(1, y+1);
	            for (int x=1; x<width-1; x++) {
	                a = b; b = c;   //shift pixels in x by 1
	                d = e; e = f;
	                g = h; h = i;
	                c = imgIn.getValue(x+1, y-1);
	                f = imgIn.getValue(x+1, y);
	                i = imgIn.getValue(x+1, y+1);
	                double xSlope = ((a+2*d+g)-(c+2*f+i)) /(8*xPixelSize);
	                double ySlope = ((a+2*b+c)-(g+2*h+i)) /(8*yPixelSize);
	                double hillshade = getShade(xSlope, ySlope, xSun, ySun, zSun, 1./nonlinContrast, halfBrightFlat);
	                imgOut.setPixel(imgOut.getIndex(x, y), (int)hillshade);
	            }
	        }
	        double[] slopes = new double[2];        //used to return xSlope, ySlope
	        for (int y=0; y<height; y+=height-1)    //edges: for y=0 and y=height-1
	            for (int x=1; x<width-1; x++) {
	                getEdgeSlopeX(imgIn, x, y, xPixelSize, yPixelSize, slopes);
	                double hillshade = getShade(slopes[0], slopes[1], xSun, ySun, zSun, 1./nonlinContrast, halfBrightFlat);
	                imgOut.setPixel(imgOut.getIndex(x, y), (int)hillshade);
	            }
	        for (int x=0; x<width; x+=width-1)      //edges: for x=0 and x=height-1
	            for (int y=1; y<height-1; y++) {
	                getEdgeSlopeY(imgIn, x, y, xPixelSize, yPixelSize, slopes);
	                double hillshade = getShade(slopes[0], slopes[1], xSun, ySun, zSun, 1./nonlinContrast, halfBrightFlat);
	                imgOut.setPixel(imgOut.getIndex(x, y), (int)hillshade);
	            }
	        for (int y=0; y<height; y+=height-1)    //4 corners
	            for (int x=0; x<width; x+=width-1) {
	                getCornerSlope(imgIn, x, y, xPixelSize, yPixelSize, slopes);
	                double hillshade = getShade(slopes[0], slopes[1], xSun, ySun, zSun, 1./nonlinContrast, halfBrightFlat);
	                imgOut.setPixel(imgOut.getIndex(x, y), (int)hillshade);
	            }
	        return imgOut;
	    }

	    /** For the edges in x direction, write xSlope, ySlope into slopes[0], slopes[1] */
	    private static void getEdgeSlopeX(GrayScaleImage ip, int x, int y, double xPixelSize, double yPixelSize, double[] slopes) {
	        int y1 = y==0 ? 1 : y-1;
	        int y2 = y==0 ? 2 : y-2;
	        float a = ip.getValue(x-1, y);
	        float b = ip.getValue(x, y);
	        float c = ip.getValue(x+1, y);
	        float d = ip.getValue(x-1, y1);
	        float e = ip.getValue(x, y1);
	        float f = ip.getValue(x+1, y1);
	        float g = ip.getValue(x-1, y2);
	        float h = ip.getValue(x, y2);
	        float i = ip.getValue(x+1, y2);
	        slopes[0] = (a-c) / (2*xPixelSize);     //xSlope
	        slopes[1] = (4*(d+2*e+f) - 3*(a+2*b+c) - (g+2*h+i)) / (8*yPixelSize);  //ySlope: extrapolate
	        if (y==0) slopes[1] = -slopes[1];
	    }

	    /** For the edges in y direction, write edge xSlope, ySlope into slopes[0], slopes[1] */
	    private static void getEdgeSlopeY(GrayScaleImage ip, int x, int y, double xPixelSize, double yPixelSize, double[] slopes) {
	        int x1 = x==0 ? 1 : x-1;
	        int x2 = x==0 ? 2 : x-2;
	        float a = ip.getValue(x, y-1);
	        float b = ip.getValue(x, y);
	        float c = ip.getValue(x, y+1);
	        float d = ip.getValue(x1, y-1);
	        float e = ip.getValue(x1, y);
	        float f = ip.getValue(x1, y+1);
	        float g = ip.getValue(x2, y-1);
	        float h = ip.getValue(x2, y);
	        float i = ip.getValue(x2, y+1);
	        slopes[1] = (a-c) / (2*yPixelSize);     //ySlope
	        slopes[0] = (4*(d+2*e+f) - 3*(a+2*b+c) - (g+2*h+i)) / (8*xPixelSize);  //xSlope: extrapolate
	        if (x==0) slopes[0] = -slopes[0];
	    }

	    /** For the corners, write xSlope, ySlope into slopes[0], slopes[1] */
	    private static void getCornerSlope(GrayScaleImage ip, int x, int y, double xPixelSize, double yPixelSize, double[] slopes) {
	        int x1 = x==0 ? 1 : x-1;
	        int x2 = x==0 ? 2 : x-2;
	        int y1 = y==0 ? 1 : y-1;
	        int y2 = y==0 ? 2 : y-2;
	        float a = ip.getValue(x, y);
	        float bx = ip.getValue(x1, y);
	        float cx = ip.getValue(x2, y);
	        float by = ip.getValue(x, y1);
	        float cy = ip.getValue(x, y2);
	        slopes[0] = (4*bx - 3*a - cx) / (2*xPixelSize);  //xSlope: extrapolate
	        if (x==0) slopes[0] = -slopes[0];
	        slopes[1] = (4*by - 3*a - cy) / (2*yPixelSize);  //ySlope: extrapolate
	        if (y==0) slopes[1] = -slopes[1];
	    }

	    /** calculates the shade value between 0 and 256 */
	    private static double getShade(double xSlope, double ySlope, double xSun, double ySun, double zSun, double invNonlinContrast, boolean halfBrightFlat) {
	        double slopeSqr = xSlope*xSlope + ySlope*ySlope;
	        double slopeFact = 1./Math.sqrt(1+slopeSqr);
	        double xNorm = xSlope*slopeFact;
	        double yNorm = ySlope*slopeFact;
	        double zNorm = slopeFact;
	        double hillshade = xNorm*xSun + yNorm*ySun + zNorm*zSun;
	        boolean doNonlinContrast = invNonlinContrast < 1e5;
	        if ((halfBrightFlat || doNonlinContrast) && hillshade > 0 && zSun > 0.0001 && zSun < 0.99999)
	            hillshade = Math.pow(hillshade, Math.log(0.5)/Math.log(zSun));  //gamma, puts the 'neutral point' to 0.5
	        if (doNonlinContrast) {
	            double sign = hillshade > 0.5 ? +2.0 : -2.0;
	            double tmp = sign*(hillshade - 0.5); //0...1 range
	            tmp = Math.log(invNonlinContrast + tmp);
	            double min = Math.log(invNonlinContrast);
	            double max = Math.log(invNonlinContrast + 1);
	            tmp = (tmp - min)/(max - min);
	            hillshade = 0.5 + 0.25*tmp*sign;
	            if (doNonlinContrast && !halfBrightFlat) //revert gamma if undesired
	                hillshade = Math.pow(hillshade, Math.log(zSun)/Math.log(0.5));
	        }
	        return hillshade * 256.0;
	    }

	
}
