package mmlib4j.representation.tree.attribute;

import java.awt.Color;
import java.util.LinkedList;

import mmlib4j.gui.WindowImages;
import mmlib4j.images.ColorImage;
import mmlib4j.images.GrayScaleImage;
import mmlib4j.images.impl.ImageFactory;
import mmlib4j.representation.tree.InfoPrunedTree;
import mmlib4j.representation.tree.NodeLevelSets;
import mmlib4j.representation.tree.componentTree.ComponentTree;
import mmlib4j.representation.tree.componentTree.ConnectedFilteringByComponentTree;
import mmlib4j.representation.tree.componentTree.NodeCT;
import mmlib4j.utils.AdjacencyRelation;
import mmlib4j.utils.ImageBuilder;


/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 */
public class ComputerMserComponentTree implements ComputerMser {
	
	private ComponentTree tree;
	private NodeCT ascendant[];
	private NodeCT descendants[];
	private int num;
	private Attribute stability[];
	private double maxVariation = Double.MAX_VALUE;
	private int minArea=0;
	private int maxArea=Integer.MAX_VALUE;
	
	private boolean estimateDelta = false;
	
	private int attribute = Attribute.AREA;
	
	public ComputerMserComponentTree(ComponentTree tree){
		this.tree = tree;
		
	}
	
	public void setEstimateDelta(boolean b){
		estimateDelta = b;
	}
	
	public void setMaxVariation(double d){
		maxVariation = d;
	}
	
	public void setAttribute(int t){
		attribute = t;
	}
	
	public void setMinArea(int a){ 
		minArea = a;
	}
	
	public void setMaxArea(int a){
		maxArea = a;
	}
	
	private NodeCT getNodeAscendant(NodeCT node, int h){
		NodeCT n = node;
		if(estimateDelta)
			h =  (int) node.getAttributeValue(Attribute.ALTITUDE)/2;
		for(int i=0; i <= h; i++){
			if(node.isMaxtree()){
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
	
	private void maxAreaDescendants(NodeCT nodeAsc, NodeCT nodeDes){
		if(descendants[nodeAsc.getId()] == null)
			descendants[nodeAsc.getId()] = nodeDes;
		
		if(descendants[nodeAsc.getId()].getArea() < nodeDes.getArea())
			descendants[nodeAsc.getId()] = nodeDes;
		
	}
	
	
	protected double getStabilityByBoundary(NodeCT node){
		if(ascendant[node.getId()] != null && descendants[node.getId()] != null){
			return (ascendant[node.getId()].getArea() - descendants[node.getId()].getArea()) / (double)node.getAttributeValue(Attribute.PERIMETER_EXTERNAL);
		}
		else{
			return Double.MAX_VALUE;
		}
	}
	
	protected double getStability(NodeCT node){
		if(ascendant[node.getId()] != null && descendants[node.getId()] != null){
			//return (ascendant[node.getId()].getArea() - descendants[node.getId()].getArea()) / (double) node.getArea();
			return (ascendant[node.getId()].getAttributeValue(attribute) - descendants[node.getId()].getAttributeValue(attribute)) / (double) node.getAttributeValue(attribute);
		}
		else{
			return Double.MAX_VALUE;
		}
	}
	
	public LinkedList<NodeCT> getNodesByMSER(int delta){
		
		LinkedList<NodeCT> list = new LinkedList<NodeCT>();
		ascendant = new NodeCT[tree.getNumNode()];
		descendants = new NodeCT[tree.getNumNode()];

		for(NodeCT node: tree.getListNodes()){
			NodeCT nodeAsc = getNodeAscendant(node, delta);
			if(nodeAsc != null){
				maxAreaDescendants(nodeAsc, node);
				ascendant[node.getId()] = nodeAsc;
				
			}
		}
		
		stability = new Attribute[tree.getNumNode()];
		for(NodeCT node: tree.getListNodes()){
			if(ascendant[node.getId()] != null && descendants[node.getId()] != null)
				stability[node.getId()] = new Attribute(Attribute.MSER, getStability(node));
		}
		
		boolean mser[] = new boolean[tree.getNumNode()];
		for(NodeCT node: tree.getListNodes()){
			
			if(stability[node.getId()] != null && stability[ ascendant[node.getId()].getId() ] != null && stability[ descendants[node.getId()].getId() ] != null){
				double minStabilityDesc = stability[ descendants[node.getId()].getId() ].getValue();
				double minStabilityAsc = stability[ ascendant[node.getId()].getId() ].getValue();
				if(stability[node.getId()].getValue() < minStabilityDesc && stability[node.getId()].getValue() < minStabilityAsc){
					if(stability[node.getId()].getValue() < maxVariation && node.getArea() > minArea && node.getArea() < maxArea){
						mser[node.getId()] = true;
						list.add(node);
					}
					//System.out.print("*");
				}
				//System.out.println(q[node.getId()]);
			}
		}
		return list;
		
	}
	

	
	public boolean[] getMappingNodesByMSER(int delta){
		ascendant = new NodeCT[tree.getNumNode()];
		descendants = new NodeCT[tree.getNumNode()];
		boolean mser[] = new boolean[tree.getNumNode()];
		num = 0;
		for(NodeCT node: tree.getListNodes()){
			NodeCT nodeAsc = getNodeAscendant(node, delta);
			if(nodeAsc != null){
				maxAreaDescendants(nodeAsc, node);
				ascendant[node.getId()] = nodeAsc;
			}
		}
		
		stability = new Attribute[tree.getNumNode()];
		for(NodeCT node: tree.getListNodes()){
			if(ascendant[node.getId()] != null && descendants[node.getId()] != null){
				stability[node.getId()] = new Attribute(Attribute.MSER, getStability(node));
				
			}
			
		}
		
		for(NodeCT node: tree.getListNodes()){
			if(stability[node.getId()] != null && stability[ ascendant[node.getId()].getId() ] != null && stability[ descendants[node.getId()].getId() ] != null){
				double minStabilityDesc = stability[ descendants[node.getId()].getId() ].getValue();
				double minStabilityAsc = stability[ ascendant[node.getId()].getId() ].getValue();
				if(stability[node.getId()].getValue() < minStabilityDesc && stability[node.getId()].getValue() < minStabilityAsc){
					if(stability[node.getId()].getValue() < maxVariation && node.getArea() >= minArea && node.getArea() <= maxArea){
						mser[node.getId()] = true;
						num++;
					}
				}
			}
		}
		return mser;
		
	}
	
	public int getNumMSER(){
		return num;
	}
	
	
	public boolean[] getMappingNodesByMSER(int delta, InfoPrunedTree prunedTree){
		ascendant = new NodeCT[tree.getNumNode()];
		descendants = new NodeCT[tree.getNumNode()];
		boolean mser[] = new boolean[tree.getNumNode()];
		
		for(NodeCT node: tree.getListNodes()){
			if(prunedTree.wasPruned(node))
				continue;
			
			NodeCT nodeAsc = getNodeAscendant(node, delta);
			if(nodeAsc != null){
				maxAreaDescendants(nodeAsc, node);
				ascendant[node.getId()] = nodeAsc;
			}
		}
		
		stability = new Attribute[tree.getNumNode()];

		for(NodeCT node: tree.getListNodes()){
			if(prunedTree.wasPruned(node))
				continue;
			
			if(ascendant[node.getId()] != null && descendants[node.getId()] != null)
				stability[node.getId()] = new Attribute(Attribute.MSER, getStability(node));
		}
		
		for(NodeCT node: tree.getListNodes()){
			if(prunedTree.wasPruned(node))
				continue;
			
			
			if(stability[node.getId()] != null && stability[ ascendant[node.getId()].getId() ] != null && stability[ descendants[node.getId()].getId() ] != null){
				double minStabilityDesc = stability[ descendants[node.getId()].getId() ].getValue();
				double minStabilityAsc = stability[ ascendant[node.getId()].getId() ].getValue();
				
				if(stability[node.getId()].getValue() < minStabilityDesc && stability[node.getId()].getValue() < minStabilityAsc){
					if(stability[node.getId()].getValue() < maxVariation){
						mser[node.getId()] = true;
					}
				}
			}
		}
		
		return mser;
		
	}
	
	public Attribute[] getAttributeStability(){
		return stability;
	}
	
	public Double[] getScoreOfBranch(NodeLevelSets no){
		Double score[] = new Double[tree.getNumNode()];
		for(NodeCT node: ((NodeCT)no).getPathToRoot()){
			if(ascendant[node.getId()] != null && descendants[node.getId()] != null){
				score[node.getId()] = getStability(node);
			}
		}
		return score;
	}
	
	
	public ColorImage getImageMSER(int delta){
		ColorImage img = ImageFactory.createCopyColorImage(tree.getInputImage());
		boolean b[] = getMappingNodesByMSER(delta);
		for(NodeCT node: tree.getListNodes()){
			if(b[node.getId()])
				for(int p: node.getPixelsOfCC()){
					img.setPixel(p, Color.RED.getRGB());
				}
		}
		return img;
	}

	public ColorImage getPointImageMSER(int delta){
		ColorImage img = ImageFactory.createCopyColorImage(tree.getInputImage());
		for(NodeCT node: getNodesByMSER(delta)){
			for(int p: node.getCanonicalPixels()){
				img.setPixel(p, Color.RED.getRGB());
			}
		}
		return img;
	}
	
	public static void main(String args[]){
		GrayScaleImage img = ImageBuilder.openGrayImage();
		ConnectedFilteringByComponentTree tree = new ConnectedFilteringByComponentTree(img, AdjacencyRelation.getAdjacency8(), false);
		//tree.extendedTree();
		//.extendedTree();
		ComputerMserComponentTree m = new ComputerMserComponentTree(tree);
		int delta = 10;
		//WindowImages.show(m.getPointImageMSER(delta));
		
		WindowImages.show(m.getImageMSER(delta));
		
		 
		//m.imprimirScoreRamo();
	}
	
}
