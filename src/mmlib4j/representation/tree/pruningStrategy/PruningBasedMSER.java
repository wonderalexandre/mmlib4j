package mmlib4j.representation.tree.pruningStrategy;

import mmlib4j.representation.tree.InfoPrunedTree;
import mmlib4j.representation.tree.MorphologicalTree;
import mmlib4j.representation.tree.NodeLevelSets;
import mmlib4j.representation.tree.attribute.Attribute;
import mmlib4j.representation.tree.attribute.ComputerMSER;


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
	
	public Attribute[] getAttributeStability(){
		return mser.getAttributeStability();
	}
	
	public Double[] getScoreOfBranch(NodeLevelSets no){
		return mser.getScoreOfBranch(no);
	}

	
}
