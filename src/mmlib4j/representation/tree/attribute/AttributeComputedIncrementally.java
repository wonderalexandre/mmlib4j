package mmlib4j.representation.tree.attribute;

import java.util.List;

import mmlib4j.representation.tree.INodeTree;


/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 */
public abstract class AttributeComputedIncrementally {
	
	public abstract void preProcessing(INodeTree v);
	
	public abstract void mergeChildren(INodeTree parent, INodeTree son);
	
	public abstract void posProcessing(INodeTree parent);
	
	public void computerAttribute(INodeTree root){
		preProcessing(root);
		List<INodeTree> children = root.getChildren();
		for(INodeTree son: children){
			computerAttribute(son);
			mergeChildren(root, son);
		}
		posProcessing(root);
	}
	
}
