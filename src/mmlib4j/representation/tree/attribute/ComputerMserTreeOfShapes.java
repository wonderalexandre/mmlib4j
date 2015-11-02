package mmlib4j.representation.tree.attribute;

import java.awt.Color;
import java.util.LinkedList;

import mmlib4j.images.ColorImage;
import mmlib4j.images.impl.ImageFactory;
import mmlib4j.representation.tree.InfoPrunedTree;
import mmlib4j.representation.tree.NodeLevelSets;
import mmlib4j.representation.tree.tos.NodeToS;
import mmlib4j.representation.tree.tos.TreeOfShape;



/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 */
public class ComputerMserTreeOfShapes implements ComputerMser{
	 
	private TreeOfShape tree;
	private NodeToS ascendant[];
	private NodeToS descendants[];
	private int num;
	private double maxVariation = Double.MAX_VALUE;
	private int minArea=0;
	private int maxArea=Integer.MAX_VALUE;
	private Attribute[] stability;
	private int attribute = Attribute.AREA;
	private boolean estimateDelta = false;
	
	public ComputerMserTreeOfShapes(TreeOfShape tree){
		this.tree = tree;
		
	}

	public void setMaxVariation(double d){
		maxVariation = d;
	}
	
	public void setMinArea(int a){ 
		minArea = a;
	}
	
	public void setAttribute(int t){
		attribute = t;
	}
	
	public void setMaxArea(int a){
		maxArea = a;
	}
	

	public Attribute[] getAttributeStability(){
		return stability;
	}
	
	public Double[] getScoreOfBranch(NodeLevelSets no){
		
		Double score[] = new Double[tree.getNumNode()];
		for(NodeToS node: ((NodeToS)no).getPathToRoot()){
			if(ascendant[node.getId()] != null && descendants[node.getId()] != null){
				score[node.getId()] = getStability(node);
			}
		}
		return score;
	}
	
	
	private NodeToS getNodeAscendant(NodeToS node, int h){
		NodeToS n = node;
		if(estimateDelta)
			h =  (int) node.getAttributeValue(Attribute.ALTITUDE)/2;
		for(int i=0; i < h; i++){
			if(node.isNodeMaxtree()){
				if(node.getLevel() >= n.getLevel() + h)
					return n;
			}else{
				if(node.getLevel() <= n.getLevel() - h)
					return n;
			}
			n = n.getParent();
			if(n==null)
				return n;
		}
		return n;
	}

	
	private double getStability(NodeToS node){
		return (ascendant[node.getId()].getAttributeValue(attribute) - descendants[node.getId()].getAttributeValue(attribute)) / (double) node.getAttributeValue(attribute); 
	}
	

	private void maxAreaDescendants(NodeToS nodeAsc, NodeToS nodeDes){
		if(descendants[nodeAsc.getId()] == null)
			descendants[nodeAsc.getId()] = nodeDes;
		
		if(descendants[nodeAsc.getId()].getArea() < nodeDes.getArea())
			descendants[nodeAsc.getId()] = nodeDes;
	}
	
	public LinkedList<NodeToS> getNodesByMSER(int delta){
		
		LinkedList<NodeToS> list = new LinkedList<NodeToS>();
		ascendant = new NodeToS[tree.getNumNode()];
		descendants = new NodeToS[tree.getNumNode()];
		
		for(NodeToS node: tree.getListNodes()){
			NodeToS nodeAsc = getNodeAscendant(node, delta);
			if(nodeAsc != null){
				maxAreaDescendants(nodeAsc, node);
				ascendant[node.getId()] = nodeAsc;
			}
		}
		
		stability = new Attribute[tree.getNumNode()];
		for(NodeToS node: tree.getListNodes()){
			if(ascendant[node.getId()] != null && descendants[node.getId()] != null)
				stability[node.getId()] = new Attribute(Attribute.MSER, getStability(node));
		}

		for(NodeToS node: tree.getListNodes()){
			if(stability[node.getId()] != null && stability[ ascendant[node.getId()].getId() ] != null && stability[ descendants[node.getId()].getId() ] != null){
				double minStabilityDesc = stability[ descendants[node.getId()].getId() ].getValue();
				double minStabilityAsc = stability[ ascendant[node.getId()].getId() ].getValue();
				if(stability[node.getId()].getValue() < minStabilityDesc && stability[node.getId()].getValue() < minStabilityAsc){
					if(stability[node.getId()].getValue() < maxVariation && node.getArea() > minArea && node.getArea() < maxArea){
						list.add(node);
						num++;
					}
				}
			}
		}
		
		return list;
		
	}
	
	
	public int getNumMSER(){
		return num;
	}
	
	public boolean[] getMappingNodesByMSER(int delta){
		num = 0;
		ascendant = new NodeToS[tree.getNumNode()];
		descendants = new NodeToS[tree.getNumNode()];
		boolean mser[] = new boolean[tree.getNumNode()];
		

		for(NodeToS node: tree.getListNodes()){
			NodeToS nodeAsc = getNodeAscendant(node, delta);
			if(nodeAsc != null){
				maxAreaDescendants(nodeAsc, node);
				ascendant[node.getId()] = nodeAsc;
			}
		}
		
		stability = new Attribute[tree.getNumNode()];
		for(NodeToS node: tree.getListNodes()){
			if(ascendant[node.getId()] != null && descendants[node.getId()] != null)
				stability[node.getId()] = new Attribute(Attribute.MSER, getStability(node));
		}

		for(NodeToS node: tree.getListNodes()){
			if(stability[node.getId()] != null && stability[ ascendant[node.getId()].getId() ] != null && stability[ descendants[node.getId()].getId() ] != null){
				double minStabilityDesc = stability[ descendants[node.getId()].getId() ].getValue();
				double minStabilityAsc = stability[ ascendant[node.getId()].getId() ].getValue();
				if(stability[node.getId()].getValue() < minStabilityDesc && stability[node.getId()].getValue() < minStabilityAsc){
					if(stability[node.getId()].getValue() < maxVariation && node.getArea() > minArea && node.getArea() < maxArea){
						mser[node.getId()] = true;
						num++;
					}
				}
			}
		}
		
		return mser;
		
	}
	
	
	public boolean[] getMappingNodesByMSER(int delta, InfoPrunedTree prunedTree){
		ascendant = new NodeToS[tree.getNumNode()];
		descendants = new NodeToS[tree.getNumNode()];
		boolean mser[] = new boolean[tree.getNumNode()];
		
		for(NodeToS node: tree.getListNodes()){
			if(prunedTree.wasPruned(node))
				continue;
			
			NodeToS nodeAsc = getNodeAscendant(node, delta);
			if(nodeAsc != null){
				maxAreaDescendants(nodeAsc, node);
				ascendant[node.getId()] = nodeAsc;
			}
		}
		
		stability = new Attribute[tree.getNumNode()];

		for(NodeToS node: tree.getListNodes()){
			if(prunedTree.wasPruned(node))
				continue;
			
			if(ascendant[node.getId()] != null && descendants[node.getId()] != null)
				stability[node.getId()] = new Attribute(Attribute.MSER, getStability(node));
		}
		
		for(NodeToS node: tree.getListNodes()){
			if(prunedTree.wasPruned(node))
				continue;
			
			
			if(stability[node.getId()] != null && stability[ ascendant[node.getId()].getId() ] != null && stability[ descendants[node.getId()].getId() ] != null){
				double minStabilityDesc = stability[ descendants[node.getId()].getId() ].getValue();
				double minStabilityAsc = stability[ ascendant[node.getId()].getId() ].getValue();
				
				if(stability[node.getId()].getValue() < minStabilityDesc && stability[node.getId()].getValue() < minStabilityAsc){
					if(stability[node.getId()].getValue() < maxVariation && node.getArea() > minArea && node.getArea() < maxArea){
						mser[node.getId()] = true;
					}
				}
			}
		}
		
		return mser;
		
	}

	/*
	public void imprimirScoreRamo(){

		NodeToS no = tree.getLeaves().getFirst();
		
		Double q[] = new Double[tree.getNumNode()];
		int cont =0;
		for(NodeToS node: no.getPathToRoot()){
			if(ascendant[node.getId()] != null && descendants[node.getId()] != null)
				q[node.getId()] = getStability(node);
			cont++;
		}
		
		for(NodeToS node:  no.getPathToRoot()){
			if(q[node.getId()] != null && q[ascendant[node.getId()].getId()] != null){
				double minStabilityDesc = getMinimalStabilityOfDescendants(node, q);
				if(minStabilityDesc == Double.MAX_VALUE) continue;
				
				double stabilityAscendant = q[ ascendant[node.getId()].getId() ];
				
				if(q[node.getId()] < minStabilityDesc && q[node.getId()] < stabilityAscendant){
					System.out.println("***" + q[node.getId()]);
				}else{
					System.out.println(q[node.getId()]);
				}
			}
		}
		System.out.println("Quant: " + cont);
		
	}
	*/
	
	public ColorImage getImageMSER(int delta){
		ColorImage img = ImageFactory.createCopyColorImage(tree.getInputImage());
		for(NodeToS node: getNodesByMSER(delta)){
			for(int p: node.getPixelsOfCC()){
				img.setPixel(p, Color.RED.getRGB());
			}
		}
		return img;
	}

	public ColorImage getPointImageMSER(int delta){
		ColorImage img = ImageFactory.createCopyColorImage(tree.getInputImage());
		for(NodeToS node: getNodesByMSER(delta)){
			for(int p: node.getCanonicalPixels()){
				img.setPixel(p, Color.RED.getRGB());
			}
		}
		return img;
	}

	public void setEstimateDelta(boolean b) {
		estimateDelta = b;
	}


}
