package mmlib4j.representation.tree.pruningStrategy;

import mmlib4j.representation.tree.MorphologicalTree;
import mmlib4j.representation.tree.NodeLevelSets;
import mmlib4j.representation.tree.attribute.Attribute;
import mmlib4j.representation.tree.attribute.ComputerMSER;
import mmlib4j.representation.tree.componentTree.ComponentTree;
import mmlib4j.representation.tree.tos.TreeOfShape;
import mmlib4j.utils.Utils;


/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 */
public class PruningBasedCircularity extends FilteringBasedOnPruning{
	
	
	String selected = "";
	

	protected int delta;
	private double maxVariation = Double.MAX_VALUE;
	private int attribute;
	
	private int tMin=5;
	private int tMax=Integer.MAX_VALUE;
	
	public PruningBasedCircularity(MorphologicalTree tree){
		super(tree, Attribute.AREA);
		Attribute.loadAttribute(tree, Attribute.AREA);
		Attribute.loadAttribute(tree, Attribute.CIRCULARITY);
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
				ComputerMSER mser = new ComputerMSER(tree, attribute);
				mser.setParameters(areaMin, areaMax, maxVariation, attribute);
				selectedNodes = mser.computerMSER(delta);
				if(Utils.debug)
					System.out.println("PruningBasedCircularity - " + selected);
			}
		}
		else if(selected.equals("TBMR")){
			selectedNodes = new PruningBasedTBMR(tree, tMin, tMax).getMappingSelectedNodes();
			if(Utils.debug)
				System.out.println("PruningBasedCircularity - " + selected);
		}
		
		boolean selectedNodesCircles[] = new boolean[tree.getNumNode()];
		if(tree instanceof ComponentTree){
			ComponentTree tree = (ComponentTree) this.tree;
			for(NodeLevelSets node: tree.getListNodes()){
				if( (selectedNodes == null && isNodeCircle(node) ) || (selectedNodes != null && selectedNodes[node.getId()] && isNodeCircle(node) ) ){
					selectedNodesCircles[node.getId()] = true;
				}
			}
			return selectedNodesCircles;
		}
		else if(tree instanceof TreeOfShape){
			TreeOfShape tree = (TreeOfShape) this.tree;
			for(NodeLevelSets node: tree.getListNodes()){
				if( (selectedNodes == null && isNodeCircle(node) ) || (selectedNodes != null && selectedNodes[node.getId()] && isNodeCircle(node) ) ){
					selectedNodesCircles[node.getId()] = true;
				}
			}
			return selectedNodesCircles;
		}
		else{
			return null;
		}
	}
	
	int areaMin = 0;
	int areaMax = 200;
	double circ = 0;
	
	public void setParameters(int areaMin, int areaMax, double circ, String selectedNode){
		this.areaMin = areaMin;
		this.areaMax = areaMax;
		this.circ = circ;
		selected = selectedNode;
	}

	public boolean isNodeCircle(NodeLevelSets node){
		if ( 
				(areaMin <= node.getAttributeValue(Attribute.AREA) && node.getAttributeValue(Attribute.AREA) <= areaMax )
				&& (node.getAttributeValue(Attribute.CIRCULARITY) >= circ)
			)
			return true;
		else
			return false;
	}
	
	

}
