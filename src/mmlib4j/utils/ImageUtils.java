package mmlib4j.utils;

import java.math.BigDecimal;

import javax.swing.JOptionPane;

import mmlib4j.gui.WindowImages;
import mmlib4j.images.GrayScaleImage;
import mmlib4j.images.impl.ImageFactory;

/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 *
 * @description
 * Esta classe define alguns metodos utilitarios para operacoes com imagens
 */
public class ImageUtils {

    
    
    public static double log2(double d) {
        return Math.log(d)/Math.log(2.0);
     }

    public static double log10(double d) {
        return Math.log(d)/Math.log(10.0);
     }
    
    public static String numberToString(double x){
        String s = new BigDecimal(x).toPlainString();
        return s;
    }

    
    /**
     * Reescala a imagem de entrada para valores no intervalode 0 a 255
     * @param img - imagem de entrada
     * @return IGrayScaleImage
     */
    public static GrayScaleImage normalizedPixels(GrayScaleImage img){
        GrayScaleImage imgOut = ImageFactory.createGrayScaleImage(img);
        int tmp = 0;
        for(int x = 0 ; x < img.getWidth() ; x++){
            for(int y = 0 ; y < img.getHeight(); y++){
                tmp = (int) (( 255.0 / (img.maxValue() - img.minValue())) * (img.getPixel(x, y) - img.minValue())); 
                imgOut.setPixel(x, y, tmp);
            }
        }
        return imgOut;
    }
    
    public static int normalized255(double value, double max, double min){
    	return (int) (( 255.0 / (max - min)) * (value - min));
    }
    

    public void print(GrayScaleImage img, int dig){
    	for(int h=0; h < img.getHeight(); h++){
    		for(int w=0; w < img.getWidth(); w++){
    			System.out.printf("%" + dig + "d  ", img.getPixel(w, h));
    		}
    		System.out.println();
    	}
    }
    
    
    
    /**
     * Reduz a profundidade da imagem
     * @param img - imagem de entrada
     * @return IGrayScaleImage
     */
    public static GrayScaleImage reduceDepth(GrayScaleImage img, int depth){
        GrayScaleImage imgOut = ImageFactory.createGrayScaleImage(img);
        
        double maxIn = img.maxValue();
        double ratio = depth / 256.0;
        for(int x = 0 ; x < img.getSize() ; x++){
        	imgOut.setPixel(x, (int)(ratio * img.getPixel(x)));
        }
        
        double maxOut = imgOut.maxValue();
        for(int x = 0 ; x < img.getSize() ; x++){
        	imgOut.setPixel(x, (int) ((imgOut.getPixel(x) / maxOut) * maxIn));
        }
        
        return imgOut;
    }

    public static void main(String args[]){
    	GrayScaleImage img = ImageBuilder.openGrayImage();
    	int nGray = Integer.parseInt(JOptionPane.showInputDialog("Entre com numero de niveis de cinza"));
    	
    	WindowImages.show(img, "entrada");
    	
    	WindowImages.show(reduceDepth(img, nGray), "Reduzida: " + nGray);
    }
        
    
    /**
     * Reduz os niveis de cinza para [0,1,2,...,img.maxValue()]
     * @param img
     * @return
     */
    public static GrayScaleImage reduce(GrayScaleImage img){
        GrayScaleImage imgOut = ImageFactory.createGrayScaleImage(img);;
        int hist[] = img.getHistogram();
        int lut[] = new int[hist.length];
        int cont = 0;
        for(int i=0; i < hist.length; i++){
        	if(hist[i] > 0)
        		lut[i] = cont++;
        }
        for (int p: imgOut.scanForward())
        	imgOut.setPixel(p, lut[img.getPixel(p)]);
        
        return imgOut;
    }
    

    
    
    /**
     * Determina a distancia entre dois pixels
     * @param w1 - coordenada da largura do primeiro pixel
     * @param h1 - coordenada da altura do primeiro pixel
     * @param w2 - coordenada da largura do segundo pixel
     * @param h2 - coordenada da altura do segundo pixel
     * @param type - tipo da distancia. 
     *               Sendo que type = 1 para euclidiana; 
     *                         type = 2 para city block; 
     *                         type = 3 para clessbord
     * @return double
     */
    public static double distance(double w1, double h1, double w2, double h2, int type){
        if(type == 1){
            return Math.sqrt(Math.pow(w1 - w2, 2) + Math.pow(h1 - h2, 2));
        }else if(type == 2){
            return Math.abs(w1 - w2) + Math.abs(h1 - h2);
        }else{
            return Math.max(Math.abs(w1 - w2), Math.abs(h1 - h2));
        }
        
    }
      
    
    public static int randomInteger (int low, int high){
    	int k;
    	k = (int) (Math.random() * (high - low + 1));
    	return low + k;
    }
    
    
    
  
    
}
