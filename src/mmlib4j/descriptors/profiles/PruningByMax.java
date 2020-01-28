package mmlib4j.descriptors.profiles;

import mmlib4j.images.GrayScaleImage;
import mmlib4j.representation.tree.componentTree.ConnectedFilteringByComponentTree;

public class PruningByMax implements FilteringStrategy{
	
	public static final PruningByMax instance = new PruningByMax();
	
	@Override
	public GrayScaleImage filterBy(ConnectedFilteringByComponentTree tree, double threshold, int attributeType) {
		return tree.filteringByPruningMax(threshold, attributeType);
	}			

}
