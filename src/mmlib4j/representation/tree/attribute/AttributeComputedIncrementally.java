package mmlib4j.representation.tree.attribute;

import java.util.List;

import mmlib4j.representation.tree.INodeTree;

public abstract class AttributeComputedIncrementally {
	
	public abstract void initialization(INodeTree v);
	
	public abstract void updateChildren(INodeTree parent, INodeTree son);
	
	public abstract void posProcessing(INodeTree parent);
	
	public void computerAttribute(INodeTree root){
		initialization(root);
		List<INodeTree> children = root.getChildren();
		for(INodeTree son: children){
			computerAttribute(son);
			updateChildren(root, son);
		}
		posProcessing(root);
	}
}
