package mmlib4j.images;


/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 */
public interface RealImage extends Image2D{
    
    public void initImage(float color);
    
    public RealImage duplicate();
    
    public float getPixel(int x, int y);
    
    public void setPixel(int x, int y, float value);
    
    public Object getPixels();
    
    public void setPixels(int width, int height, Object pixels);
    
    public float getPixelValueMax();
    
    public float getPixelValueMin();

    public void setPixel(int i, float level);
    
    public float getPixel(int i);
        
    public float getValue(int x, int y);
	
	public float getValue(int p);
  
}
