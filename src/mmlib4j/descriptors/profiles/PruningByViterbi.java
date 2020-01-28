package mmlib4j.descriptors.profiles;

import mmlib4j.images.GrayScaleImage;
import mmlib4j.representation.tree.componentTree.ConnectedFilteringByComponentTree;

public class PruningByViterbi implements FilteringStrategy{
	
	public static final PruningByViterbi instance = new PruningByViterbi();
	
	@Override
	public GrayScaleImage filterBy(ConnectedFilteringByComponentTree tree, double threshold, int attributeType) {
		return tree.filteringByPruningViterbi(threshold, attributeType);
	}			

}
