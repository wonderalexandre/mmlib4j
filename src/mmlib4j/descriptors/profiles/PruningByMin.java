package mmlib4j.descriptors.profiles;

import mmlib4j.images.GrayScaleImage;
import mmlib4j.representation.tree.componentTree.ConnectedFilteringByComponentTree;

public class PruningByMin implements FilteringStrategy{
	
	public static final PruningByMin instance = new PruningByMin();
	
	@Override
	public GrayScaleImage filterBy(ConnectedFilteringByComponentTree tree, double threshold, int attributeType) {
		return tree.filteringByPruningMin(threshold, attributeType);
	}			

}
