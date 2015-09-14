package mmlib4j.images.impl;

import mmlib4j.images.ColorImage;
import mmlib4j.images.GrayScaleImage;
import mmlib4j.segmentation.Labeling;
import mmlib4j.utils.AdjacencyRelation;



/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 * @description
 * Classe que representa uma imagem em tons de cinza. 
 * A profundidade dos pixels eh 32bits (int)  
 * 
 */ 
public class IntegerImage extends AbstractGrayScale implements GrayScaleImage{
    
	private int pixels[]; //matriz de pixel da imagem
	
	
	IntegerImage(int width, int height) {
        this.width = width;
        this.height = height;
        this.pixels = new int[width * height];
    }

	IntegerImage(int pixels[], int width, int height) {
        this.width = width;
        this.height = height;
        this.pixels = pixels;
    }
	
    
    /**
     * Pega o valor do pixel (x, y)
     * @param x - largura
     * @param y - altura
     * @return float - valor do pixel
     */
    public int getPixel(int x, int y){
    	return pixels[y * width + x];
    }
    
    
    /**
     * Modifica o valor do pixel (x, y) = value
     * @param x - largura
     * @param y - altura
     * @param value - valor do pixel
     */
    public void setPixel(int x, int y, int value){
        pixels[y * width + x] = value;
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
        this.pixels = (int[]) pixels;
    }
    

    public void setPixel(int i, byte level){
        pixels[i] = level;
    }
    
    public void setPixel(int i, int level){
        pixels[i] = level;
    }
    
    
    public int getPixel(int i){
    	return pixels[i];
    }
    
    /**
     * Modificao tamanho da matriz
     */
    public void resizeCenter(int width, int height){
    	int oldWidth = this.width;
    	int oldHeight = this.height;
    	this.width = width;
        this.height = height;
        this.pixels = new int[width * height];
        initImage(255);
        
        for (int i = 0, x = Math.abs(oldWidth - width)/2; i < oldWidth; i++, x++){
            for (int j = 0, y = Math.abs(oldHeight - height)/2; j < oldHeight; j++, y++){
                setPixel(x, y, pixels[j * oldWidth + i]);
            }
        }
    }
    
    /**
     * Cria uma copia da imagem original
     * @return BinaryImage - nova imagem
     */
    public GrayScaleImage duplicate(){
    	GrayScaleImage clone = new IntegerImage(getWidth(), getHeight());
        System.arraycopy(this.getPixels(), 0, clone.getPixels(), 0, this.getSize());
        return clone;
    }
    
    public int getDepth(){
    	return 32;
    }

    public ColorImage labeling(AdjacencyRelation adj){
    	return Labeling.labeling(this, adj).randomColor();
    }

	
	
}

