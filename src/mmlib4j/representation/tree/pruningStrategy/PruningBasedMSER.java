package mmlib4j.representation.tree.pruningStrategy;

import mmlib4j.representation.tree.MorphologicalTreeFiltering;
import mmlib4j.representation.tree.attribute.Attribute;
import mmlib4j.representation.tree.attribute.ComputerMserComponentTree;
import mmlib4j.representation.tree.attribute.ComputerMserTreeOfShapes;
import mmlib4j.representation.tree.componentTree.ComponentTree;
import mmlib4j.representation.tree.tos.TreeOfShape;


/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 */
public class PruningBasedMSER implements MappingStrategyOfPruning{

	protected MorphologicalTreeFiltering tree;
	protected int num;
	protected Double q[];
	
	protected int delta;
	private double maxVariation = Double.MAX_VALUE;
	private int minArea=0;
	private int maxArea=Integer.MAX_VALUE;
	private int attribute;
	private boolean estimateDelta = false;
	
	public PruningBasedMSER(){
		tree.loadAttribute(Attribute.AREA);
	}
	
	public void setMaxVariation(double d){
		maxVariation = d;
	}
	
	public void setMinArea(int a){
		minArea = a;
	}
	
	public void setMaxArea(int a){
		maxArea = a;
	}
	
	public void setEstimateDelta(boolean b){
		estimateDelta = b;
	}
	
	public void setAttribute(int t){
		attribute = t;
	}

	public void setParameters(int minArea, int maxArea, double maxVariation, int attribute){
		this.minArea = minArea;
		this.maxArea = maxArea;
		this.maxVariation = maxVariation;
		this.attribute = attribute;
	}
	
	public PruningBasedMSER(MorphologicalTreeFiltering tree, int delta){
		this.tree = tree;
		this.delta = delta;
	}

	public boolean[] getMappingSelectedNodesRank() {
		if(tree instanceof ComponentTree){
			this.num = 0;
			int rank[] = new int[tree.getNumNode()];
			boolean rankSelected[] = new boolean[tree.getNumNode()];
			ComputerMserComponentTree mser = new ComputerMserComponentTree( (ComponentTree) tree);
			mser.setMaxArea(maxArea);
			mser.setMinArea(minArea);
			mser.setMaxVariation(maxVariation);
			mser.setAttribute(attribute);
			mser.setEstimateDelta(estimateDelta);
			for(int i=1; i < 50; i++){
				boolean result[] = mser.getMappingNodesByMSER(i);
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
		else if(tree instanceof TreeOfShape){
			int rank[] = new int[tree.getNumNode()];
			boolean rankSelected[] = new boolean[tree.getNumNode()];
			this.num = 0;
			ComputerMserTreeOfShapes mser = new ComputerMserTreeOfShapes((TreeOfShape) tree);
			mser.setMaxArea(maxArea);
			mser.setMinArea(minArea);
			mser.setMaxVariation(maxVariation);
			mser.setAttribute(attribute);
			mser.setEstimateDelta(estimateDelta);
			for(int i=0; i < 50; i++){
				boolean result[] = mser.getMappingNodesByMSER(i);
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
		else{
			return null;
		}
	}
	
	public boolean[] getMappingSelectedNodes() {
		if(tree instanceof ComponentTree){
			ComputerMserComponentTree mser = new ComputerMserComponentTree( (ComponentTree) tree);
			mser.setMaxArea(maxArea);
			mser.setMinArea(minArea);
			mser.setMaxVariation(maxVariation);
			mser.setAttribute(attribute);
			mser.setEstimateDelta(estimateDelta);
			boolean result[] = mser.getMappingNodesByMSER(delta);
			this.num = mser.getNumMSER();
			return result;
		}
		else if(tree instanceof TreeOfShape){
			ComputerMserTreeOfShapes mser = new ComputerMserTreeOfShapes((TreeOfShape) tree);
			mser.setMaxArea(maxArea);
			mser.setMinArea(minArea);
			mser.setMaxVariation(maxVariation);
			mser.setAttribute(attribute);
			mser.setEstimateDelta(estimateDelta);
			boolean result[] = mser.getMappingNodesByMSER(delta);
			this.num = mser.getNumMSER();
			return result;
		}
		else{
			return null;
		}
	}
	
	
	
	public int getNumOfPruning(){
		return num;
	}
	


	
}
