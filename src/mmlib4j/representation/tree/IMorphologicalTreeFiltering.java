package mmlib4j.representation.tree;

import mmlib4j.images.GrayScaleImage;


/**
 * MMorph4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 */
public interface IMorphologicalTreeFiltering {
	
	public final static int ATTRIBUTE_AREA = 0;
	public final static int ATTRIBUTE_VOLUME = 1;
	public final static int ATTRIBUTE_HEIGHT = 2;
	public final static int ATTRIBUTE_WIDTH = 3;
	public final static int ATTRIBUTE_PERIMETER = 4;
	public final static int ATTRIBUTE_X_MAX = 5;
	public final static int ATTRIBUTE_X_MIN = 6;
	public final static int ATTRIBUTE_Y_MAX = 7;
	public final static int ATTRIBUTE_Y_MIN = 8;
	public final static int ATTRIBUTE_ALTITUDE = 9;
	
	public final static int ATTRIBUTE_ECCENTRICITY = 10;
	public final static int ATTRIBUTE_MAJOR_AXES = 11;
	public final static int ATTRIBUTE_ORIENTATION = 12;
	public final static int ATTRIBUTE_CIRCULARITY = 13;
	public final static int ATTRIBUTE_RETANGULARITY = 14;
	public final static int ATTRIBUTE_ELONGATION = 15;
	
	public final static int ATTRIBUTE_NC_VARIANCIA = 0;
	public final static int ATTRIBUTE_NC_PERIMETRO = 1;
	
	public final static int PRUNING = 0;
	public final static int EXTINCTION_VALUE = 1;
	//public final static int PRUNING_MIN = 2;
	//public final static int PRUNING_MAX = 3;
	//public final static int PRUNING_VERTEBI = 4;
	public final static int PRUNING_MSER = 4;
	public final static int PRUNING_TBMR = 5;
	public final static int PRUNING_GRADUAL_TRANSITION = 6;
	
	public final static int RULE_DIRECT = 0;
	public final static int RULE_SUBTRACTIVE = 1;
	
	
	public GrayScaleImage filteringByPruning(double attributeValue, int type);
	
	public GrayScaleImage getInputImage();
	
	public INodeTree getRoot();
		
	public void setContours(boolean b);
	
	public int getNumNode();
		
}
