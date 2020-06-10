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
public class PruningBasedTextLocation extends FilteringBasedOnPruning{
	
	String selected;
	
	protected int delta;
	private double maxVariation = Double.MAX_VALUE;
	private int attribute;
	
	private int tMin=5;
	private int tMax=Integer.MAX_VALUE;
	
	public PruningBasedTextLocation(MorphologicalTree tree){
		super(tree, Attribute.AREA);
		Attribute.loadAttribute(tree, Attribute.AREA);
		Attribute.loadAttribute(tree, Attribute.HEIGHT);
		Attribute.loadAttribute(tree, Attribute.WIDTH);
		Attribute.loadAttribute(tree, Attribute.BIT_QUADS_HOLE_NUMBER);
		Attribute.loadAttribute(tree, Attribute.RECTANGULARITY);
		Attribute.loadAttribute(tree, Attribute.RATIO_WIDTH_HEIGHT);
		Attribute.loadAttribute(tree, Attribute.VARIANCE_LEVEL);
	}
	
	public void setParametersTBMR(int tMin, int tMax){
		this.tMin = tMin;
		this.tMax = tMax;
	}

	
	public void setSelected(String s){
		selected = s;
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
				mser.setParameters(areaMax, areaMax, maxVariation, attribute);
				selectedNodes = mser.computerMSER(delta);
				if(Utils.debug)
					System.out.println("PruningBasedTextLocation - " + selected);
			}
		}else if(selected.equals("TBMR")){
			selectedNodes = new PruningBasedTBMR(tree, tMin, tMax).getMappingSelectedNodes();
			if(Utils.debug)
				System.out.println("PruningBasedTextLocation - " + selected);
		}
		
		
		boolean selectedNodesTextLocation[] = new boolean[tree.getNumNode()];
		if(tree instanceof ComponentTree){
			ComponentTree tree = (ComponentTree) this.tree;
			
			for(NodeLevelSets node: tree.getNodesMap()){
				if( (selectedNodes == null && isNodeTextLocation(node) ) ||(selectedNodes != null && selectedNodes[node.getId()] && isNodeTextLocation(node)) ){
					selectedNodesTextLocation[node.getId()] = true;
				}
			}
			return selectedNodesTextLocation;
		}
		else if(tree instanceof TreeOfShape){
			TreeOfShape tree = (TreeOfShape) this.tree;
			for(NodeLevelSets node: tree.getNodesMap()){
				if( (selectedNodes == null && isNodeTextLocation(node) ) ||(selectedNodes != null && selectedNodes[node.getId()] && isNodeTextLocation(node)) ){
					selectedNodesTextLocation[node.getId()] = true;
				}
			}
			return selectedNodesTextLocation;
		}
		else{
			return null;
		}
	}
	
	int areaMin=50;
	int areaMax=100000;
	int heightMin = 10;
	int heightMax = 400;
	int widthMin = 8;
	int widthMax = 400;
	int numHoleMin = 0;
	int numHoleMax = 20;
	double rectMin = 0.3;
	double rectMax = 1;
	double ratioWHMin=0;
	double ratioWHMax=14;
	double varianceMin=0;
	double varianceMax=100;
	
	
	public void setParameters(int areaMin, int areaMax, int heightMin, int heightMax, int widthMin, int widthMax,
			int numHoleMin, int numHoleMax, double rectMin, double rectMax, double ratioWHMin, double ratioWHMax, double varianceMin, double varianceMax, String selectedNode){
		this.areaMin = areaMin;
		this.areaMax = areaMax;
		this.heightMin = heightMin;
		this.heightMax = heightMax;
		this.widthMin = widthMin;
		this.widthMax = widthMax;
		this.numHoleMin = numHoleMin;
		this.numHoleMax = numHoleMax;
		this.rectMin = rectMin;
		this.rectMax = rectMax;
		this.ratioWHMin=ratioWHMin;
		this.ratioWHMax=ratioWHMax;
		this.varianceMin=varianceMin;
		this.varianceMax=varianceMax;
		this.selected = selectedNode;
	}


	public boolean isNodeTextLocation(NodeLevelSets node){
		
		if((areaMin <= node.getAttributeValue(Attribute.AREA) && node.getAttributeValue(Attribute.AREA) <= areaMax)
				&& (heightMin <= node.getAttributeValue(Attribute.HEIGHT) && node.getAttributeValue(Attribute.HEIGHT) <= heightMax)
				&& (widthMin <= node.getAttributeValue(Attribute.WIDTH) && node.getAttributeValue(Attribute.WIDTH) <= widthMax)
				&& (numHoleMin <= node.getAttributeValue(Attribute.BIT_QUADS_HOLE_NUMBER) && node.getAttributeValue(Attribute.BIT_QUADS_HOLE_NUMBER) <= numHoleMax)
				&& (rectMin <= node.getAttributeValue(Attribute.RECTANGULARITY) && node.getAttributeValue(Attribute.RECTANGULARITY) <= rectMax)
				&& (ratioWHMin <= node.getAttributeValue(Attribute.RATIO_WIDTH_HEIGHT) &&  node.getAttributeValue(Attribute.RATIO_WIDTH_HEIGHT) <= ratioWHMax)
				&& (varianceMin <= node.getAttributeValue(Attribute.VARIANCE_LEVEL) && node.getAttributeValue(Attribute.VARIANCE_LEVEL) <= varianceMax)
				//&& (eccentMin <= node.getAttributeValue(Attribute.MOMENT_ECCENTRICITY) && node.getAttributeValue(Attribute.MOMENT_ECCENTRICITY) <= eccentMax)
				//&& (compactMin <= node.getAttributeValue(Attribute.COMPACTNESS) && node.getAttributeValue(Attribute.COMPACTNESS) <= compactMax)
				//&& (node.getAttributeValue(Attribute.ALTITUDE) < 100) 
			){
			//System.out.println("MOMENT_ECCENTRICITY: "+node.getAttributeValue(Attribute.MOMENT_ECCENTRICITY));
			//System.out.println("MOMENT_COMPACTNESS: "+node.getAttributeValue(Attribute.MOMENT_COMPACTNESS));
			//System.out.println("COMPACTNESS: "+node.getAttributeValue(Attribute.COMPACTNESS));
			return true;
		}else
			return false;
		
	}
	
	
	
}

