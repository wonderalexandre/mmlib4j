package mmlib4j.representation.tree;

import mmlib4j.datastruct.SimpleLinkedList;
import mmlib4j.images.GrayScaleImage;

public interface MorphologicalTree {

	public GrayScaleImage reconstruction();
	
	public NodeLevelSets getSC(int p);
	
	public NodeLevelSets getRoot();

	public GrayScaleImage getInputImage();
	
	public int getNumNodeIdMax();
	
	public int getNumNode();
	
	public void mergeParent(NodeLevelSets node);
	
	public void prunning(NodeLevelSets node);

	public SimpleLinkedList<NodeLevelSets> getListNodes();
	
	public SimpleLinkedList<NodeLevelSets> getLeaves();
	
	public NodeLevelSets[] getNodesMap();
	
	public void extendedTree();
	
	public MorphologicalTree getClone();
}
