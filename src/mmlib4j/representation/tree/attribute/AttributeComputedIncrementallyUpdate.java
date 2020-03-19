package mmlib4j.representation.tree.attribute;

import mmlib4j.datastruct.SimpleLinkedList;
import mmlib4j.representation.tree.NodeLevelSets;

public abstract class AttributeComputedIncrementallyUpdate extends AttributeComputedIncrementally{
	
	boolean[] update; 
	boolean[] modified;

	public void computerAttribute(NodeLevelSets root){
		if(update[root.getId()]) {
			preProcessing(root);
			SimpleLinkedList<NodeLevelSets> children = root.getChildren();		
			for(NodeLevelSets son: children){			
				computerAttribute(son);
				mergeChildren(root, son);				 
			}
			posProcessing(root);
		}			
	}
	
}
