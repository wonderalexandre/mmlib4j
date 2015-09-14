package mmlib4j.utils;

import mmlib4j.images.BinaryImage;
import mmlib4j.images.ColorImage;
import mmlib4j.images.GrayScaleImage;
import mmlib4j.images.impl.BitImage;
import mmlib4j.images.impl.ImageFactory;
 

/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 *
 *  
 * @description
 * Classe que implementa metodos de reamostragem de imagem
 * - ampliacao e reducao de imagem pelo vizinho mais proximo
 * - ampliacao e reducao de imagem pelo metodo bilinear
 * - rotacao de imagem
 */
public class Resampling {
    
    /**
     * Ampliacao e reducao de imagem pelo vizinho mais proximo
     * @param img - imagem de entrada
     * @param fator - fator de ampliacao - (1 = 100%)
     * @return imgem de saida
     */
    public static GrayScaleImage nearestNeighbor(GrayScaleImage img, float fator){
        GrayScaleImage imgOut = ImageFactory.createGrayScaleImage(img.getDepth(),  (int)(img.getWidth() * fator), (int) (img.getHeight() * fator));
        int value;
        if(fator <= 0) throw new RuntimeException("fator precisa ser maior que 0!");
        for(int w=0; w < imgOut.getWidth(); w++){
            for(int h=0; h < imgOut.getHeight(); h++){
                if(img.isPixelValid(Math.round(w / fator), Math.round(h / fator))){
                    value = img.getPixel(Math.round(w / fator), Math.round(h / fator));
                    imgOut.setPixel(w,h, value);
                }
            }
        }
        return imgOut;
    }
    

    /**
     * Ampliacao e reducao de imagem pelo metodo bilinear
     * @param img - imagem de entrada
     * @param width - largura
     * @param heigth - altura
     * @return imgem de saida
     */
    public static BinaryImage bilinear(BinaryImage img, int width, int heigth){
    	
    	
    	BinaryImage imgOut = new BitImage(width, heigth);
    	
        int value;
        if(width <= 0 || heigth <= 0) throw new RuntimeException("largura ou altura da imagem precisa ser maior que 0!");
        
        double p1,p2,p3,p4;
        
        double fatorW = (double) width / (double) img.getWidth();
        double fatorH = (double) heigth / (double) img.getHeight();
        
        for(int w=0; w < imgOut.getWidth(); w++){
            for(int h=0; h < imgOut.getHeight(); h++){
                p1=p2=p3=p4=0;
                int ww = (int) Math.floor(w / fatorW);
                int hh = (int) Math.floor(h / fatorH);
                
                double dw = (w / fatorW) - ww;
                double dh = (h / fatorH) - hh;
                
                if(img.isPixelValid(ww, hh))
                    p1 = (1.0 - dw) * (1.0 - dh) * (img.getPixel(ww, hh)?1:0);
                if(img.isPixelValid(ww, hh+1))
                    p2 = dh * (1.0 - dw) * (img.getPixel(ww, hh + 1)?1:0);
                if(img.isPixelValid(ww+1, hh))
                    p3 = dw * (1.0 - dh) * (img.getPixel(ww+1, hh)?1:0);
                if(img.isPixelValid(ww+1, hh+1))
                    p4 = dh * dw * (img.getPixel(ww+1, hh + 1)?1:0);
                
                
                
                value = (int) Math.round(p1 + p2 + p3 + p4);
                imgOut.setPixel(w,h, value == 1);
            }
        }
        return imgOut;
    }
    
    /**
     * Ampliacao e reducao de imagem pelo metodo bilinear
     * @param img - imagem de entrada
     * @param width - largura
     * @param heigth - altura
     * @return imgem de saida
     */
    public static GrayScaleImage bilinear(GrayScaleImage img, int width, int heigth){
    	
    	
        GrayScaleImage imgOut = ImageFactory.createGrayScaleImage(img.getDepth(), width, heigth);
        int value;
        if(width <= 0 || heigth <= 0) throw new RuntimeException("largura ou altura da imagem precisa ser maior que 0!");
        
        double p1,p2,p3,p4;
        
        double fatorW = (double) width / (double) img.getWidth();
        double fatorH = (double) heigth / (double) img.getHeight();
        
        for(int w=0; w < imgOut.getWidth(); w++){
            for(int h=0; h < imgOut.getHeight(); h++){
                p1=p2=p3=p4=0;
                int ww = (int) Math.floor(w / fatorW);
                int hh = (int) Math.floor(h / fatorH);
                
                double dw = (w / fatorW) - ww;
                double dh = (h / fatorH) - hh;
                
                if(img.isPixelValid(ww, hh))
                    p1 = (1.0 - dw) * (1.0 - dh) * img.getPixel(ww, hh);
                if(img.isPixelValid(ww, hh+1))
                    p2 = dh * (1.0 - dw) * img.getPixel(ww, hh + 1);
                if(img.isPixelValid(ww+1, hh))
                    p3 = dw * (1.0 - dh) * img.getPixel(ww+1, hh);
                if(img.isPixelValid(ww+1, hh+1))
                    p4 = dh * dw * img.getPixel(ww+1, hh + 1);
                
                
                
                value = (int) Math.round(p1 + p2 + p3 + p4);
                imgOut.setPixel(w,h, value);
            }
        }
        return imgOut;
    }

    /**
     * Ampliacao e reducao de imagem pelo metodo bilinear
     * @param img - imagem de entrada
     * @param width - largura
     * @param heigth - altura
     * @return imgem de saida
     */
    public static ColorImage bilinear(ColorImage img, int width, int heigth){
    	
    	
    	ColorImage imgOut = ImageFactory.createColorImage(width, heigth);
        int value;
        if(width <= 0 || heigth <= 0) throw new RuntimeException("largura ou altura da imagem precisa ser maior que 0!");
        
        double p1R,p2R,p3R,p4R;
        double p1G,p2G,p3G,p4G;
        double p1B,p2B,p3B,p4B;
        
        double fatorW = (double) width / (double) img.getWidth();
        double fatorH = (double) heigth / (double) img.getHeight();
        

        
        for(int w=0; w < imgOut.getWidth(); w++){
            for(int h=0; h < imgOut.getHeight(); h++){
              	p1R=p2R=p3R=p4R=0;
            	p1G=p2G=p3G=p4G=0;
            	p1B=p2B=p3B=p4B=0;
            	int ww = (int) Math.floor(w / fatorW);
            	int hh = (int) Math.floor(h / fatorH);
                    
            	double dw = (w / fatorW) - ww;
            	double dh = (h / fatorH) - hh;
                    
            	
            	if(img.isPixelValid(ww, hh)){
                	p1R = (1.0 - dw) * (1.0 - dh) * img.getRed(ww, hh);
                	p1G = (1.0 - dw) * (1.0 - dh) * img.getGreen(ww, hh);
                	p1B = (1.0 - dw) * (1.0 - dh) * img.getBlue(ww, hh);
                }
                if(img.isPixelValid(ww, hh+1)){
                	p2R = dh * (1.0 - dw) * img.getRed(ww, hh + 1);
                	p2G = dh * (1.0 - dw) * img.getGreen(ww, hh + 1);
                	p2B = dh * (1.0 - dw) * img.getBlue(ww, hh + 1);
                }
                if(img.isPixelValid(ww+1, hh)){
                	p3R = dw * (1.0 - dh) * img.getRed(ww+1, hh);
                	p3G = dw * (1.0 - dh) * img.getGreen(ww+1, hh);
                	p3B = dw * (1.0 - dh) * img.getBlue(ww+1, hh);
                }
                if(img.isPixelValid(ww+1, hh+1)){
                	p4R = dh * dw * img.getRed(ww+1, hh + 1);
                	p4G = dh * dw * img.getGreen(ww+1, hh + 1);
                	p4B = dh * dw * img.getBlue(ww+1, hh + 1);
                }
                    
                int r = (int) Math.round(p1R + p2R + p3R + p4R);
                int g = (int) Math.round(p1G + p2G + p3G + p4G);
                int b = (int) Math.round(p1B + p2B + p3B + p4B);
               
            	imgOut.setPixel(w, h, new int[]{r,g,b});
            	
            }
        }
        return imgOut;
    }


    /**
     * Ampliacao e reducao de imagem pelo metodo bilinear
     * @param img - imagem de entrada
     * @param fator - fator de ampliacao - (1 = 100%)
     * @return imgem de saida
     */
    public static BinaryImage bilinear(BinaryImage img, float fator){
    	BinaryImage imgOut = new BitImage( (int)(img.getWidth() * fator), (int) (img.getHeight() * fator));
    	
    	int value;
        if(fator <= 0) throw new RuntimeException("fator precisa ser maior que 0!");
        
        double p1,p2,p3,p4;
        
        
        for(int w=0; w < imgOut.getWidth(); w++){
            for(int h=0; h < imgOut.getHeight(); h++){
                p1=p2=p3=p4=0;
                int ww = (int) Math.floor(w / fator);
                int hh = (int) Math.floor(h / fator);
                
                double dw = (w / fator) - ww;
                double dh = (h / fator) - hh;
                
                if(img.isPixelValid(ww, hh))
                    p1 = (1.0 - dw) * (1.0 - dh) * (img.getPixel(ww, hh)?1:0);
                if(img.isPixelValid(ww, hh+1))
                    p2 = dh * (1.0 - dw) * (img.getPixel(ww, hh + 1)?1:0);
                if(img.isPixelValid(ww+1, hh))
                    p3 = dw * (1.0 - dh) * (img.getPixel(ww+1, hh)?1:0);
                if(img.isPixelValid(ww+1, hh+1))
                    p4 = dh * dw * (img.getPixel(ww+1, hh + 1)?1:0);
                
                
                
                value = (int) Math.round(p1 + p2 + p3 + p4);
                imgOut.setPixel(w,h, value==1);
            }
        }
        return imgOut;
    }
    
    /**
     * Ampliacao e reducao de imagem pelo metodo bilinear
     * @param img - imagem de entrada
     * @param fator - fator de ampliacao - (1 = 100%)
     * @return imgem de saida
     */
    public static GrayScaleImage bilinear(GrayScaleImage img, float fator){
        GrayScaleImage imgOut = ImageFactory.createGrayScaleImage(img.getDepth(), (int)(img.getWidth() * fator), (int) (img.getHeight() * fator));
        int value;
        if(fator <= 0) throw new RuntimeException("fator precisa ser maior que 0!");
        
        double p1,p2,p3,p4;
        
        
        for(int w=0; w < imgOut.getWidth(); w++){
            for(int h=0; h < imgOut.getHeight(); h++){
                p1=p2=p3=p4=0;
                int ww = (int) Math.floor(w / fator);
                int hh = (int) Math.floor(h / fator);
                
                double dw = (w / fator) - ww;
                double dh = (h / fator) - hh;
                
                if(img.isPixelValid(ww, hh))
                    p1 = (1.0 - dw) * (1.0 - dh) * img.getPixel(ww, hh);
                if(img.isPixelValid(ww, hh+1))
                    p2 = dh * (1.0 - dw) * img.getPixel(ww, hh + 1);
                if(img.isPixelValid(ww+1, hh))
                    p3 = dw * (1.0 - dh) * img.getPixel(ww+1, hh);
                if(img.isPixelValid(ww+1, hh+1))
                    p4 = dh * dw * img.getPixel(ww+1, hh + 1);
                
                
                
                value = (int) Math.round(p1 + p2 + p3 + p4);
                imgOut.setPixel(w,h, value);
            }
        }
        return imgOut;
    }
    
    /**
     * Ampliacao e reducao de imagem pelo metodo bilinear
     * @param img - imagem de entrada
     * @param fator - fator de ampliacao - (1 = 100%)
     * @return imgem de saida
     */ 
    public static ColorImage bilinear(ColorImage img, float fator){
        ColorImage imgOut = ImageFactory.createColorImage( (int)(img.getWidth() * fator), (int) (img.getHeight() * fator));

        if(fator <= 0) throw new RuntimeException("fator precisa ser maior que 0!");
        
        double p1R,p2R,p3R,p4R;
        double p1G,p2G,p3G,p4G;
        double p1B,p2B,p3B,p4B;
        
        
        for(int w=0; w < imgOut.getWidth(); w++){
            for(int h=0; h < imgOut.getHeight(); h++){
            	p1R=p2R=p3R=p4R=0;
            	p1G=p2G=p3G=p4G=0;
            	p1B=p2B=p3B=p4B=0;
                int ww = (int) Math.floor(w / fator);
                int hh = (int) Math.floor(h / fator);
                double dw = (w / fator) - ww;
                double dh = (h / fator) - hh;
                
                if(img.isPixelValid(ww, hh)){
                	p1R = (1.0 - dw) * (1.0 - dh) * img.getRed(ww, hh);
                	p1G = (1.0 - dw) * (1.0 - dh) * img.getGreen(ww, hh);
                	p1B = (1.0 - dw) * (1.0 - dh) * img.getBlue(ww, hh);
                }
                if(img.isPixelValid(ww, hh+1)){
                	p2R = dh * (1.0 - dw) * img.getRed(ww, hh + 1);
                	p2G = dh * (1.0 - dw) * img.getGreen(ww, hh + 1);
                	p2B = dh * (1.0 - dw) * img.getBlue(ww, hh + 1);
                }
                if(img.isPixelValid(ww+1, hh)){
                	p3R = dw * (1.0 - dh) * img.getRed(ww+1, hh);
                	p3G = dw * (1.0 - dh) * img.getGreen(ww+1, hh);
                	p3B = dw * (1.0 - dh) * img.getBlue(ww+1, hh);
                }
                if(img.isPixelValid(ww+1, hh+1)){
                	p4R = dh * dw * img.getRed(ww+1, hh + 1);
                	p4G = dh * dw * img.getGreen(ww+1, hh + 1);
                	p4B = dh * dw * img.getBlue(ww+1, hh + 1);
                }
                    
                int r = (int) Math.round(p1R + p2R + p3R + p4R);
                int g = (int) Math.round(p1G + p2G + p3G + p4G);
                int b = (int) Math.round(p1B + p2B + p3B + p4B);
               
            	imgOut.setPixel(w, h, new int[]{r,g,b});
            }
        }
        return imgOut;
    }
    
    
    /**
     * Rotacao em sentido anti-horario
     * @param img - imagem de entrada
     * @param graus - graus
     * @return imgem de saida
     */
    public static GrayScaleImage rotation(GrayScaleImage img, float graus){
        GrayScaleImage imgOut = ImageFactory.createGrayScaleImage(img);;
        //imgOut.initImage(255);
        
        double radian = 2.0 * Math.PI * graus / 360.0;
        double ct = Math.cos(radian);
        double st = Math.sin(radian);
        
        int hc = img.getHeight()/2;
        int wc = img.getWidth()/2;
        for(int w=0; w < img.getWidth(); w++){
            for(int h=0; h < img.getHeight(); h++){
                int nh = (int) ((h - hc) * ct - (w - wc) * st + hc);
                int nw = (int) ((h - hc) * st + (w - wc) * ct + wc);
                if(img.isPixelValid(nw, nh))
                    imgOut.setPixel(w, h, img.getPixel(nw, nh));
                else
                    imgOut.setPixel(w, h, 255);
               
               
            }
        }
        return imgOut;//adjustImage(imgOut);
        
    }
    

    
    /**
     * Rotacao em sentido anti-horario
     * @param img - imagem de entrada
     * @param graus - graus
     * @return imgem de saida
     */
    public static BinaryImage rotation(BinaryImage img, double graus){
        BinaryImage imgOut = new BitImage(img.getWidth(), img.getHeight());
        
        double radian = 2.0 * Math.PI * graus / 360.0;
        double ct = Math.cos(radian);
        double st = Math.sin(radian);
        
        int hc = img.getHeight()/2;
        int wc = img.getWidth()/2; 
        for(int w=0; w < img.getWidth(); w++){
            for(int h=0; h < img.getHeight(); h++){
                int nh = (int) ((h - hc) * ct - (w - wc) * st + hc);
                int nw = (int) ((h - hc) * st + (w - wc) * ct + wc);
                if(img.isPixelValid(nw, nh))
                    imgOut.setPixel(w, h, img.getPixel(nw, nh));
            }
        }
        return imgOut;
    }
    
    
    
    /**
     * Rotacao em sentido anti-horario
     * @param img - imagem de entrada
     * @param graus - graus
     * @return imgem de saida
     */
    public static ColorImage rotation(ColorImage img, double graus){
        ColorImage imgOut = ImageFactory.createColorImage(img.getWidth(), img.getHeight());
        
        double radian = 2.0 * Math.PI * graus / 360.0;
        double ct = Math.cos(radian);
        double st = Math.sin(radian);
        
        int hc = img.getHeight()/2;
        int wc = img.getWidth()/2;
        for(int w=0; w < img.getWidth(); w++){
            for(int h=0; h < img.getHeight(); h++){
                int nh = (int) ((h - hc) * ct - (w - wc) * st + hc);
                int nw = (int) ((h - hc) * st + (w - wc) * ct + wc);
                if(img.isPixelValid(nw, nh))
                    imgOut.setPixel(w, h, img.getPixel(nw, nh));
            }
        }
        return imgOut;
    }
    
  
    
    
}
