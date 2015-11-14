package mmlib4j.images.impl;

import java.util.Iterator;

import mmlib4j.images.Image2D;

/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 */
public abstract class AbstractImage2D implements Image2D{

	protected int width; //largura da imagem
	protected int height; //altura da imagem
    protected PixelIndexer pixelIndexer;

    public boolean isPixelValid(int x, int y){
        return (x >= 0 && x < this.getWidth() && y >= 0 && y < this.getHeight());
    }

    public boolean isPixelValid(int p){
    	return isPixelValid(p % this.getWidth(), p / this.getWidth());
    }
    
    public PixelIndexer getPixelIndexer(){
    	return pixelIndexer;
    }
    
    public void setPixelIndexer(PixelIndexer indexer){
    	pixelIndexer = indexer;
    }
        
    /**
     * Retorna o tamanho da imagem
     * @return
     */
    public int getSize(){
        return getHeight() * getWidth();
    }
    
    /**
     * Retorna a altura da imagem
     * @return int - altura
     */
    public int getHeight(){
        return height;
    }
    
    /**
     * Retorna a largura da imagem
     * @return int - largura
     */
    public int getWidth(){
        return width;
    }
    
    public int convertToIndex(int x, int y){
    	return y * getWidth() + x;
    	
    }
    
    public int getIndex(int p){
    	return pixelIndexer.getIndex(p);
    }
    
    public int getIndex(int x, int y){
    	return pixelIndexer.getIndex(x, y);
    }

    public Iterable<Integer> scanForward(){
    	final int size = getSize();
    	return new Iterable<Integer>() {
			public Iterator<Integer> iterator() {
				return new Iterator<Integer>() {
					int p=0;
					public boolean hasNext() {
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
    
    public Iterable<Integer> scanBackward(){
    	final int size = getSize();
    	return new Iterable<Integer>() {
			public Iterator<Integer> iterator() {
				return new Iterator<Integer>() {
					int p = size-1;
					public boolean hasNext() {
						return p >= 0;
					}
					public Integer next() {
						return p--;
					}
					public void remove() { }
					
				};
			}
		};
    }
	
}
