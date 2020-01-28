package mmlib4j.descriptors.profiles;

import mmlib4j.images.GrayScaleImage;
import mmlib4j.representation.tree.componentTree.ConnectedFilteringByComponentTree;

public interface FilteringStrategy {	
	public GrayScaleImage filterBy(ConnectedFilteringByComponentTree tree, double threshold, int attributeType);
}
