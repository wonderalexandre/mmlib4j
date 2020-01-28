package mmlib4j.descriptors.profiles;

import mmlib4j.images.GrayScaleImage;
import mmlib4j.representation.tree.componentTree.ConnectedFilteringByComponentTree;

public class DirectRule implements FilteringStrategy{
	
	public static final DirectRule instance = new DirectRule();
	
	@Override
	public GrayScaleImage filterBy(ConnectedFilteringByComponentTree tree, double threshold, int attributeType) {
		return tree.simplificationTreeByDirectRuleMtree(threshold, attributeType);
	}			

}
