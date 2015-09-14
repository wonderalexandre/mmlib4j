package mmlib4j.filtering;

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
    
    public Histogram(GrayScaleImage img){
        this.img = img;
    }
   
   
    /**
     * Pega o histograma acumulado
     * @return int[]
     */
    public int[] getHistogramAccumulator(){
        int result[] = new int[256];
        int h[] = img.getHistogram();
        int aux = 0;
        for(int i=0; i < result.length; i++){
            aux += h[i];
            result[i] = aux;
           
        }
        return result; 
    }
    
    /**
     * Pega o histograma acumulado de frequencia relativa
     * @return float[]
     */
    public float[] getHistogramAccumulatorRelative(){
        int n = img.getHeight() * img.getWidth();
        int h[] = getHistogramAccumulator();
        float result[] = new float[h.length];
        for(int i=0; i < result.length; i++){
            result[i] = (float) h[i] / n;
            
        }
        return result;
        
    }
    
    /**
     * Pega o histograma de frequencia relativa
     * @return float[]
     */
    public float[] getHistogramRelative(){
        int n = img.getHeight() * img.getWidth();
        int h[] = img.getHistogram();
        float result[] = new float[h.length];
        for(int i=0; i < result.length; i++){
            result[i] = (float) h[i] / n;
        }
        return result;
    }
    
    /**
     * Pega o histograma da imagem
     * @return
     */
    public int[] getHistogram(){
        return this.img.getHistogram();
    }
    /**
     * Paga a media do histograma
     * @return double
     */
    public double getMean(){
        double mean = 0.0;
        int histogram[] = img.getHistogram();
        for (int k=0;  k<histogram.length; k++)  {
            mean += (k+1) * histogram[k];
        }
        return (mean / (img.getHeight() * img.getWidth()));
    }
    

    /**
     * Paga a media dos picos do histograma
     * @return double
     */
    public double getMeanPeak(){
        double mean = 0.0;
        int histogram[] = img.getHistogram();
        for (int k=0;  k<histogram.length; k++)  {
            mean += histogram[k];
        }
        return (mean / histogram.length);
    }
    

    /**
     * Pega o valor do pico maximo do histograma
     * @return double
     */
    public int getMaxPeak(){
        int max = 0;
        int histogram[] = img.getHistogram();
        for (int k=0;  k<histogram.length; k++)  {
            if(histogram[k] > max)
                max = histogram[k];
        }
        return max;
    }
    
    /**
     * Pega o desvio padrao do histograma
     * @return double
     */
    public double getStandardDeviatio(){
        double mean = getMean();
        double sum = 0;
        int histogram[] = img.getHistogram();
        for (int k=0;  k<histogram.length; k++)  {
            sum += Math.pow(k - mean, 2) * histogram[k];
        }
        return Math.sqrt(sum / ((img.getHeight() * img.getWidth())-1));
        
    }
    
    /**
     * Equalizacao do histograma
     * @return IGrayScaleImage
     */
    public GrayScaleImage equalisation(){
        float hfa[] = getHistogramAccumulatorRelative();
        int lut[] = new int[hfa.length]; //look-up table
        int l = 256 - 1;
        for(int i=0; i < hfa.length; i++){ //construindo look-up table
            lut[i] =(int) Math.floor(l * hfa[i]);
        }
        return getImageTransform(lut, img);
    }
   
    
    /**
     * cria uma look-up table linear
     * @param b - coeficiente angular
     * @return int[]
     */
    public int[] createLookUpTableLinear(int b){
        int lut[] = new int[256]; //look-up table
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
        int lut[] = new int[256]; //look-up table
        float a = (float) (255 / ImageUtils.log10(256));
        
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
        int lut[] = new int[256]; //look-up table
        for(int i=0; i < lut.length; i++){ //construindo look-up table 
            //lut[i] =(int) Math.floor((i-1) / Math.log1p(256));
        	//45.986 = i / (255 / ln(256))
        	 lut[i] =(int) Math.exp(i / 45.986);
        }
        return lut;
    }

    /**
     * cria uma look-up table potencia
     * 
     * @return int[]
     */
    public int[] createLookUpTablePotency(int y){
        int lut[] = new int[256]; //look-up table
        for(int i=0; i < lut.length; i++){ //construindo look-up table 
            lut[i] =(int) Math.floor(Math.pow(255, 1-y) * Math.pow(i, y));
        }
        return lut;
    }
    
    /**
     * Normaliza os valores dos pixel que sairem do intervalo de [0-255] para um invertalo valido
     * @param p - valor do pixel
     * @return int
     */
    public static int getPixelNormalized(int p){
        if(p < 0)
            return 0;
        else if(p > 255)
            return 255;
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
        int k;
        for(int w=0; w < img.getWidth(); w++){
            for(int h=0; h < img.getHeight(); h++){
                imgOut.setPixel(w, h, getPixelNormalized(lut[img.getPixel(w, h)]));
            }
        }
        return imgOut;
    }
    
    
    
    /**
     * Pega o histograma equalizado
     * @return IGrayScaleImage
     */
    public int[] getHistogramEqualisation(){
        float hfa[] = getHistogramAccumulatorRelative();
        int lut[] = new int[hfa.length]; //look-up table
        int l = 256 - 1;
        for(int i=0; i < hfa.length; i++){
            lut[i] =(int) Math.floor(l * hfa[i]);
        }
        int hist[] = img.getHistogram();
        int histEq[] = new int[hfa.length]; //histograma equalizado
        for(int i=0; i < histEq.length; i++){
            if(hist[i] != 0){
                histEq[lut[i]] = hist[i];
            }
        }
        return histEq;
    }
    
    
}
