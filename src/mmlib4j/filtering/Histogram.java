package mmlib4j.filtering;

import mmlib4j.gui.WindowHistogram;
import mmlib4j.images.ColorImage;
import mmlib4j.images.GrayScaleImage;
import mmlib4j.utils.ImageUtils;

/**
 * Project: Computer Vision Framework
 * 
 * @author Wonder Alexandre Luz Alves
 * @advisor Ronaldo Fumio Hashimoto
 * 
 * @date 20/01/2008
 *  
 * @description
 * Essa classe define operacoes sobre o histograma  
 * 
 */
public class Histogram {

    GrayScaleImage img;
    
    int[] hist;
    float histRelative[];
    int histAcc[];
    float histAccRelative[];
    
    double meanPeak;
    int maxPeak;
    int minPeak;
    double sd;
    
    public Histogram(GrayScaleImage img){
        this.img = img;
        hist = img.getHistogram();
        
        histRelative = new float[hist.length];
        histAcc = new int[hist.length];
        histAccRelative = new float[hist.length];
        
        
        //pre-processing
        int n = img.getHeight() * img.getWidth();
        
        int sumAcc = 0;
        for(int i=0; i < histAcc.length; i++){
        	histRelative[i] = ((float) hist[i]) / n;
        	sumAcc += hist[i];
            histAcc[i] = sumAcc;
            histAccRelative[i] = ((float) histAcc[i]) / n;
        }
        
        double mean = img.meanValue();
        meanPeak = 0.0;
        maxPeak = 0;
        minPeak = n;
        sd=0;
        for (int k=0;  k<hist.length; k++)  {
            meanPeak += hist[k];
            sd += ((k - mean) * (k - mean)) * hist[k];
            if(hist[k] > maxPeak)
            	maxPeak = hist[k];
            if(hist[k] < minPeak)
            	minPeak = hist[k];
        }
        
        meanPeak = (meanPeak / hist.length);
        sd = Math.sqrt(sd / n);
        
    }
    
   
    
   
    /**
     * Pega o histograma acumulado
     * @return int[]
     */
    public int[] getHistogramAccumulator(){
        return histAcc; 
    }
    
    /**
     * Pega o histograma acumulado de frequencia relativa
     * @return float[]
     */
    public float[] getHistogramAccumulatorRelative(GrayScaleImage img){
        return histAccRelative;
        
    }
    
    /**
     * Pega o histograma de frequencia relativa
     * @return float[]
     */
    public float[] getHistogramRelative(GrayScaleImage img){
        return histRelative;
    }
    
    /**
     * Pega o histograma da imagem
     * @return
     */
    public int[] getHistogram(){
        return hist;
    }
    
    /**
     * Paga a media dos picos do histograma
     * @return double
     */
    public double getMeanPeak(){
        return meanPeak;
    }
    

    /**
     * Pega o valor do pico maximo do histograma
     * @return double
     */
    public int getMaxPeak(){
    	return maxPeak;
    }
    
    public int getMinPeak(){
    	return minPeak;
    }
    
    /**
     * Pega o desvio padrao do histograma
     * @return double
     */
    public double getStandardDeviatio(){
    	return sd;
    }
    
    /**
     * Equalizacao do histograma
     * @return IGrayScaleImage
     */
    public GrayScaleImage equalisation(){
        int lut[] = new int[histAccRelative.length]; //look-up table
        int l = 256 - 1;
        for(int i=0; i < histAccRelative.length; i++){ //construindo look-up table
            lut[i] =(int) Math.floor(l * histAccRelative[i]);
        }
        return getImageTransform(lut, img);
    }
   
    
    /**
     * cria uma look-up table linear
     * @param b - coeficiente angular
     * @return int[]
     */
    public int[] createLookUpTableLinear(int b){
        int lut[] = new int[hist.length]; //look-up table
        for(int i=0; i < lut.length; i++){ //construindo look-up table
            lut[i] =(int) Math.floor(i * b);
        }
        return lut;
    }

    /**
     * cria uma look-up table logaritmica
     * 
     * @return int[]
     */
    public int[] createLookUpTableLogarithmic(){
        int lut[] = new int[hist.length]; //look-up table
        float a = (float) (hist.length-1 / ImageUtils.log10(hist.length));
        
        for(int i=0; i < lut.length; i++){ //construindo look-up table
            lut[i] =(int) Math.floor(a * Math.log(i + 1));
        }
        return lut;
    }
    
    /**
     * cria uma look-up table exponencial
     * 
     * @return int[]
     */
    public int[] createLookUpTableExponential(){
        int lut[] = new int[hist.length]; //look-up table
        for(int i=0; i < lut.length; i++){ //construindo look-up table 
            //lut[i] =(int) Math.floor((i-1) / Math.log1p(256));
        	//45.986 = i / (255 / ln(256))
        	 //lut[i] =(int) Math.exp(i / 45.986);
        	lut[i] =(int) Math.exp(i /  (hist.length-1) / Math.log1p(hist.length-1) );
        }
        return lut;
    }

    /**
     * cria uma look-up table potencia
     * 
     * @return int[]
     */
    public int[] createLookUpTablePotency(int y){
        int lut[] = new int[hist.length]; //look-up table
        for(int i=0; i < lut.length; i++){ //construindo look-up table 
            lut[i] =(int) Math.floor(Math.pow(hist.length-1, 1-y) * Math.pow(i, y));
        }
        return lut;
    }
    
    /**
     * Normaliza os valores dos pixel que sairem do intervalo de [0-2^bits-1] para um invertalo valido
     * @param p - valor do pixel
     * @return int
     */
    public int getValue(int p){
        if(p < 0)
            return 0;
        else if(p > hist.length-1)
            return hist.length-1;
        else 
            return p;
    }
    
    /**
     * Pega uma imagem transformada por uma look-up table
     * @param lut - look-up table
     * @param IGrayScaleImage - imagem transformada 
     */
    public GrayScaleImage getImageTransform(int lut[], GrayScaleImage img){
        GrayScaleImage imgOut = img.duplicate();
        for(int w=0; w < img.getWidth(); w++){
            for(int h=0; h < img.getHeight(); h++){
                imgOut.setPixel(w, h, getValue(lut[img.getPixel(w, h)]));
            }
        }
        return imgOut;
    }
    
    public ColorImage getGraphic(){
    	return WindowHistogram.getGraphic(img);
    }
    
    
}
