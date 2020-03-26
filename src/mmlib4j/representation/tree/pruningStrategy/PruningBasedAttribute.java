package mmlib4j.representation.tree.pruningStrategy;

import mmlib4j.representation.tree.MorphologicalTree;
import mmlib4j.representation.tree.MorphologicalTreeFiltering;
import mmlib4j.representation.tree.NodeLevelSets;
import mmlib4j.representation.tree.componentTree.ComponentTree;
import mmlib4j.representation.tree.componentTree.NodeCT;
import mmlib4j.representation.tree.filtering.AttributeFilters;
import mmlib4j.representation.tree.tos.NodeToS;
import mmlib4j.representation.tree.tos.TreeOfShape;


/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 */
public class PruningBasedAttribute implements MappingStrategyOfPruning{

	private MorphologicalTreeFiltering tree;
	private int typeParam;
	private double valueParam;
	private int num;
	
	public PruningBasedAttribute(MorphologicalTreeFiltering tree, int typeParam){
		this.tree = tree;
		this.typeParam = typeParam;
		this.valueParam = Double.MAX_VALUE;
		tree.loadAttribute(typeParam);
		
	}
	
	public void setParameter(double v){
		this.valueParam = v;
	}
	
	public boolean[] getMappingSelectedNodes() {
		this.num = 0;
		if(tree instanceof ComponentTree){
			ComponentTree tree = (ComponentTree) this.tree;
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
		else if(tree instanceof TreeOfShape){
			TreeOfShape tree = (TreeOfShape) this.tree;
			boolean selected[] = new boolean[tree.getNumNode()];
			for(NodeLevelSets node: tree.getListNodes()){
				if(node.getParent() != null && node.getAttribute(typeParam).getValue() <= valueParam){
					if ( node.getParent().getAttributeValue(typeParam) != node.getAttributeValue(typeParam)) {
						selected[node.getId()] = true;
						num++;
					}
				}
			}
			return selected;
		}
		else
			return null;
	}
	
	
	public int getNumOfPruning(){
		return num;
	}
	

	
}
