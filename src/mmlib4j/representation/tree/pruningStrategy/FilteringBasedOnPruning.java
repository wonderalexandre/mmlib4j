package mmlib4j.representation.tree.pruningStrategy;

import mmlib4j.datastruct.SimpleLinkedList;
import mmlib4j.images.GrayScaleImage;
import mmlib4j.representation.tree.InfoPrunedTree;
import mmlib4j.representation.tree.MorphologicalTree;
import mmlib4j.representation.tree.NodeLevelSets;
import mmlib4j.utils.Utils;

/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 */
public abstract class FilteringBasedOnPruning {

	protected MorphologicalTree tree;
	protected int attributeType;
	protected int num;
	
	public abstract boolean[] getMappingSelectedNodes();
	
	
	public FilteringBasedOnPruning(MorphologicalTree tree, int attributeType) {
		this.tree = tree;
		this.attributeType = attributeType;
	}
	
	public int getNumOfPruning(){
		return num;
	}
	
	public GrayScaleImage reconstruction(double attributeValue) {
		return getPrunedTree(attributeValue).reconstruction();
	}
	
	public SimpleLinkedList<NodeLevelSets> getListOfSelectedNodes(){
		boolean mapping[] = getMappingSelectedNodes();
		SimpleLinkedList<NodeLevelSets> list = new SimpleLinkedList<NodeLevelSets>(num);
		for(NodeLevelSets node: tree.getListNodes()) {
			if(mapping[node.getId()])
				list.add(node);
		}
		return list;
	}
	
	public InfoPrunedTree getPrunedTree(double attributeValue) {
		long ti = System.currentTimeMillis();
		boolean resultPruning[] = new boolean[tree.getNumNode()];
		boolean result[] = this.getMappingSelectedNodes();
		InfoPrunedTree prunedTree = new InfoPrunedTree(tree, attributeType, attributeValue);
		
		for(NodeLevelSets node: tree.getListNodes()){
			if(node.getAttributeValue(attributeType) <= attributeValue && result[node.getId()]){ //poda				
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
			System.out.println("Tempo de execucao [FilteringBasedOnPruning - filtering by pruning using gradual transition]  "+ ((tf - ti) /1000.0)  + "s");
		}
		return prunedTree;
	}

	public boolean[] getMappingSelectedNodesInPrunedTree(InfoPrunedTree prunedTree){
		boolean selectedInPrunedTree[] = new boolean[tree.getNumNode()];
		boolean selected[] = getMappingSelectedNodes();
		
		for(NodeLevelSets node: tree.getListNodes()){
			if( !prunedTree.wasPruned(node) ){
				if ( selected[node.getId()] ) {
					selectedInPrunedTree[node.getId()] = true;
				}
			}
		}	
		
		return selected;
	}
	
	//public LinkedList getListOfSelectedNodes ();
	
}
