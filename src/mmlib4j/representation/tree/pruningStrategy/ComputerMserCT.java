package mmlib4j.representation.tree.pruningStrategy;

import java.awt.Color;
import java.util.Arrays;
import java.util.LinkedList;

import mmlib4j.gui.WindowImages;
import mmlib4j.images.ColorImage;
import mmlib4j.images.GrayScaleImage;
import mmlib4j.images.impl.ImageFactory;
import mmlib4j.representation.tree.InfoPrunedTree;
import mmlib4j.representation.tree.componentTree.ComponentTree;
import mmlib4j.representation.tree.componentTree.ConnectedFilteringByComponentTree;
import mmlib4j.representation.tree.componentTree.NodeCT;
import mmlib4j.utils.AdjacencyRelation;
import mmlib4j.utils.ImageBuilder;


/**
 * MMorph4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 */
public class ComputerMserCT {
	
	private ComponentTree tree;
	private NodeCT ascendant[];
	private NodeCT descendants[];
	private int num;
	private Double q[];
	private double maxVariation = Double.MAX_VALUE;
	private int minArea=0;
	private int maxArea=Integer.MAX_VALUE;
	
	public ComputerMserCT(ComponentTree tree){
		this.tree = tree;
		
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
	
	private NodeCT getNodeAscendant(NodeCT node, int h){
		NodeCT n = node;
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
	
	
	public double getStabilityByBoundary(NodeCT node){
		if(ascendant[node.getId()] != null && descendants[node.getId()] != null){
			return (ascendant[node.getId()].getArea() - descendants[node.getId()].getArea()) / (double)node.getPerimeterExternal();
		}
		else{
			return Double.MAX_VALUE;
		}
	}
	
	public double getStability(NodeCT node){
		if(ascendant[node.getId()] != null && descendants[node.getId()] != null){
			return (ascendant[node.getId()].getArea() - descendants[node.getId()].getArea()) / (double) node.getArea();
			//return (ascendant[node.getId()].getHeightNode() - descendants[node.getId()].getHeightNode()) / (double) node.getHeightNode();
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
		
		q = new Double[tree.getNumNode()];
		for(NodeCT node: tree.getListNodes()){
			if(ascendant[node.getId()] != null && descendants[node.getId()] != null)
				q[node.getId()] = getStability(node);
		}
		
		boolean mser[] = new boolean[tree.getNumNode()];
		for(NodeCT node: tree.getListNodes()){
			
			if(q[node.getId()] != null && q[ ascendant[node.getId()].getId() ] != null && q[ descendants[node.getId()].getId() ] != null){
				double minStabilityDesc = q[ descendants[node.getId()].getId() ];
				double minStabilityAsc = q[ ascendant[node.getId()].getId() ];
				if(q[node.getId()] < minStabilityDesc && q[node.getId()] < minStabilityAsc){
					if(q[node.getId()] < maxVariation && node.getArea() > minArea && node.getArea() < maxArea){
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
		
		q= new Double[tree.getNumNode()];

		for(NodeCT node: tree.getListNodes()){
			if(ascendant[node.getId()] != null && descendants[node.getId()] != null){
				q[node.getId()] = getStability(node);
				node.mser = q[node.getId()];
				
			}
			
		}
		
		for(NodeCT node: tree.getListNodes()){
			if(q[node.getId()] != null && q[ ascendant[node.getId()].getId() ] != null && q[ descendants[node.getId()].getId() ] != null){
				double minStabilityDesc = q[ descendants[node.getId()].getId() ];
				double minStabilityAsc = q[ ascendant[node.getId()].getId() ];
				if(q[node.getId()] < minStabilityDesc && q[node.getId()] < minStabilityAsc){
					if(q[node.getId()] < maxVariation){
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
		
		q = new Double[tree.getNumNode()];

		for(NodeCT node: tree.getListNodes()){
			if(prunedTree.wasPruned(node))
				continue;
			
			if(ascendant[node.getId()] != null && descendants[node.getId()] != null)
				q[node.getId()] = getStability(node);
		}
		
		for(NodeCT node: tree.getListNodes()){
			if(prunedTree.wasPruned(node))
				continue;
			
			
			if(q[node.getId()] != null && q[ ascendant[node.getId()].getId() ] != null && q[ descendants[node.getId()].getId() ] != null){
				double minStabilityDesc = q[ descendants[node.getId()].getId() ];
				double minStabilityAsc = q[ ascendant[node.getId()].getId() ];
				
				if(q[node.getId()] < minStabilityDesc && q[node.getId()] < minStabilityAsc){
					if(q[node.getId()] < maxVariation){
						mser[node.getId()] = true;
					}
				}
			}
		}
		
		return mser;
		
	}
	
	public Double[] getScore(){
		return q;
	}
	
	public Double[] getScoreOfBranch(NodeCT no){
		
		q= new Double[tree.getNumNode()];
		
		for(NodeCT node: no.getPathToRoot()){
			if(ascendant[node.getId()] != null && descendants[node.getId()] != null){
				q[node.getId()] = getStability(node);
			}
		}
		
		/*
		for(NodeCT node:  no.getPathToRoot()){
			if(q[node.getId()] != null && q[ ascendant[node.getId()].getId() ] != null && q[ descendants[node.getId()].getId() ] != null){
				double minStabilityDesc = q[ descendants[node.getId()].getId() ];
				double minStabilityAsc = q[ ascendant[node.getId()].getId() ];
				
				if(q[node.getId()] < minStabilityDesc && q[node.getId()] < minStabilityAsc){
					//System.out.println("***" + q[node.getId()]);
				}else{
					//System.out.println(q[node.getId()]);
				}
			}
		}
		*/
		
		return q;
	}
	
	
	public ColorImage getImageMSER(int delta){
		ColorImage img = ImageFactory.createColorImage(tree.getInputImage());
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
		ColorImage img = ImageFactory.createColorImage(tree.getInputImage());
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
		ComputerMserCT m = new ComputerMserCT(tree);
		int delta = 10;
		//WindowImages.show(m.getPointImageMSER(delta));
		
		WindowImages.show(m.getImageMSER(delta));
		
		 
		//m.imprimirScoreRamo();
	}
	
}
