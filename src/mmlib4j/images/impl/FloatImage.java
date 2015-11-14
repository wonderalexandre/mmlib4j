package mmlib4j.images.impl;

import mmlib4j.images.RealImage;


/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 * @description
 * Classe que representa uma imagem float   
 * 
 */ 
public class FloatImage extends AbstractImage2D implements RealImage {

    private float pixels[]; //matriz de pixel da imagem
    

    /**
     * Construtor para criar uma nova imagem
     * @param width - largura
     * @param height - altura
     */
    FloatImage(int width, int height) {
        this.width = width;
        this.height = height;
        this.pixels = new float[width * height];
        setPixelIndexer( PixelIndexer.getExceptionIndexer(getWidth(), getWidth()) );
    }
     
    /**
     * Construtor para criar uma nova imagem
     * @param width - largura
     * @param height - altura
     */
    FloatImage(float pixels[], int width, int height) {
        setPixels(width, height,pixels);
    }
    
    /**
     * Inicializar todos os pixel da imagem para um dado nivel de cinza
     * @param color
     */
    public void initImage(float color){
        for (int i = 0; i < getWidth(); i++){
            for (int j = 0; j < getHeight(); j++){
                setPixel(i,j, color);
            }
        }
    }
    
    public float getValue(int x, int y) {
		return getPixel(getPixelIndexer().getIndex(x, y));
	}
	
	public float getValue(int p) {
		return getPixel(getPixelIndexer().getIndex(p));
	}

    /**
     * Cria uma copia da imagem original
     * @return FloatImage - nova imagem
     */
    public FloatImage duplicate(){
        FloatImage clone = new FloatImage(getWidth(), getHeight());
        System.arraycopy(this.pixels, 0, clone.pixels, 0, this.pixels.length);
        return clone;
    }

    /**
     * modifica todos os niveis de cinza de uma dada cor para uma outra cor
     * @param colorOld
     * @param colorNew
     */
    public void replaceValue(float colorOld, float colorNew){
    	for(int i=0; i < this.getSize(); i++){
    		if(getPixel(i) == colorOld)
    			setPixel(i, colorNew);
    	}
    }
    
    
    /**
     * Pega o valor do pixel (x, y)
     * @param x - largura
     * @param y - altura
     * @return float - valor do pixel
     */
    public float getPixel(int x, int y){
        return pixels[y * width + x];
    }
    
    
    /**
     * Modifica o valor do pixel (x, y) = value
     * @param x - largura
     * @param y - altura
     * @param value - valor do pixel
     */
    public void setPixel(int x, int y, float value){
        pixels[y * width + x] = value;
    }
    
    /**
     * Pega uma matriz bidimensional de pixel da imagem
     * @return float[][]
     */
    public float[] getPixels(){
        return pixels;
    }

    
    
    /**
     * Modifica a matriz de pixel da imagem para os valores da matriz dada
     * @param matrix 
     */
    public void setPixels(int width, int height, float pixels[]){
        this.width = width;
        this.height = height;
        this.pixels = pixels;
        setPixelIndexer( PixelIndexer.getExceptionIndexer(getWidth(), getWidth()) );
    }
    

    /**
     * Pega o maior pixel da imagem
     */
    public float getPixelMax() {
        float max = Float.MIN_VALUE;
        for (int i = 0; i < getWidth(); i++){
            for (int j = 0; j < getHeight(); j++){
                if(getPixel(i, j) > max)
                    max = getPixel(i, j);
            }
        }
        return max;
    }
    
    /**
     * Pega o menor pixel da imagem
     */
    public float getPixelMin() {
        float min = Float.MAX_VALUE;
        for (int i = 0; i < getWidth(); i++){
            for (int j = 0; j < getHeight(); j++){
                if(getPixel(i, j) < min)
                    min = getPixel(i, j);
            }
        }
        return min;
    }

    /**
     * Verifica se duas imagens sao iguais
     * @param img - IGrayScaleImage
     * @return true se forem iguais false caso contrario
     */
    public boolean equals(Object o){
        FloatImage img = (FloatImage) o;
        for(int x = 0 ; x < getWidth() ; x++)
            for(int y = 0 ; y < getHeight(); y++)
                if(getPixel(x, y) != img.getPixel(x, y)) 
                    return false;
                
        return true;
    }

   
    public void setPixel(int i, float level){
    	pixels[i] = level;
    }
    
    public float getPixel(int i){
        return pixels[i];
    }
    
    public boolean isPixelValid(int x, int y){
        return (x >= 0 && x < this.getWidth() && y >= 0 && y < this.getHeight());
    }

    public boolean isPixelValid(int p){
    	return isPixelValid(p % this.getWidth(), p / this.getWidth());
    }
   
    public int getDepth(){
    	return 32;
    }

}
