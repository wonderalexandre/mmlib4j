package mmlib4j.representation.tree.pruningStrategy;

import mmlib4j.gui.WindowImages;
import mmlib4j.images.GrayScaleImage;
import mmlib4j.representation.tree.InfoPrunedTree;
import mmlib4j.representation.tree.MorphologicalTree;
import mmlib4j.representation.tree.NodeLevelSets;
import mmlib4j.representation.tree.attribute.Attribute;
import mmlib4j.representation.tree.attribute.ComputerMSER;
import mmlib4j.representation.tree.componentTree.ComponentTree;
import mmlib4j.utils.AdjacencyRelation;
import mmlib4j.utils.ImageBuilder;
import mmlib4j.utils.Utils;


/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 */
public class PruningBasedMSER extends FilteringBasedOnPruning{

	
	private ComputerMSER mser;
	private int delta;
	
	
	public PruningBasedMSER(MorphologicalTree tree, int attributeType, int delta){
		super(tree, attributeType);
		this.delta = delta;
		mser = new ComputerMSER(tree, attributeType);
	}

	public void setParameters(int minArea, int maxArea, double maxVariation){
		mser.setParameters(minArea, maxArea, maxVariation, attributeType);
	}
	

	public boolean[] getMappingSelectedNodesRank() {
		super.num = 0;
		int rank[] = new int[tree.getNumNode()];
		boolean rankSelected[] = new boolean[tree.getNumNode()];
		for(int i=1; i < 50; i++){
			boolean result[] = mser.computerMSER(i);
			for(int k=0; k < result.length; k++){
				rank[k] += result[k]? 1:0;
			}
		}
		for(int k=0; k < rank.length; k++){
			if(rank[k] > delta){
				rankSelected[k] = true;
				this.num++;
			}
		}
			
		return rankSelected;
		
	}
	
	public boolean[] getMappingSelectedNodes() {
		boolean result[] = mser.computerMSER(delta);
		super.num = mser.getNumNodes();
		return result;
	}
	

	
	public boolean[] getMappingSelectedNodesInPrunedTree(int localDelta, InfoPrunedTree prunedTree){
		boolean selectedInPrunedTree[] = new boolean[tree.getNumNode()];
		boolean selected[] = mser.computerMSER(localDelta);
		
		for(NodeLevelSets node: tree.getListNodes()){
			if( !prunedTree.wasPruned(node) ){
				if ( selected[node.getId()] ) {
					selectedInPrunedTree[node.getId()] = true;
				}
			}
		}	
		
		return selected;
	}
	
	public InfoPrunedTree getPrunedTreeByAdaptativeThreshold(double attributeValue) {
		boolean selectedPruned[] = new boolean[tree.getNumNode()]; //nodes pruned
		for(NodeLevelSets node: tree.getListNodes()){
			if(node.getParent() != null && node.getAttribute(attributeType).getValue() <= attributeValue){
				if ( node.getParent().getAttribute(attributeType).getValue() != node.getAttribute(attributeType).getValue()) {
					selectedPruned[node.getId()] = true;
				}
			}
		}
		

		mser.computerMSER(delta);
		Attribute stability[] = mser.getAttributeStability();
		boolean result[] = new boolean[tree.getNumNode()];
		for(NodeLevelSets node: tree.getListNodes()){
			
			if(selectedPruned[node.getId()] && stability[node.getId()] != null){ //node pruned
				double min = stability[node.getId()].getValue();
				NodeLevelSets nodeStabilityMin = node; 
				for(NodeLevelSets nodeDesc: node.getNodesDescendants()) {
					if(stability[nodeDesc.getId()] != null &&  Math.abs( nodeDesc.getLevel() - node.getLevel() ) < 10 && stability[nodeDesc.getId()].getValue() < min) {
						min = stability[nodeDesc.getId()].getValue();
						nodeStabilityMin = nodeDesc;
					}
				}
				result[nodeStabilityMin.getId()] = true;
			}
		}
		
		
		
		long ti = System.currentTimeMillis();
		boolean resultPruning[] = new boolean[tree.getNumNode()];
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
	
	
	public Attribute[] getAttributeStability(){
		return mser.getAttributeStability();
	}
	
	public Double[] getScoreOfBranch(NodeLevelSets no){
		return mser.getScoreOfBranch(no);
	}

	public static void main(String args[]) {
		GrayScaleImage img = ImageBuilder.openGrayImage();
		MorphologicalTree tree = new ComponentTree(img, AdjacencyRelation.getAdjacency8(), true);
		
		double lambdas[] = {100, 500, 1000, 5000};
		GrayScaleImage imgArea[] = new GrayScaleImage[lambdas.length];
		GrayScaleImage imgAreaMSER[] = new GrayScaleImage[lambdas.length];
		GrayScaleImage imgAreaMSERAdp[] = new GrayScaleImage[lambdas.length];
		int i = 0;		
		int delta = 5;
		for(double attributeValue: lambdas) {
			imgArea[i] = new PruningBasedAttribute(tree, Attribute.AREA, attributeValue).getPrunedTree(attributeValue).reconstruction();
			imgAreaMSER[i] = new PruningBasedMSER(tree, Attribute.AREA, delta).getPrunedTree(attributeValue).reconstruction();
			imgAreaMSERAdp[i] = new PruningBasedMSER(tree, Attribute.AREA, delta).getPrunedTreeByAdaptativeThreshold(attributeValue).reconstruction();
			i += 1;
		}
		
		
		WindowImages.show(imgArea);
		WindowImages.show(imgAreaMSER);
		WindowImages.show(imgAreaMSERAdp);
		
		
		
	}
	
	
}
