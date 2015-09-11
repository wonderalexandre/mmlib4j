package mmlib4j.representation.tree.attribute;

import mmlib4j.images.ColorImage;
import mmlib4j.representation.tree.InfoPrunedTree;
import mmlib4j.representation.tree.NodeLevelSets;


/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 */
public interface ComputerMser {

	public void setMaxVariation(double d);
	
	public void setMinArea(int a);
	
	public void setMaxArea(int a);
	
	public void setAttribute(int t);
		
	public boolean[] getMappingNodesByMSER(int delta);

	public boolean[] getMappingNodesByMSER(int delta, InfoPrunedTree prunedTree);
	
	public int getNumMSER();
		
	public Attribute[] getAttributeStability();
	
	public Double[] getScoreOfBranch(NodeLevelSets no);
	
	public ColorImage getImageMSER(int delta);

	public ColorImage getPointImageMSER(int delta);
	
	public void setEstimateDelta(boolean b);
}
