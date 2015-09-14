package mmlib4j.images;


/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 */
public interface ColorImage extends Image2D{
    
    public void initImage(int rgb);
    
    public ColorImage duplicate();
    
    public int getPixel(int x, int y);
    
    public int getPixel(int i);
    
    public int[] getPixels();
    
    public int getSize();
    
    public void setPixel(int x, int y, int value[]);
    
    public void setPixel(int x, int y, int rgb);
    
    public void setPixel(int i, int value[]);

    public void setPixel(int i, int rgb);
    
    public void setPixels(int width, int height, int pixels[]);
        
    public GrayScaleImage convertGrayScaleImage();
    
    public GrayScaleImage getRed();
    public GrayScaleImage getBlue();
    public GrayScaleImage getGreen();

    public void paintBoundBox(int x1, int y1, int x2, int y2, int rgb);
    
    public void addSubImage(ColorImage img, int x, int y);
    
    public void addSubImage(GrayScaleImage img, int x, int y);
    
    public void setAlpha(int value);
    
    public int getRed(int i);
    public int getGreen(int i);
    public int getBlue(int i);
    
    public int getRed(int x, int y);
    public int getGreen(int x, int y);
    public int getBlue(int x, int y);
    
    public void setRed(int x, int y, int value);
    public void setGreen(int x, int y, int value);
    public void setBlue(int x, int y, int value);
    
    public void setRed(int i, int value);
    public void setGreen(int i, int value);
    public void setBlue(int i, int value);
    
    public void setGray(int x, int y, int value);
    public void setGray(int i, int value);
    public int getGray(int i);
    public int getGray(int x, int y);
    
}
    