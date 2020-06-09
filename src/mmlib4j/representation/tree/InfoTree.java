package mmlib4j.representation.tree;

import mmlib4j.images.GrayScaleImage;

public interface InfoTree {

	public MorphologicalTree getInputTree();
	
	public int getNumLeaves();
	
	public int getNumNode();
	
	public GrayScaleImage reconstruction();
	
}
