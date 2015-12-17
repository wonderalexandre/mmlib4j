package mmlib4j.representation.tree;

import mmlib4j.images.GrayScaleImage;


/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 */
public interface MorphologicalTreeFiltering {
	
	public final static int PRUNING = 0;
	public final static int PRUNING_EXTINCTION_VALUE = 1;
	//public final static int PRUNING_MIN = 2;
	//public final static int PRUNING_MAX = 3;
	//public final static int PRUNING_VERTEBI = 4;
	public final static int PRUNING_MSER = 4;
	public final static int PRUNING_TBMR = 5;
	public final static int PRUNING_GRADUAL_TRANSITION = 6;
	
	public final static int RULE_DIRECT = 0;
	//public final static int RULE_SUBTRACTIVE = 1;
	
	
	public GrayScaleImage filteringByPruning(double attributeValue, int type);
	
	public GrayScaleImage getInputImage();
	
	public NodeLevelSets getRoot();
		
	public int getNumNode();
	
	public void loadAttribute(int attr);
		
}
