package mmlib4j.images;

/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 */
public interface BinaryImage extends Image2D{
	
    public void initImage(boolean color);
    
    public BinaryImage duplicate();
    
    public boolean getPixel(int x, int y);
   
    public void setPixel(int x, int y, boolean value);
    
    public boolean[] getPixels();
    
    public void setPixels(int width, int height, boolean pixels[]);
    
    public BinaryImage invert();
    
    public GrayScaleImage convertGrayScale();
    
    public Iterable<Integer> scanForwardObjectPixel();
    
    public int getArea();
    
    public BinaryImage rot90();

    public BinaryImage rot180();

    public BinaryImage rot270();
    
    public int[] getHistogramXprojection();
    
    public int[] getHistogramYprojection();
    
    public void setPixel(int i, boolean level); 
    
    public boolean getPixel(int i);
    
    public int getSize();
    
    public boolean isPixelBackground(int i);
    
    public boolean isPixelForeground(int i);
    
    public boolean isPixelBackground(int x, int y);
    
    public boolean isPixelForeground(int x, int y);
    
    public void drawLine(int x1, int y1, int x2, int y2);

}
    