package mmlib4j.representation.tree.pruningStrategy;

import mmlib4j.representation.tree.MorphologicalTree;
import mmlib4j.representation.tree.NodeLevelSets;
import mmlib4j.representation.tree.attribute.Attribute;


/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 */
public class PruningBasedTBMR extends FilteringBasedOnPruning{

	private int tMin;
	private int tMax;
	
	public PruningBasedTBMR(MorphologicalTree tree, int tmin, int tmax){
		super(tree, Attribute.AREA);
		this.tMin = tmin;
		this.tMax = tmax;
		Attribute.loadAttribute(tree, Attribute.AREA);
	}
	
	
	public boolean[] getMappingSelectedNodes() {
		boolean result[] = new boolean[tree.getNumNode()];
		super.num = 0;
		int numChildren[] = new int[tree.getNumNode()];
		for(NodeLevelSets node: tree.getListNodes()){
			if(node.getAttributeValue(Attribute.AREA) >= tMin && node.getParent() != null)
				++numChildren[node.getParent().getId()];
		}
		for(NodeLevelSets node: tree.getListNodes()){
			if(node.getParent() != null && node.getAttributeValue(Attribute.AREA) < tMax && numChildren[node.getId()] == 1 && numChildren[node.getParent().getId()] >= 2){
				result[node.getId()] = true;
				super.num += 1;
			}
		}
		return result;
		
	}

	


}
