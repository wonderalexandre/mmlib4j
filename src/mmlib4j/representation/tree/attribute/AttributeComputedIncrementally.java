package mmlib4j.representation.tree.attribute;


import mmlib4j.datastruct.SimpleLinkedList;
import mmlib4j.representation.tree.NodeLevelSets;


/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 */
public abstract class AttributeComputedIncrementally {
	
	public abstract void preProcessing(NodeLevelSets v);
	
	public abstract void mergeChildren(NodeLevelSets parent, NodeLevelSets son);
	
	public abstract void posProcessing(NodeLevelSets parent);
	
	public void computerAttribute(NodeLevelSets root){
		preProcessing(root);
		SimpleLinkedList<NodeLevelSets> children = root.getChildren();
		for(NodeLevelSets son: children){
			computerAttribute(son);
			mergeChildren(root, son);
		}
		posProcessing(root);
	}
	
}
