package mmlib4j.images.impl;

import mmlib4j.images.GrayScaleImage;



/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 *
 * @description
 * Classe que representa uma imagem em tons de cinza. 
 * A profundidade dos pixels eh 32bits (int)  
 * 
 */ 
public class ShortImage extends AbstractGrayScale implements GrayScaleImage{
    
	private short pixels[]; //matriz de pixel da imagem
	
	ShortImage(int width, int height) {
        this.width = width;
        this.height = height;
        this.pixels = new short[width * height];
        setPixelIndexer( PixelIndexer.getExceptionIndexer(getWidth(), getHeight()) );
    }

	ShortImage(short pixels[], int width, int height) {
        this.width = width;
        this.height = height;
        this.pixels = pixels;
        setPixelIndexer( PixelIndexer.getExceptionIndexer(getWidth(), getHeight()) );
    }
    

    public static int toInt(short b){
		return b & 0xFFFF;
	}
	
	public static short toShort(int b){
		return (short) (b & 0xFFFF);
	}	    
    
    /**
     * Cria uma copia da imagem original
     * @return BinaryImage - nova imagem
     */
    public GrayScaleImage duplicate(){
    	GrayScaleImage clone = new ShortImage(getWidth(), getHeight());
        System.arraycopy(this.getPixels(), 0, clone.getPixels(), 0, this.getSize());
        return clone;
    }
    
    
    /**
     * Pega o valor do pixel (x, y)
     * @param x - largura
     * @param y - altura
     * @return float - valor do pixel
     */
    public int getPixel(int x, int y){
    	return toInt(pixels[y * width + x]);
    }
    
    
    /**
     * Modifica o valor do pixel (x, y) = value
     * @param x - largura
     * @param y - altura
     * @param value - valor do pixel
     */
    public void setPixel(int x, int y, int value){
        pixels[y * width + x] = toShort(value);
    }
    
    /**
     * Pega uma matriz bidimensional de pixel da imagem
     * @return int[][]
     */
    public Object getPixels(){
        return pixels;
    }
    
    
    /**
     * Modifica a matriz de pixel da imagem para os valores da matriz dada
     * @param matrix 
     */
    public void setPixels(int width, int height, Object pixels){
    	this.width = width;
        this.height = height;
        this.pixels = (short[]) pixels;
        setPixelIndexer( PixelIndexer.getExceptionIndexer(getWidth(), getHeight()) );
    }
    
    
    /**
     * Modificao tamanho da matriz
     */
    public void resizeCenter(int width, int height){
    	int oldWidth = this.width;
    	int oldHeight = this.height;
    	this.width = width;
        this.height = height;
        this.pixels = new short[width * height];
        initImage(255);
        setPixelIndexer( PixelIndexer.getExceptionIndexer(getWidth(), getHeight()) );
        for (int i = 0, x = Math.abs(oldWidth - width)/2; i < oldWidth; i++, x++){
            for (int j = 0, y = Math.abs(oldHeight - height)/2; j < oldHeight; j++, y++){
                setPixel(x, y, toInt(pixels[j * oldWidth + i]));
            }
        }
    }
    


    public void setPixel(int i, byte level){
        pixels[i] = level;
    }
    
    public void setPixel(int i, int level){
        pixels[i] = toShort(level);
    }
    
    
    public int getPixel(int i){
    	return toInt(pixels[i]);
    }
    
    
    public int getDepth(){
    	return ImageFactory.DEPTH_16BITS;
    }

	
}

