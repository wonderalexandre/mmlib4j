package mmlib4j.representation.tree;

import mmlib4j.images.GrayScaleImage;


/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 */
public interface MorphologicalTreeFiltering {
	
	//public final static int PRUNING = 0;
	public final static int PRUNING_MIN = 1;
	public final static int PRUNING_MAX = 2;
	public final static int PRUNING_VITERBI = 3;
	public final static int RULE_DIRECT = 4;
	public final static int RULE_SUBTRACTIVE = 5;
	
	/*public final static int PRUNING_EXTINCTION_VALUE = 1;
	public final static int PRUNING_MSER = 5;
	public final static int PRUNING_TBMR = 6;
	public final static int PRUNING_GRADUAL_TRANSITION = 7;
	*/
	
	public GrayScaleImage getImageFiltered(double attributeValue, int attributeType, int typeSimplification);
	
	public InfoPrunedTree getInfoPrunedTree(double attributeValue, int attributeType, int typeSimplification);
	
	public void simplificationTree(double attributeValue, int attributeType, int typeSimplification);
	
	//public GrayScaleImage filteringByPruning(double attributeValue, int type);
	
	public GrayScaleImage getInputImage();
	
	public NodeLevelSets getRoot();
		
	public int getNumNode();
	
	public void loadAttribute(int attr);
		
}
