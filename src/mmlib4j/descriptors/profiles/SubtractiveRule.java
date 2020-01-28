package mmlib4j.descriptors.profiles;

import mmlib4j.images.GrayScaleImage;
import mmlib4j.representation.tree.componentTree.ConnectedFilteringByComponentTree;

public class SubtractiveRule implements FilteringStrategy{
	
	public static final SubtractiveRule instance = new SubtractiveRule();
	
	@Override
	public GrayScaleImage filterBy(ConnectedFilteringByComponentTree tree, double threshold, int attributeType) {
		return tree.simplificationTreeBySubstractiveRuleMtree(threshold, attributeType);
	}			

}
