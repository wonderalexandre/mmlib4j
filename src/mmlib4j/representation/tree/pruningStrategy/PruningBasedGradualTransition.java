package mmlib4j.representation.tree.pruningStrategy;


import mmlib4j.datastruct.SimpleLinkedList;
import mmlib4j.representation.tree.InfoPrunedTree;
import mmlib4j.representation.tree.MorphologicalTree;
import mmlib4j.representation.tree.NodeLevelSets;
import mmlib4j.representation.tree.attribute.Attribute;
import mmlib4j.representation.tree.componentTree.ComponentTree;
import mmlib4j.representation.tree.tos.TreeOfShape;


/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 */
public class PruningBasedGradualTransition implements MappingStrategyOfPruning{
	
	private MorphologicalTree inputTree;
	private int typeParam;
	private int delta;
	private int num;
	
	public PruningBasedGradualTransition(MorphologicalTree tree, int typeParam, int delta){
		this.inputTree = tree;
		this.typeParam = typeParam;
		this.delta = delta;
		Attribute.loadAttribute(tree, typeParam);
	}
	
	
	public boolean[] getMappingSelectedNodes(){
		this.num = 0;
		boolean selected[] = null;
		if(inputTree instanceof ComponentTree){
			ComponentTree tree = (ComponentTree) inputTree;
			selected = new boolean[tree.getNumNode()];
			for(NodeLevelSets node: tree.getListNodes()){
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
			for(NodeLevelSets node: tree.getListNodes()){
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
			for(NodeLevelSets node: tree.getListNodes()){
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
			for(NodeLevelSets node: tree.getListNodes()){
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
	
	public SimpleLinkedList<NodeLevelSets> getListOfSelectedNodes( ){
		SimpleLinkedList<NodeLevelSets> list = new SimpleLinkedList<NodeLevelSets>();
		if(inputTree instanceof ComponentTree){
			ComponentTree tree = (ComponentTree) inputTree;
			for(NodeLevelSets node: tree.getListNodes()){
				if(node.getParent() != null){
					if ( node.getParent().getAttribute(typeParam).getValue() - node.getAttribute(typeParam).getValue()  > delta) {
						list.add(node.getParent());
					}
				}
			}
		}else{
			TreeOfShape tree = (TreeOfShape) inputTree;
			for(NodeLevelSets node: tree.getListNodes()){
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
