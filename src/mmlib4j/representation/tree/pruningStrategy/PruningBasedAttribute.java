package mmlib4j.representation.tree.pruningStrategy;

import mmlib4j.representation.tree.MorphologicalTree;
import mmlib4j.representation.tree.NodeLevelSets;
import mmlib4j.representation.tree.attribute.Attribute;


/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 */
public class PruningBasedAttribute implements MappingStrategyOfPruning{

	private MorphologicalTree tree;
	private int typeParam;
	private double valueParam;
	private int num;
	
	public PruningBasedAttribute(MorphologicalTree tree, int typeParam){
		this.tree = tree;
		this.typeParam = typeParam;
		this.valueParam = Double.MAX_VALUE;
		Attribute.loadAttribute(tree, typeParam);
		
	}
	
	public void setParameter(double v){
		this.valueParam = v;
	}
	
	public boolean[] getMappingSelectedNodes() {
		this.num = 0;
		boolean selected[] = new boolean[tree.getNumNode()];
		for(NodeLevelSets node: tree.getListNodes()){
			if(node.getParent() != null && node.getAttribute(typeParam).getValue() <= valueParam){
				if ( node.getParent().getAttribute(typeParam).getValue() != node.getAttribute(typeParam).getValue()) {
					selected[node.getId()] = true;
					num++;
				}
			}
		}
		return selected;
		
	}
	
	
	public int getNumOfPruning(){
		return num;
	}
	

	
}
