package mmlib4j.representation.tree.attribute;

import java.awt.Color;

import mmlib4j.datastruct.SimpleLinkedList;
import mmlib4j.gui.WindowImages;
import mmlib4j.images.ColorImage;
import mmlib4j.images.GrayScaleImage;
import mmlib4j.images.impl.ImageFactory;
import mmlib4j.representation.tree.MorphologicalTree;
import mmlib4j.representation.tree.NodeLevelSets;
import mmlib4j.representation.tree.componentTree.ComponentTree;
import mmlib4j.utils.AdjacencyRelation;
import mmlib4j.utils.ImageBuilder;

public class ComputerMSER {
	
	protected MorphologicalTree tree;
	protected Double q[];
	
	private double maxVariation = 0.5;
	private int minArea=0;
	private int maxArea=Integer.MAX_VALUE;
	private boolean estimateDelta = false;
	private int attributeType;
	private int num;
	
	private NodeLevelSets ascendant[];
	private NodeLevelSets descendants[];
	private Attribute stability[];
	

	public ComputerMSER(MorphologicalTree tree, int attributeType){
		this.tree = tree;
		this.attributeType = attributeType;
		Attribute.loadAttribute(tree, attributeType);
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
		attributeType = t;
	}

	public void setParameters(int minArea, int maxArea, double maxVariation, int attribute){
		this.minArea = minArea;
		this.maxArea = maxArea;
		this.maxVariation = maxVariation;
		this.attributeType = attribute;
	}

	public NodeLevelSets[] getAscendant() {
		return ascendant;
	}
	public NodeLevelSets[] getDescendants() {
		return descendants;
	}

	public boolean[] computerMSER(int delta){
		this.ascendant = new NodeLevelSets[tree.getNumNode()];
		this.descendants = new NodeLevelSets[tree.getNumNode()];
		boolean mser[] = new boolean[tree.getNumNode()];
		this.num = 0;
		for(NodeLevelSets node: tree.getListNodes()){
			NodeLevelSets nodeAsc = getNodeAscendant(node, delta);
			if(nodeAsc != null){
				maxAreaDescendants(nodeAsc, node);
				this.ascendant[node.getId()] = nodeAsc;
			}
		}
		
		this.stability = new Attribute[tree.getNumNode()];
		for(NodeLevelSets node: tree.getListNodes()){
			if(this.ascendant[node.getId()] != null && this.descendants[node.getId()] != null){
				this.stability[node.getId()] = new Attribute(Attribute.MSER, getStability(node));
				
			}
			
		}
		
		for(NodeLevelSets node: tree.getListNodes()){
			if(this.stability[node.getId()] != null && this.stability[this.ascendant[node.getId()].getId() ] != null && this.stability[this.descendants[node.getId()].getId() ] != null){
				double minStabilityDesc = this.stability[this.descendants[node.getId()].getId() ].getValue();
				double minStabilityAsc = this.stability[this.ascendant[node.getId()].getId() ].getValue();
				if(stability[node.getId()].getValue() < minStabilityDesc && this.stability[node.getId()].getValue() < minStabilityAsc){
					if(stability[node.getId()].getValue() < maxVariation && node.getAttributeValue(Attribute.AREA) >= minArea && node.getAttributeValue(Attribute.AREA) <= maxArea){
						mser[node.getId()] = true;
						this.num++;
					}
				}
			}
		}
		return mser;
		
	}

	public int getNumNodes() {
		return  num;
	}

	public SimpleLinkedList<NodeLevelSets> getListOfSelectedNodes(int delta){
		boolean mapping[] = computerMSER(delta);
		SimpleLinkedList<NodeLevelSets> list = new SimpleLinkedList<NodeLevelSets>(this.num);
		for(NodeLevelSets node: tree.getListNodes()) {
			if(mapping[node.getId()])
				list.add(node);
		}
		return list;	
	}
	
	

	private NodeLevelSets getNodeAscendant(NodeLevelSets node, int h){
		NodeLevelSets n = node;
		if(estimateDelta)
			h =  (int) node.getAttributeValue(Attribute.ALTITUDE)/2;
		for(int i=0; i <= h; i++){
			if(node.isNodeMaxtree()){
				if(node.getLevel() >= n.getLevel() + h)
					return n;
			}else{
				if(node.getLevel() <= n.getLevel() - h)
					return n;
			}
			if(n.getParent() != null)
				n = n.getParent();
			else 
				return n;
		}
		return n;
	}
	
	private void maxAreaDescendants(NodeLevelSets nodeAsc, NodeLevelSets nodeDes){
		if(descendants[nodeAsc.getId()] == null)
			descendants[nodeAsc.getId()] = nodeDes;
		
		if(descendants[nodeAsc.getId()].getAttributeValue(Attribute.AREA) < nodeDes.getAttributeValue(Attribute.AREA))
			descendants[nodeAsc.getId()] = nodeDes;
		
	}
	
	
	private double getStabilityByBoundary(NodeLevelSets node){
		if(ascendant[node.getId()] != null && descendants[node.getId()] != null){
			return (ascendant[node.getId()].getAttributeValue(Attribute.AREA) - descendants[node.getId()].getAttributeValue(Attribute.AREA)) / (double)node.getAttributeValue(Attribute.PERIMETER_EXTERNAL);
		}
		else{
			return Double.MAX_VALUE;
		}
	}
	
	private double getStability(NodeLevelSets node){
		if(ascendant[node.getId()] != null && descendants[node.getId()] != null){
			//return (ascendant[node.getId()].getArea() - descendants[node.getId()].getArea()) / (double) node.getArea();
			//System.out.println(ascendant[node.getId()].getAttributeValue(attributeType));
		
			return (ascendant[node.getId()].getAttributeValue(attributeType) - descendants[node.getId()].getAttributeValue(attributeType)) / (double) node.getAttributeValue(attributeType);
		}
		else{
			return Double.MAX_VALUE;
		}
	}
	
	public Attribute[] getAttributeStability(){
		return stability;
	}
	
	public static void loadAttribute(MorphologicalTree tree) {
		loadAttribute(tree, Attribute.AREA, 5);
	}
	
	public static void loadAttribute(MorphologicalTree tree, int attributeType, int delta) {
		ComputerMSER mser = new ComputerMSER(tree, attributeType);
		mser.computerMSER(delta);
		mser.addAttributeInNodes(tree.getListNodes());
	}
	
	/**
	 * 
	 * 	This method add the computed attributes in the list of nodes passed by parameter.
	 * 
	 * 	@param listNodes A list of nodes.
	 * 
	 */
	public void addAttributeInNodes(SimpleLinkedList<NodeLevelSets> listNodes){
		for(NodeLevelSets node: listNodes){
			addAttributeInNodes(node);
		}
	}
	
	public void addAttributeInNodes(NodeLevelSets node){
		node.addAttribute(Attribute.MSER, stability[ node.getId() ]);
	}
	
	public Double[] getScoreOfBranch(NodeLevelSets no){
		Double score[] = new Double[tree.getNumNode()];
		for(NodeLevelSets node: no.getPathToRoot()){
			if(ascendant[node.getId()] != null && descendants[node.getId()] != null){
				score[node.getId()] = getStability(node);
			}
		}
		return score;
	}
	
	
	public ColorImage getImageMSER(int delta){
		ColorImage img = ImageFactory.createCopyColorImage(tree.getInputImage());
		boolean b[] = computerMSER(delta);
		for(NodeLevelSets node: tree.getListNodes()){
			if(b[node.getId()])
				for(int p: node.getPixelsOfCC()){
					img.setPixel(p, Color.RED.getRGB());
				}
		}
		return img;
	}

	public ColorImage getPointImageMSER(int delta){
		ColorImage img = ImageFactory.createCopyColorImage(tree.getInputImage());
		for(NodeLevelSets node: getListOfSelectedNodes(delta)){
			for(int p: node.getCompactNodePixels()){
				img.setPixel(p, Color.RED.getRGB());
			}
		}
		return img;
	}

	
	public static void main(String args[]){
		GrayScaleImage img = ImageBuilder.openGrayImage();
		MorphologicalTree tree = new ComponentTree(img, AdjacencyRelation.getAdjacency8(), true);
		//tree.extendedTree();
		//.extendedTree();
		
		ComputerMSER m = new ComputerMSER(tree, Attribute.AREA);
		WindowImages.show(m.getImageMSER(5));
		
		
		 
		//m.imprimirScoreRamo();
	}
	
}
