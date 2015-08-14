package mmlib4j.images;

import java.util.LinkedList;

import mmlib4j.utils.AdjacencyRelation;


/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 */
public interface GrayScaleImage extends Image2D{
    
    public void initImage(int color);
    
    public int getPixel(int x, int y);
    
    public int getPixel(int i);
    
    public int getValue(int x, int y);
    
    public int getValue(int i);
    
    public void setPixel(int x, int y, int value);
    
    public void setPixel(int i, int level);
    
    public void setPadding(int value);
    
    public void setPixels(int width, int height, Object pixels);
   
    public Object getPixels();
    
    public int countPixels(int color);
    
    public int getSize();
    
    public int numValues();
    
    public int maxValue();
    
    public int minValue();
    
    public int meanValue();
    
    public int maxPixel();
    
    public int minPixel();
    
    public void invert();
    
    public GrayScaleImage getInvert();

    public void add(int a);
    
    public void multiply(double a);
    
    public int[] getHistogram();
    
    public LinkedList<Integer>[] getPixelsOfHistogram();
    
    public void resizeCenter(int x, int y);
    
    public void paintBoundBox(int x1, int y1, int x2, int y2, int color);
    
    public void replaceValue(int colorOld, int colorNew);
    
    public ColorImage randomColor();
    
    public ColorImage randomColor(int alpha);
        
    public GrayScaleImage duplicate();
    
    public ColorImage labeling(AdjacencyRelation adj);
	
    public void drawLine(int x1, int y1, int x2, int y2, int grayLine);
}
    