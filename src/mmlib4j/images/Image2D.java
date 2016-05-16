package mmlib4j.images;

import mmlib4j.images.impl.PixelIndexer;

/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 */
public interface Image2D {
    
    public int getHeight();
    
    public int getWidth();
    
    public int getSize();
    
    public boolean isPixelValid(int x, int y);

    public boolean isPixelValid(int p);

    public Iterable<Integer> scanForward();
   
    public Iterable<Integer> scanBackward();
    
    public int convertToIndex(int x, int y);
    
    public int getDepth();
   
    public PixelIndexer getPixelIndexer();
    
    public void setPixelIndexer(PixelIndexer indexer);
    
    public int getIndex(int p);
    
    public int getIndex(int x, int y);
    
    public int getChannelSize();
    
    public Image2D getChannel(int index);
}

