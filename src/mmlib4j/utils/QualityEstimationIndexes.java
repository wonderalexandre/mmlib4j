package mmlib4j.utils;


import java.text.DecimalFormat;

import mmlib4j.images.GrayScaleImage;

/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Flávio Kubo
 */
public class QualityEstimationIndexes {

	public static double mse(GrayScaleImage imgIn, GrayScaleImage imgOut){
        int sum=0;
        for(int p=0; p < imgIn.getSize(); p++){
        	sum += Math.pow((imgOut.getPixel(p) -imgIn.getPixel(p)), 2);
        }
        return sum/((double) imgIn.getSize());
    }
	
    public static double psnr(GrayScaleImage imgIn,GrayScaleImage imgOut){
        double mse=mse(imgIn,imgOut);
        double a2max = Math.pow(2,imgIn.getDepth())-1;
        double psnr = 10 * Math.log10(Math.pow(a2max,2) / mse);
        return psnr;
    }
    
    public static double snr(GrayScaleImage imgIn,GrayScaleImage imgOut){
        int sum=0;
        int sum2=0;
        for(int p=0; p < imgIn.getSize(); p++){
        	sum += Math.pow((imgOut.getPixel(p) - imgIn.getPixel(p)), 2);
            sum2+=Math.pow(imgIn.getPixel(p), 2);
        }
        return 10 * Math.log10(((double)sum2)/((double)sum));
    }
    
    public static String getQualityIndexes(GrayScaleImage imgIn,GrayScaleImage imgOut){
    	DecimalFormat df = new DecimalFormat("#####.####");
    	String psnr=df.format(psnr(imgIn, imgOut));
        String mse=df.format(mse(imgIn, imgOut));
        String snr=df.format(snr(imgIn, imgOut));
    	return "PSNR ="+psnr+" MSE ="+mse+" SNR ="+snr;
    }
}
