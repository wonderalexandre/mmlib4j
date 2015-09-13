package mmlib4j.images.impl;

import java.util.Iterator;

import mmlib4j.images.BinaryImage;
import mmlib4j.images.GrayScaleImage;



/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 * @description
 * Classe que representa uma imagem binaria de 1 bit de profundidade
 */
public class BitImage extends AbstractImage2D implements BinaryImage{
   
	private boolean pixels[]; 
    
    /**
     * Construtor para criar uma nova imagem
     * @param width - largura
     * @param height - altura 
     */
    public BitImage(int width, int height) {
        this.width = width;
        this.height = height;
        pixels = new boolean[width * height];
    }

    public BitImage(boolean pixels[], int width, int height) {
        this.width = width;
        this.height = height;
        this.pixels = pixels;
    }
    
    /**
     * Inicializar todos os pixel da imagem para um dado nivel de cinza
     * @param color
     */
    public void initImage(boolean color){
        for (int p = 0; p < getSize(); p++){
        	pixels[p] = color;
        }
    }
    
    /**
     * Cria uma copia da imagem original
     * @return BinaryImage - nova imagem
     */
    public BinaryImage duplicate(){
        BitImage clone = new BitImage(getWidth(), getHeight());
        System.arraycopy(this.pixels, 0, clone.pixels, 0, this.pixels.length);
		return  clone;
    }
    
    public Iterable<Integer> scanForwardObjectPixel(){
    	final int size = getSize();
    	return new Iterable<Integer>() {
			public Iterator<Integer> iterator() {
				return new Iterator<Integer>() {
					int p=0;
					public boolean hasNext() {
						while(p < size && !isPixelForeground(p)){
							p++;
						}
						return p < size;
					}
					public Integer next() {
						return p++;
					}
					public void remove() { }
					
				};
			}
		};
    }
    
    /**
     * Pega o valor do pixel (x, y)
     * @param x - largura
     * @param y - altura
     * @return float - valor do pixel
     */
    public boolean getPixel(int x, int y){
    	return pixels[y * width + x];
    }
    
    /**
     * Calculando uma estimacao da orientacao do componente conexo (em radiano)
     * Esse procedimento e baseado em momentos estatiscos
     * @param img - imagem de entrada
     * @return orientacao
     */
    public static double getOrientationEstimationInRadian(BinaryImage img){
        double xx = 0, yy = 0;
        //double u00 = 0;
        double u20 = 0, u02 = 0, u11 = 0;
        double thetaR = 0;
         
        //calculando o momento central
        for (int x = 0; x < img.getWidth(); x++) {
            for (int y = 0; y < img.getHeight(); y++) {
                if(img.isPixelBackground(x, y))
            	xx += x;
                yy += y; 
            }
        }
        double m00 = img.getArea();
        xx = xx / m00;
        yy = yy / m00;
        
        //Calculado os momentos de segunda ordem 
        //u00 = getMoment(img, xx, yy, 0, 0);
        u20 = getMoment(img, xx, yy, 2, 0);
        u02 = getMoment(img, xx, yy, 0, 2);
        u11 = getMoment(img, xx, yy, 1, 1);
       
        //calculando a orientacao
        thetaR = 0.5 * Math.atan2( 2.0 * u11, u20 - u02 );
        return thetaR;
    }
   
    public static double getMoment(BinaryImage img, double xCentroide, double yCentroide, int p, int q){
        double result = 0;
        for (int x = 0; x < img.getWidth(); x++) {
            for (int y = 0; y < img.getHeight(); y++) {
            	if(img.isPixelBackground(x, y))
            		result += Math.pow(x - xCentroide, p) * Math.pow(y - yCentroide, q) ;
            }
        }
        return result;
    }
   
    
    /**
     * Modifica o valor do pixel (x, y) = value
     * @param x - largura
     * @param y - altura
     * @param value - valor do pixel
     */
    public void setPixel(int x, int y, boolean value){
        pixels[y * width + x] = value;
    }
    
    /**
     * Pega uma matriz bidimensional de pixel da imagem
     * @return int[][]
     */
    public boolean[] getPixels(){
        return pixels;
    }
    
    /**
     * Modifica a matriz de pixel da imagem para os valores da matriz dada
     * @param matrix 
     */
    public void setPixels(int width, int height, boolean pixels[]){
        this.width = width;
        this.height = height;
        this.pixels = pixels;
       
    }
    

    
    /**
     * Converte uma imagem binaria em niveis de cinza
     * @return IGrayScaleImage
     */
    public GrayScaleImage convertGrayScale(){
        GrayScaleImage clone = new ByteImage(getWidth(), getHeight());
        for (int i = 0; i < getWidth(); i++){
            for (int j = 0; j < getHeight(); j++){
                clone.setPixel(i,j, getPixel(i, j)?1:0);
            }
        }
        return clone;
    }
    
    /**
     * Pega um histograma da imagem
     * @return int[]
     */
    public int[] getHistogram() {
       int result[] = new int[2];
       result[0] = getSize() - getArea();
       result[1] = getSize() - result[0];
       return result;
    }
    
    public int getArea(){
        int area =0;
        for(int p=0; p < getSize(); p++){
        	if(getPixel(p)){
        		area++;
            }
        }
        return area;
    }

    
    
    /**
     * Verifica se duas imagens sao iguais
     * @param img - IGrayScaleImage
     * @return true se forem iguais false caso contrario
     */
    public boolean equals(Object o){
        BinaryImage img = (BinaryImage) o;
        for(int p=0; p < getSize(); p++){
        	if(getPixel(p) != img.getPixel(p)) 
        		return false;
        }        
        return true;
    }

    /**
     * Inverte as tonalidades de cores da imagem
     */
    public BinaryImage invert() {
        BitImage clone = new BitImage(getWidth(), getHeight());
        for (int i = 0; i < getSize(); i++){
        	clone.setPixel(i, !getPixel(i));
        }
        return clone;
    }
    
    /**
     *  Rotaciona 90 graus sentido anti-horario.
     */
    public BinaryImage rot90(){
        BitImage d = new BitImage(this.getHeight(), this.getWidth());
        for (int l=0; l < this.getWidth(); ++l)
            for (int c=0; c < this.getHeight(); ++c){
                if(d.isPixelValid(this.getHeight() - c - 1 , l))
                    d.setPixel(this.getHeight() - c - 1 , l,  getPixel(l,c));
            }
        return d;
    }

    /**
     *  Rotaciona 180 graus sentido anti-horario.
     */
    public BinaryImage rot180(){
        BitImage d = new BitImage(this.getWidth(), this.getHeight());
        for (int l=0; l < this.getWidth(); ++l)
            for (int c=0; c < this.getHeight(); ++c)
                if(d.isPixelValid(this.getWidth() - l - 1, getHeight() - c - 1))
                    d.setPixel(this.getWidth() - l - 1, getHeight() - c - 1,  getPixel(l,c));
      
        return d;
    }

    /**
     * Rotaciona 270 graus sentido anti-horario.
     */
    public BinaryImage rot270(){
         BitImage d = new BitImage(this.getHeight(), this.getWidth());
         for (int l=0; l < this.getWidth(); ++l)
             for (int c=0; c < this.getHeight(); ++c)
                     d.setPixel(c, getWidth() - l - 1, getPixel(l,c));
         return d;
    }
    
    /**
     * Pega o histograma da projecao dos pixels em X
     */
    public int[] getHistogramXprojection() {
        int hist[] = new int[this.getWidth()];
        for(int i=0; i < this.getWidth(); i++){
            for(int j=0; j < this.getHeight(); j++){
                if(this.getPixel(i, j)){
                    hist[i]++;
                }
            }
        }
        return hist;
    } 

    /**
     * Pega o histograma da projecao dos pixels em Y
     */
    public int[] getHistogramYprojection() {
        int hist[] = new int[this.getHeight()];
        for(int i=0; i < this.getWidth(); i++){
            for(int j=0; j < this.getHeight(); j++){
                if(this.getPixel(i, j)){
                    hist[j]++;
                }
            }
        }
        return hist;  
    }
    
    

    public void setPixel(int i, boolean level){
    	pixels[i] = level;
    }
    
    public boolean getPixel(int i){
    	return pixels[i];
    }
    
    public boolean isPixelBackground(int i){
        return !getPixel(i);
    }
    
    public boolean isPixelForeground(int i){
        return getPixel(i);
    }
    
    public boolean isPixelBackground(int x, int y){
        return !getPixel(x, y);
    }
    
    public boolean isPixelForeground(int x, int y){
        return getPixel(x, y);
    }
    
    public int getDepth(){
    	return 1;
    }
    

	public void drawLine(int x1, int y1, int x2, int y2){
		//algoritmo de bresenham
		if(Math.abs( x2 - x1 ) > Math.abs( y2 - y1 )){
			if(x1 > x2) drawLine(x2, y2, x1, y1);
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
			this.setPixel(x, y, true);
				
			while (x<= x2){
				if(isPixelValid(x, y))
					this.setPixel(x, y, true);
					
				x= x + 1;
				if(v <= 0){
					v = v + neg;
				}else{
					y = y + inc;
					v = v+ pos;
				}
			}
		}else{
			if(y1 > y2) drawLine(x2, y2, x1, y1);
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
			this.setPixel(x, y, true);	
			while(y <= y2){
				if(isPixelValid(x, y))
					this.setPixel(x, y, true);
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
}
