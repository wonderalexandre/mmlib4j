package mmlib4j.representation.tree.pruningStrategy;

import mmlib4j.representation.tree.MorphologicalTreeFiltering;
import mmlib4j.representation.tree.componentTree.ComponentTree;
import mmlib4j.representation.tree.componentTree.NodeCT;
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
	private int num;
	
	public PruningBasedAttribute(MorphologicalTreeFiltering tree, int typeParam){
		this.tree = tree;
		this.typeParam = typeParam;
		tree.loadAttribute(typeParam);
		
	}
	
	public boolean[] getMappingSelectedNodes() {
		this.num = 0;
		if(tree instanceof ComponentTree){
			ComponentTree tree = (ComponentTree) this.tree;
			boolean selected[] = new boolean[tree.getNumNode()];
			for(NodeCT node: tree.getListNodes()){
				if(node.getParent() != null){
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
			for(NodeToS node: tree.getListNodes()){
				if(node.getParent() != null){
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
