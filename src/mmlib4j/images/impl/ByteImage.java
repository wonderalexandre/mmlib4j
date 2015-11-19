package mmlib4j.images.impl;

import mmlib4j.images.GrayScaleImage;


/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 * @description
 * Classe que representa uma imagem em tons de cinza. 
 * A profundidade dos pixels eh 32bits (int)  
 * 
 */ 
public class ByteImage extends AbstractGrayScale implements GrayScaleImage{
    
	private byte pixels[]; //matriz de pixel da imagem
	private Statistics stats = null;
	
	ByteImage(int width, int height) {
        this.width = width;
        this.height = height;
        this.pixels = new byte[width * height];
        setPixelIndexer( PixelIndexer.getExceptionIndexer(getWidth(), getWidth()) );
    }

	ByteImage(byte pixels[], int width, int height) {
        this.width = width;
        this.height = height;
        this.pixels = pixels;
        setPixelIndexer( PixelIndexer.getExceptionIndexer(getWidth(), getWidth()) );
    }
    

    public static int toInt(byte b){
		return b & 0xFF;
	}
	
	public static byte toByte(int b){
		return (byte) (b & 0xFF);
	}
	
    
    
    public void loadStatistics(){
    	stats = new Statistics();
    }
    
	
    
    /**
     * Cria uma copia da imagem original
     * @return BinaryImage - nova imagem
     */
    public GrayScaleImage duplicate(){
    	GrayScaleImage clone = new ByteImage(getWidth(), getHeight());
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
        pixels[y * width + x] = toByte(value);
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
        this.pixels = (byte[]) pixels;
        setPixelIndexer( PixelIndexer.getExceptionIndexer(getWidth(), getWidth()) );
    }
    
    
    /**
     * Modificao tamanho da matriz
     */
    public void resizeCenter(int width, int height){
    	int oldWidth = this.width;
    	int oldHeight = this.height;
    	this.width = width;
        this.height = height;
        this.pixels = new byte[width * height];
        setPixelIndexer( PixelIndexer.getExceptionIndexer(getWidth(), getWidth()) );
        initImage(255);
        
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
        pixels[i] = toByte(level);
    }
    
    
    public int getPixel(int i){
    	return toInt(pixels[i]);
    }
    
    
    public int getDepth(){
    	return 8;
    }

	
}

