package mmlib4j.representation.tree.pruningStrategy;

import mmlib4j.representation.tree.MorphologicalTree;
import mmlib4j.representation.tree.NodeLevelSets;
import mmlib4j.representation.tree.attribute.Attribute;


/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 */
public class PruningBasedAttribute extends FilteringBasedOnPruning{

	private double attributeValue;
	
	public PruningBasedAttribute(MorphologicalTree tree, int attributeType, double attributeValue){
		super(tree, attributeType);
		this.attributeValue = attributeValue;
		Attribute.loadAttribute(tree, attributeType);
		
	}
	
	public void setParameter(double attributeValue){
		this.attributeValue = attributeValue;
	}
	
	public boolean[] getMappingSelectedNodes() {
		super.num = 0;
		boolean selected[] = new boolean[tree.getNumNode()];
		for(NodeLevelSets node: tree.getListNodes()){
			if(node.getParent() != null && node.getAttribute(attributeType).getValue() <= attributeValue){
				if ( node.getParent().getAttribute(attributeType).getValue() != node.getAttribute(attributeType).getValue()) {
					selected[node.getId()] = true;
					super.num++;
				}
			}
		}
		return selected;
		
	}
	

	
}
