package mmlib4j.segmentation;

import mmlib4j.images.BinaryImage;
import mmlib4j.images.GrayScaleImage;
import mmlib4j.images.impl.BitImage;


/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 */
public class ThresholdGlobal {
    
    public static BinaryImage otsu(GrayScaleImage img){        
        return lowerSet(img, getIndexOtsu(img));
    }
    
    /**
     * pixel do objeto é <= limiar
     * @param img
     * @param limiar
     * @return
     */
    public static BinaryImage lowerSet(GrayScaleImage img, int limiar){
        BinaryImage imgOut = new BitImage(img.getWidth(), img.getHeight());        
        for(int i = 0; i < imgOut.getWidth(); i++){
            for(int j = 0; j < imgOut.getHeight(); j++){
                if(img.getPixel(i, j) <= limiar){
                    imgOut.setPixel(i, j, true); //0bj
                }
            }
        }

        return imgOut;
    }
    
    /**
     * pixel do objeto é >= limiar
     * @param img
     * @param limiar
     * @return
     */
    public static BinaryImage upperSet(GrayScaleImage img, int limiar){
        BinaryImage imgOut = new BitImage(img.getWidth(), img.getHeight());        
        for(int i = 0; i < imgOut.getWidth(); i++){
            for(int j = 0; j < imgOut.getHeight(); j++){
                if(img.getPixel(i, j) >= limiar){
                    imgOut.setPixel(i, j, true); //0bj
                }
            }
        }

        return imgOut;
    }

    public static int getIndexOtsu(GrayScaleImage img){
    	int argmax = 0;
        double valueMax = 0; 
        double value = 0;
        double omega1 = 0;
        double omega2 = 0;
        double m1 = 0, m2 = 0, ms = 0, miT = 0;
        double sigmaB = 0, sigmaT = 0;

        int histogram[] = img.getHistogram();
        double relativeHistogram[] = new double[histogram.length];
        for(int i = 0; i < histogram.length; i++){
            relativeHistogram[i] = (double) histogram[i] / (img.getWidth() * img.getHeight());
        }        
        
        for(int i = 0; i < relativeHistogram.length; i++){
            miT += i * relativeHistogram[i];
        }

        for(int i = 0; i < relativeHistogram.length; i++){
            sigmaT += Math.pow(i - miT, 2) * relativeHistogram[i];
        }       

        for(int t = 0; t < relativeHistogram.length; t++){

        	omega1 += relativeHistogram[t];
        	ms += t * relativeHistogram[t];
        	
            omega2 = 1 - omega1;
            m1 = ms / omega1;
            m2 = (miT - ms) / omega2;

            sigmaB = omega1 * omega2 * Math.pow(m1 - m2, 2);            

            value = Math.pow(sigmaB, 2)/Math.pow(sigmaT, 2);

            if(value > valueMax){
                valueMax = value;
                argmax = t;
            }
        }               

        return argmax;
    }


   
}

