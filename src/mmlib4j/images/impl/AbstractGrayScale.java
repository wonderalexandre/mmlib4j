package mmlib4j.images.impl;

import java.util.LinkedList;

import mmlib4j.images.ColorImage;
import mmlib4j.images.GrayScaleImage;
import mmlib4j.segmentation.Labeling;
import mmlib4j.utils.AdjacencyRelation;
import mmlib4j.utils.ImageUtils;

/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 */
public abstract class AbstractGrayScale extends AbstractImage2D implements GrayScaleImage{

	private Statistics stats = null;
    //protected int padding = Integer.MIN_VALUE; 
    
    public void paintBoundBox(int x1, int y1, int x2, int y2, int color){
        int w = x2 - x1;
        int h = y2 - y1;
        for(int i=0; i < w; i++){
            for(int j=0; j < h; j++){
                if(i == 0 || j == 0 || i == w-1 || j == h-1)
                    setPixel(x1 + i, y1 + j, color);        
            }
        }
    }
    
    public int countPixels(int color) {
        int area =0;
        for(int p=0; p < getSize(); p++){
        	if(getPixel(p) == color){
        		area++;
        	}
        }
        return area;
    }
    
    /**
     * Inicializar todos os pixel da imagem para um dado nivel de cinza
     * @param color
     */
    public void initImage(int color){
    	for(int p=0; p < getSize(); p++){
    		setPixel(p, color);
    	}
    }
    
    /**
     * modifica todos os niveis de cinza de uma dada cor para uma outra cor
     * @param colorOld
     * @param colorNew
     */
    public void replaceValue(int colorOld, int colorNew){
    	for(int i=0; i < this.getSize(); i++){
    		if(getPixel(i) == colorOld)
    			setPixel(i, colorNew);
    	}
    }
 
    /*
	public int getValuePixel(int x, int y) {
		return isPixelValid(x, y) ? getPixel(x, y): padding;
	}
	
	public int getValuePixel(int i) {
		return isPixelValid(i) ? getPixel(i): padding;
		
	}
	*/
    
	public int getValue(int x, int y) {
		return getPixel(getPixelIndexer().getIndex(x, y));
	}
	
	public int getValue(int p) {
		return getPixel(getPixelIndexer().getIndex(p));
		
	}


 
    /**
     * Pega um histograma da imagem
     * @return int[]
     */
    public int[] getHistogram() {
       int result[] = new int[(int)Math.pow(2, getDepth())];
       for(int p=0; p < getSize(); p++){
    	   result[getPixel(p)]++;
       }
       return result;
    }
    
    public LinkedList<Integer>[] getPixelsOfHistogram(){
    	LinkedList<Integer> result[] = new LinkedList[(int)Math.pow(2, getDepth())];
        for(int p=0; p < this.getSize(); p++){
        	if(result[this.getPixel(p)] == null)
        		result[this.getPixel(p)] = new LinkedList<Integer>();
        	result[this.getPixel(p)].add(p);
        }
        return result;
    }

    /**
     * Pega o quantidade de valores diferente na imagem
     */
    public int numValues() {
        int h[] = getHistogram();
        int numPixel = 0;
        for(int i=0; i < h.length; i++) 
            if(h[i] > 0) numPixel++;
        return numPixel;
        
    }
    /*
    public void setPadding(int value){
    	this.padding = value;
    }*/
    
    /**
     * Verifica se duas imagens sao iguais
     * @param img - IGrayScaleImage
     * @return true se forem iguais false caso contrario
     */
    public boolean equals(Object o){
        GrayScaleImage img = (GrayScaleImage) o;
        for(int p=0; p < getSize(); p++){
        	if(getPixel(p) != img.getPixel(p)) 
        		return false;
        }
        return true;
    }
    
    public void add(int a){
    	int maxValue = (int) Math.pow(2, getDepth()) - 1;
    	for(int p=0; p < getSize(); p++){
    		if(getPixel(p) + a > maxValue){
    			setPixel(p, maxValue);
    		}
    		else if(getPixel(p) + a < 0){
    			setPixel(p, 0);
    		}
    		else{
    			setPixel(p, getPixel(p) + a);
    		}
        }
        
    }
    public void multiply(double a){
    	int maxValue = (int) Math.pow(2, getDepth()) - 1;
    	for(int p=0; p < getSize(); p++){
    		if(getPixel(p) * a > maxValue){
    			setPixel(p, maxValue);
    		}
    		else if(getPixel(p) * a < 0){
    			setPixel(p, 0);
    		}
    		else{
    			setPixel(p, (int) (getPixel(p) * a));
    		}
        }
    }

    public void invert(){
    	int maxValue = (int) Math.pow(2, getDepth()) - 1;
    	for(int p=0; p < getSize(); p++){
    		setPixel(p, maxValue - this.getPixel(p));
    	}
    }
    
    public GrayScaleImage getInvert(){
    	GrayScaleImage imgOut = ImageFactory.createGrayScaleImage(this.getDepth(), this.getWidth(), this.getHeight());
    	imgOut.invert();
    	return  imgOut;
    }

    public ColorImage randomColor(int alpha){
    	int max = this.maxValue();
    	int r[] = new int[max+1];
    	int g[] = new int[max+1];
    	int b[] = new int[max+1];
    	for(int i=1; i <= max; i++){
    		r[i] = ImageUtils.randomInteger(0, 255);
    		g[i] = ImageUtils.randomInteger(0, 255);
    		b[i] = ImageUtils.randomInteger(0, 255);
    	}
    	ColorImage imgOut = new RGBImage(this.getWidth(), this.getHeight());
    	imgOut.setAlpha(alpha);
        for(int i=0; i < imgOut.getSize(); i++){
        	imgOut.setRed(i, r[i]);
        	imgOut.setGreen(i, g[i]);
        	imgOut.setBlue(i, b[i]);
        }
        return imgOut;
    }
    
    public ColorImage randomColor(){
    	int max = this.maxValue();
    	int r[] = new int[max+1];
    	int g[] = new int[max+1];
    	int b[] = new int[max+1];
    	for(int i= 1; i <= max; i++){
    		r[i] = ImageUtils.randomInteger(0, 255);
    		g[i] = ImageUtils.randomInteger(0, 255);
    		b[i] = ImageUtils.randomInteger(0, 255);
    	}
    	ColorImage imgOut = new RGBImage(this.getWidth(), this.getHeight());
        for(int i=0; i < imgOut.getSize(); i++){
        	imgOut.setRed(i, r[this.getPixel(i)]);
        	imgOut.setGreen(i, g[this.getPixel(i)]);
        	imgOut.setBlue(i, b[this.getPixel(i)]);
        }
        return imgOut;
    }
    
    

    /**
     * Pega o valor do maior pixel da imagem
     */
    public int maxValue() {
        if(stats == null)
            loadStatistics();
        return stats.max;
    }
    

    /**
     * Pega o valor da media dos pixels da imagem
     */
    public int meanValue() {
    	if(stats == null)
            loadStatistics();
        return stats.mean;
    }
    
    
    
    

    /**
     * Pega o valor menor pixel da imagem
     */
    public int minValue() {
    	if(stats == null)
            loadStatistics();
        return stats.min;
    }
    
    public ColorImage labeling(AdjacencyRelation adj){
    	return Labeling.labeling(this, adj).randomColor();
    }
    
    /**
     * Pega o maior pixel da imagem
     */
    public int maxPixel() {
    	if(stats == null)
            loadStatistics();
        return stats.pixelMax;
    }

    /**
     * Pega o menor pixel da imagem
     */
    public int minPixel() {
    	if(stats == null)
            loadStatistics();
        return stats.pixelMin;
    }
    

	public void drawLine(int x1, int y1, int x2, int y2, int grayLine){
		//algoritmo de bresenham
		if(Math.abs( x2 - x1 ) > Math.abs( y2 - y1 )){
			if(x1 > x2) drawLine(x2, y2, x1, y1, grayLine);
			int a = x2 - x1;
			int b = y2 -y1;
			
			int inc = 1;
			if(b<0){
				inc = -1;
				b = -b;
			}
			int v = 2 * a + b;
			int neg = 2 * b;
			int pos = 2 * (b - a);
			int x = x1;
			int y = y1;
			this.setPixel(x, y, grayLine);
				
			while (x<= x2){
				if(isPixelValid(x, y))
					this.setPixel(x, y, grayLine);
					
				x= x + 1;
				if(v <= 0){
					v = v + neg;
				}else{
					y = y + inc;
					v = v+ pos;
				}
			}
		}else{
			if(y1 > y2) drawLine(x2, y2, x1, y1, grayLine);
			int b = x2 - x1;
			int a = y2 - y1;
			int inc = 1;
			if( b < 0){
				inc = -1;
				b = -b;
			}
			int v = 2 * b - a;
			int neg = 2 * b;
			int pos = 2 * (b - a);
			int x = x1;
			int y = y1;
			this.setPixel(x, y, grayLine);	
			while(y <= y2){
				if(isPixelValid(x, y))
					this.setPixel(x, y, grayLine);
				y = y + 1;
				if(v <= 0){
					v = v + neg;
				}else{
					x = x + inc;
					v = v + pos;
				}
			}
		}
	}
    
    public void loadStatistics(){
    	stats = new Statistics();
    }
    
    
    class Statistics{
    	int min;
    	int max;
    	int pixelMin;
    	int pixelMax;
    	int mean;
        
    	Statistics(){
        	max = Integer.MIN_VALUE;
            min = Integer.MAX_VALUE;
            int sum=0;
            for (int i = 0; i < getSize(); i++){
            	sum += getPixel(i);
            	if(getPixel(i) < min){
            		min = getPixel(i);
            		pixelMin = i;
            	}
            	if(getPixel(i) > max){
            		max = getPixel(i);
            		pixelMax = i;
            	}
            }
            mean = sum / (getWidth() * getHeight());
        }
    }
}
