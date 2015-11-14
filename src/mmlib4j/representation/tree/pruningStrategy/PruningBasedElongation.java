package mmlib4j.representation.tree.pruningStrategy;

import mmlib4j.representation.tree.MorphologicalTreeFiltering;
import mmlib4j.representation.tree.NodeLevelSets;
import mmlib4j.representation.tree.attribute.Attribute;
import mmlib4j.representation.tree.componentTree.ComponentTree;
import mmlib4j.representation.tree.componentTree.NodeCT;
import mmlib4j.representation.tree.tos.NodeToS;
import mmlib4j.representation.tree.tos.TreeOfShape;
import mmlib4j.utils.Utils;


/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 */
public class PruningBasedElongation implements MappingStrategyOfPruning{
	
	MorphologicalTreeFiltering tree;
	String selected = "";
	
	private int tMin=5;
	private int tMax=Integer.MAX_VALUE;
	
	protected int delta;
	private double maxVariation = Double.MAX_VALUE;
	private int attribute;
	
	public PruningBasedElongation(MorphologicalTreeFiltering tree){
		this.tree = tree;
		tree.loadAttribute(Attribute.AREA);
		tree.loadAttribute(Attribute.MOMENT_ELONGATION);
	}
	
	public void setParametersTBMR(int tMin, int tMax){
		this.tMin = tMin;
		this.tMax = tMax;
	}
	
	public void setParametersMSER(int delta, double maxVariation, int attribute){
		this.delta = delta;
		this.maxVariation = maxVariation;
		this.attribute = attribute;
	}
	
	public boolean[] getMappingSelectedNodes() {
		boolean selectedNodes[] = null;

		if(selected.equals("MSER")){
			if(delta != 0){
				PruningBasedMSER pruning = new PruningBasedMSER(tree, delta);
				pruning.setAttribute(attribute);
				pruning.setMaxArea(areaMax);
				pruning.setMinArea(areaMin);
				pruning.setMaxVariation(maxVariation);
				selectedNodes = pruning.getMappingSelectedNodes();
				if(Utils.debug)
					System.out.println("PruningBasedElongation - " + selected);
			}
		}else if(selected.equals("MSER by rank")){
			if(delta != 0){
				PruningBasedMSER pruning = new PruningBasedMSER(tree, delta);
				pruning.setAttribute(attribute);
				pruning.setMaxArea(areaMax);
				pruning.setMinArea(areaMin);
				pruning.setMaxVariation(maxVariation);
				selectedNodes = pruning.getMappingSelectedNodesRank();
				if(Utils.debug)
					System.out.println("PruningBasedElongation - " + selected);
			}
		}
		else if(selected.equals("TBMR")){
			selectedNodes = new PruningBasedTBMR(tree, tMin, tMax).getMappingSelectedNodes();
			if(Utils.debug)
				System.out.println("PruningBasedElongation - " + selected);
		}
		
		boolean mserElongation[] = new boolean[tree.getNumNode()];
		if(tree instanceof ComponentTree){
			ComponentTree tree = (ComponentTree) this.tree;
			for(NodeCT node: tree.getNodesMap()){
				if( (selectedNodes == null && isNodeElongation(node) ) || (selectedNodes != null && selectedNodes[node.getId()] && isNodeElongation(node)) ){
					mserElongation[node.getId()] = true;
				}
			}
			return mserElongation;
		}
		else if(tree instanceof TreeOfShape){
			TreeOfShape tree = (TreeOfShape) this.tree;
			for(NodeToS node: tree.getNodesMap()){
				if( (selectedNodes == null && isNodeElongation(node) ) || (selectedNodes != null && selectedNodes[node.getId()] && isNodeElongation(node)) ){
					mserElongation[node.getId()] = true;
				}
			}
			return mserElongation;
		}
		else{
			return null;
		}
	}
	
	int areaMin;
	int areaMax;
	double elongation;
	
	public void setParameters(int areaMin, int areaMax, double elon, String selectedNode){
		this.areaMin = areaMin;
		this.areaMax = areaMax;
		this.elongation = elon;
	}


	public boolean isNodeElongation(NodeLevelSets node){
		if(node.getAttributeValue(Attribute.MOMENT_ELONGATION) < elongation && node.getArea() >= areaMin && node.getArea() <= areaMax){
			return true;
		}
		else
			return false;
	}
	
	

	
}
