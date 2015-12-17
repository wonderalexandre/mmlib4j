package mmlib4j.segmentation;

import java.util.Arrays;

import mmlib4j.filtering.MorphologicalOperatorsBasedOnSE;
import mmlib4j.images.BinaryImage;
import mmlib4j.images.GrayScaleImage;
import mmlib4j.images.impl.BitImage;
import mmlib4j.utils.AdjacencyRelation;


/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 */
public class ThresholdLocal {
    
    final GrayScaleImage img;
    
    
    public ThresholdLocal(final GrayScaleImage img){
        this.img = img;
    }
    
    /**
     * Thresholding local baseado em media de maximos e minimos
     * @param img - imagem de entrada
     * @param size - tamanho do kernel
     * @param con - correcao na media
     * @return IBinaryImage
     */
    public BinaryImage meanOfMaxMin(int size, float con){
        BinaryImage imgOut = new BitImage(img.getWidth(), img.getHeight());
        int mean = 0;
        int max, min;
        int tmp;
        int k = (int) Math.floor(size / 2);
        for(int w = 0; w < img.getWidth(); w++){
          for(int h = 0; h < img.getHeight(); h++){
            mean = 0; 
            max = img.getPixel(w, h);
            min = img.getPixel(w, h);
            for(int x=-k; x <= k; x++){ 
                for(int y=-k; y < k; y++){
                    if(img.isPixelValid(w + x, h + y)){
                        tmp = img.getPixel(w + x, h + y);
                        if(tmp > max){
                            max = tmp;
                        }
                        if(tmp < min){
                            min = tmp;
                        }
                    }
                       
                }
            }            
            tmp = max + min;
            tmp = tmp / 2;
            mean = (int) Math.floor(tmp - con);
    
            //Threshold below the mean
            if(img.getPixel(w, h) >= mean){
                imgOut.setPixel(w, h, true);
            }else {
                imgOut.setPixel(w, h, false);
            }
          } 
        }
     return imgOut;   
    }
    
    public BinaryImage otsuLocal(int limiar){
        BinaryImage imgOut = new BitImage(img.getWidth(), img.getHeight());
        AdjacencyRelation adj = AdjacencyRelation.getCircular(2);
        GrayScaleImage imgDilate = MorphologicalOperatorsBasedOnSE.dilation(img, adj);
        GrayScaleImage imgErode = MorphologicalOperatorsBasedOnSE.erosion(img, adj);
        BinaryImage imgOtsu = ThresholdGlobal.otsu(img);
        
        boolean level; 
        int diff, diffErode, diffDilate;
        for(int i=0; i < imgOut.getSize(); i++){
            diff = Math.abs(imgDilate.getPixel(i) - imgErode.getPixel(i));
            diffErode = Math.abs(img.getPixel(i) - imgErode.getPixel(i));
            diffDilate = Math.abs(imgDilate.getPixel(i) - img.getPixel(i));
            if(diff >= limiar && diffErode <= diffDilate){
                level = true;
            }if(diff >= limiar && diffErode > diffDilate){
            	level = false;
            }else{
                level = imgOtsu.getPixel(i);
            }
            
            imgOut.setPixel(i, level);
        }
        return imgOut;
    }
    
    
    
    /**
     * Thresholding local baseado na media e no desvio padrao
     * @param size - tamanho do kernel
     * @param con - correcao na media
     * @return IBinaryImage
     */
    public BinaryImage meanAndStandardDeviatio(int size, float con){
        BinaryImage imgOut = new BitImage(img.getWidth(), img.getHeight());
        int k = (int) Math.floor(size / 2);
        double mean = 0;
        int count;
        double sum =0;
        double sd = 0;
        int value = 0;
        for(int w = 0; w < img.getWidth(); w++){
          for(int h = 0; h < img.getHeight(); h++){
              mean = 0;
              count = 0;
              sum =0;
              for(int x=-k; x <= k; x++){ 
                  for(int y=-k; y < k; y++){
                      if(img.isPixelValid(w + x, h + y)) {
                          sum += img.getPixel(w + x, h + y);
                          count++;
                      }
                  }
              }
          
              mean = sum /count;
              
              for(int x=-k; x <= k; x++){ 
                  for(int y=-k; y < k; y++){
                      if(img.isPixelValid(w + x, h + y)) {
                          sd += Math.pow(img.getPixel(w + x, h + y) - mean, 2);
                      }
                  }
              }
              sd =  Math.sqrt(sd / sum);
              
              value = (int) Math.floor(mean + (con * sd));
              
              //Threshold below the mean
              if(img.getPixel(w,h) > value){
                  imgOut.setPixel(w, h, true);
              }
              else {
                  imgOut.setPixel(w, h, false);
              }
          }
        }
        return imgOut;
    }
    
    
    /**
     * Thresholding local baseado na media
     * @param size - tamanho do kernel
     * @param con - correcao na media
     * @return IBinaryImage
     */
    public BinaryImage mean(int size, float con){
        BinaryImage imgOut = new BitImage(img.getWidth(), img.getHeight());
        int k = (int) Math.floor(size / 2);
        int mean = 0;
        int count;
        int sum =0;
        for(int w = 0; w < img.getWidth(); w++){
          for(int h = 0; h < img.getHeight(); h++){
              mean = 0;
              count = 0;
              sum =0;
              for(int x=-k; x <= k; x++){ 
                  for(int y=-k; y < k; y++){
                      if(!img.isPixelValid(w + x, h + y))
                          sum +=0;
                      else {
                          sum += img.getPixel(w + x, h + y);
                          count++;
                      }
                  }
              }
          
              mean = (int)Math.floor((sum /count) - con);
              //Threshold below the mean
              if(img.getPixel(w,h) > mean){
                  imgOut.setPixel(w, h, true);
              }
              else {
                  imgOut.setPixel(w, h, false);
              }
          }
        }
        return imgOut;
    }
    
    /**
     * Thresholding local baseado na mediana
     * @param size - tamanho do kernel
     * @param con - correcao na media
     * @return IBinaryImage
     */
    public BinaryImage median(int size, int con){
        int hist[] = new int[size*size];
        int k = (int) Math.floor(size / 2);
        BinaryImage imgOut = new BitImage(img.getWidth(), img.getHeight());
        int median;
        for(int w = 0; w < img.getWidth(); w++){
          for(int h = 0; h < img.getHeight(); h++){
              int j = 0;
              for(int x=-k; x <= k; x++){ //Varendo o elemento estruturante
                  for(int y=-k; y < k; y++){
                      if(!img.isPixelValid(w + x, h + y))
                          hist[j++] =0;
                      else 
                          hist[j++] = img.getPixel(w + x, h + y);
                  }
              }
              Arrays.sort(hist);
              median = hist[hist.length / 2] - con;          
              if(img.getPixel(w,h) >= median){
                  imgOut.setPixel(w, h, true);
              }
              else {
                  imgOut.setPixel(w, h, false);
              }
          }
        }
        return imgOut;
    }
    

    
    
}
