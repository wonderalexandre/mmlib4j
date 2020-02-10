package mmlib4j.representation.tree.attribute.mergetree;

import mmlib4j.datastruct.SimpleLinkedList;
import mmlib4j.representation.tree.InfoMergedTree.NodeMergedTree;

public abstract class AttributeUpdatedIncrementally {
	
	boolean[] mapCorrection;
	
	public int numberOfCalls = 0;
	
	abstract void preProcessing(NodeMergedTree root_);
	
	abstract void mergeChildren(NodeMergedTree root_, NodeMergedTree son_);
	
	abstract void mergeChildrenUpdate(NodeMergedTree root_, NodeMergedTree son_);
	
	abstract void posProcessing(NodeMergedTree root_);
	
	public void computerAttribute(NodeMergedTree root_){
		numberOfCalls++;
		preProcessing(root_);
		SimpleLinkedList<NodeMergedTree> children = root_.getChildren();				
		for(NodeMergedTree son_: children){	
			if(mapCorrection[son_.getId()]) {
				computerAttribute(son_);
				mergeChildren(root_, son_);			
			} else {
				mergeChildrenUpdate(root_, son_);
			}	
		}
		posProcessing(root_);
	}
	
}
