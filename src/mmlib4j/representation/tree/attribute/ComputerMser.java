package mmlib4j.representation.tree.attribute;

import mmlib4j.images.ColorImage;
import mmlib4j.representation.tree.INodeTree;
import mmlib4j.representation.tree.InfoPrunedTree;

public interface ComputerMser {

	public void setMaxVariation(double d);
	
	public void setMinArea(int a);
	
	public void setMaxArea(int a);
		
	public boolean[] getMappingNodesByMSER(int delta);

	public boolean[] getMappingNodesByMSER(int delta, InfoPrunedTree prunedTree);
	
	public int getNumMSER();
		
	public Attribute[] getAttributeStability();
	
	public Double[] getScoreOfBranch(INodeTree no);
	
	public ColorImage getImageMSER(int delta);

	public ColorImage getPointImageMSER(int delta);
	
}
