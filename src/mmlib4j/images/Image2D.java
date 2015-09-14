package mmlib4j.images;


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
   
}

