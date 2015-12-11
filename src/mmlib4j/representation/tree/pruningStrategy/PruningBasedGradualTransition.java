package mmlib4j.representation.tree.pruningStrategy;

import java.util.LinkedList;

import mmlib4j.representation.tree.InfoPrunedTree;
import mmlib4j.representation.tree.MorphologicalTreeFiltering;
import mmlib4j.representation.tree.NodeLevelSets;
import mmlib4j.representation.tree.componentTree.ComponentTree;
import mmlib4j.representation.tree.componentTree.NodeCT;
import mmlib4j.representation.tree.tos.NodeToS;
import mmlib4j.representation.tree.tos.TreeOfShape;


/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 */
public class PruningBasedGradualTransition implements MappingStrategyOfPruning{
	
	private MorphologicalTreeFiltering inputTree;
	private int typeParam;
	private int delta;
	private int num;
	
	public PruningBasedGradualTransition(MorphologicalTreeFiltering tree, int typeParam, int delta){
		this.inputTree = tree;
		this.typeParam = typeParam;
		this.delta = delta;
		tree.loadAttribute(typeParam);
	}
	
	
	public boolean[] getMappingSelectedNodes(){
		this.num = 0;
		boolean selected[] = null;
		if(inputTree instanceof ComponentTree){
			ComponentTree tree = (ComponentTree) inputTree;
			selected = new boolean[tree.getNumNode()];
			for(NodeCT node: tree.getListNodes()){
				if(node.getParent() != null){
					if ( node.getParent().getAttribute(typeParam).getValue() - node.getAttribute(typeParam).getValue()  > delta) {
						selected[node.getId()] = true;
						this.num = this.num + 1;
					}
				}
			}
		}else{
			TreeOfShape tree = (TreeOfShape) inputTree;
			selected = new boolean[tree.getNumNode()];
			for(NodeToS node: tree.getListNodes()){
				if(node.getParent() != null){
					if ( node.getParent().getAttributeValue(typeParam) - node.getAttributeValue(typeParam)  > delta) {
						selected[node.getId()] = true;
						this.num = this.num + 1;
					}
				}
			}
		}
		return selected;
	}
	
	public boolean[] getMappingSelectedNodes(InfoPrunedTree prunedTree){
		boolean selected[] = null;
		this.num = 0;
		if(inputTree instanceof ComponentTree){
			ComponentTree tree = (ComponentTree) inputTree;
			selected = new boolean[tree.getNumNode()];
			for(NodeCT node: tree.getListNodes()){
				if(node.getParent() != null && !prunedTree.wasPruned(node)){
					if ( node.getParent().getAttribute(typeParam).getValue() - node.getAttribute(typeParam).getValue()  > delta) {
						selected[node.getId()] = true;
						this.num = this.num + 1;
					}
				}
			}	
		}else{
			TreeOfShape tree = (TreeOfShape) inputTree;
			selected = new boolean[tree.getNumNode()];
			for(NodeToS node: tree.getListNodes()){
				if(node.getParent() != null && !prunedTree.wasPruned(node)){
					if ( node.getParent().getAttributeValue(typeParam) - node.getAttributeValue(typeParam)  > delta) {
						selected[node.getId()] = true;
						this.num = this.num + 1;
					}
				}
			}
		}
		
		return selected;
	}

	public int getNumOfPruning(){
		return num;
	}
	
	public LinkedList<NodeLevelSets> getListOfSelectedNodes( ){
		LinkedList<NodeLevelSets> list = new LinkedList<NodeLevelSets>();
		if(inputTree instanceof ComponentTree){
			ComponentTree tree = (ComponentTree) inputTree;
			for(NodeCT node: tree.getListNodes()){
				if(node.getParent() != null){
					if ( node.getParent().getAttribute(typeParam).getValue() - node.getAttribute(typeParam).getValue()  > delta) {
						list.add(node.getParent());
					}
				}
			}
		}else{
			TreeOfShape tree = (TreeOfShape) inputTree;
			for(NodeToS node: tree.getListNodes()){
				if(node.getParent() != null){
					if ( node.getParent().getAttributeValue(typeParam) - node.getAttributeValue(typeParam)  > delta) {
						list.add(node.getParent());
					}
				}
			}
		}
		this.num = list.size();
		return list;
	}
}
