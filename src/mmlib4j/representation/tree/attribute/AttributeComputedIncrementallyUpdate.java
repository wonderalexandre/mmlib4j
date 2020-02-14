package mmlib4j.representation.tree.attribute;

import mmlib4j.datastruct.SimpleLinkedList;
import mmlib4j.representation.tree.NodeLevelSets;

public abstract class AttributeComputedIncrementallyUpdate extends AttributeComputedIncrementally{		

	public void computerAttribute(NodeLevelSets root, boolean[] mapCorrection){
		if(mapCorrection[root.getId()]) {
			preProcessing(root);
			SimpleLinkedList<NodeLevelSets> children = root.getChildren();		
			for(NodeLevelSets son: children){			
				computerAttribute(son, mapCorrection);
				mergeChildren(root, son);				 
			}
			posProcessing(root);
		}			
	}
	
}
