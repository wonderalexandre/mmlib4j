package mmlib4j.representation.tree.attribute;

import mmlib4j.datastruct.SimpleLinkedList;
import mmlib4j.representation.tree.NodeLevelSets;

public abstract class AttributeComputedIncrementallyUpdate extends AttributeComputedIncrementally{	
	
	public abstract void mergeChildrenUpdate(NodeLevelSets parent, NodeLevelSets son);
	
	public void computerAttribute(NodeLevelSets root, boolean[] mapCorrection){
		preProcessing(root);
		SimpleLinkedList<NodeLevelSets> children = root.getChildren();		
		for(NodeLevelSets son: children){			
			if(mapCorrection[root.getId()]) {
				computerAttribute(son, mapCorrection);
				mergeChildren(root, son);			
			} else {
				mergeChildrenUpdate(root, son);
			}	
		}
		posProcessing(root);
	}
	
}
