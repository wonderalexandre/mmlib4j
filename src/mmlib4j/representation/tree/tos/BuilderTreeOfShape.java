package mmlib4j.representation.tree.tos;

import mmlib4j.images.GrayScaleImage;


/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 */
public interface BuilderTreeOfShape {

	public NodeToS getRoot();
	public int getNumNode();
	public int getNumNodeIdMax();
	public BuilderTreeOfShape getClone();
	public GrayScaleImage getInputImage();
	
	public int[] getOrderedPixels();
	
	public byte[] getValuesOfOrderedPixels();
	
	public int[] getParent();
}
