package mmlib4j.representation.tree.pruningStrategy;


import mmlib4j.datastruct.SimpleLinkedList;
import mmlib4j.representation.tree.InfoPrunedTree;
import mmlib4j.representation.tree.MorphologicalTree;
import mmlib4j.representation.tree.NodeLevelSets;
import mmlib4j.representation.tree.attribute.Attribute;


/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 */
public class PruningBasedGradualTransition extends FilteringBasedOnPruning{
	
	private int delta;
	
	
	public PruningBasedGradualTransition(MorphologicalTree tree, int attributeType, int delta){
		super(tree, attributeType);
		this.delta = delta;
		Attribute.loadAttribute(tree, attributeType);
	}

	
	public boolean[] getMappingSelectedNodes(){
		super.num = 0;
		boolean selected[] = null;
		selected = new boolean[tree.getNumNode()];
		for(NodeLevelSets node: tree.getListNodes()){
			if(node.getParent() != null){
				if ( node.getParent().getAttribute(attributeType).getValue() - node.getAttribute(attributeType).getValue()  > delta) {
					selected[node.getId()] = true;
					super.num = super.num + 1;
				}
			}
		
		}
		return selected;
	}
	

	
	/*
	public InfoPrunedTree getPrunedTree(double attributeValue){
		long ti = System.currentTimeMillis();
		
		InfoPrunedTree prunedTree = new InfoPrunedTree(tree, typeParam, attributeValue);
		
		boolean resultPruning[] = this.getMappingSelectedNodes( );
		SimpleLinkedList<NodeLevelSets> list = this.getListOfSelectedNodes( );
		
		for(NodeLevelSets node: list){
			if(node.getAttributeValue(typeParam) <= attributeValue){ //poda				
				for(NodeLevelSets song: node.getChildren()){
					for(NodeLevelSets n: song.getNodesDescendants())
						resultPruning[n.getId()] = true;	 
				}
			}
		}
		
		for(NodeLevelSets no: tree.getListNodes()){
			if( ! resultPruning[no.getId()]  ){ //nao poda
				prunedTree.addNodeNotPruned(no);
			}
		}
		
		
		if(Utils.debug){
			long tf = System.currentTimeMillis();
			System.out.println("Tempo de execucao [PruningBasedGradualTransition - filtering by pruning using gradual transition]  "+ ((tf - ti) /1000.0)  + "s");
		}
		
		
		return prunedTree;
	}*/
	
}
