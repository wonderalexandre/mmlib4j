package mmlib4j.representation.tree.pruningStrategy;

import java.awt.Color;
import java.util.LinkedList;

import mmlib4j.images.ColorImage;
import mmlib4j.images.impl.ImageFactory;
import mmlib4j.representation.tree.InfoPrunedTree;
import mmlib4j.representation.tree.tos.NodeToS;
import mmlib4j.representation.tree.tos.TreeOfShape;



/**
 * MMorph4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 */
public class ComputerMserToS {
	
	private TreeOfShape tree;
	private NodeToS ascendant[];
	private LinkedList<NodeToS> descendants[];
	private int num;
	
	public ComputerMserToS(TreeOfShape tree){
		this.tree = tree;
		
	}
	
	private NodeToS getNodeAscendant(NodeToS node, int h){
		NodeToS n = node;
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
	
	private int sumAreaDescendants(NodeToS node){
		if(descendants[node.getId()] == null || descendants[node.getId()].isEmpty())
			return 0;
		
		int sum =0;
		for(NodeToS n: descendants[node.getId()]){
			sum += n.getArea();
		}
		return sum;
	}
	
	private double getMinimalStabilityOfDescendants(NodeToS node, Double q[]){
		double min = Double.MAX_VALUE;
		for(NodeToS n: descendants[node.getId()]){
			if(q[n.getId()] != null && q[n.getId()] < min){
				min = q[n.getId()];
			}
		}
		return min;
	}
	
	private double getStability(NodeToS node){
		return (ascendant[node.getId()].getArea() - sumAreaDescendants(node)) / (double) node.getArea(); 
	}
	
	public LinkedList<NodeToS> getNodesByMSER(int delta){
		
		LinkedList<NodeToS> list = new LinkedList<NodeToS>();
		ascendant = new NodeToS[tree.getNumNode()];
		descendants = new LinkedList[tree.getNumNode()];
		
		for(NodeToS node: tree.getListNodes()){
			NodeToS n = getNodeAscendant(node, delta);
			if(n != null){
				if(descendants[n.getId()] == null)
					descendants[n.getId()] = new LinkedList<NodeToS>();
				
				descendants[n.getId()].add(node);
				ascendant[node.getId()] = n;
			}
		}
		
		Double q[] = new Double[tree.getNumNode()];
		boolean mser[] = new boolean[tree.getNumNode()];
		for(NodeToS node: tree.getListNodes()){
			if(ascendant[node.getId()] != null && descendants[node.getId()] != null)
				q[node.getId()] = getStability(node);
		}
		
		for(NodeToS node: tree.getListNodes()){
			if(q[node.getId()] != null && q[ascendant[node.getId()].getId()] != null){
				double minStabilityDesc = getMinimalStabilityOfDescendants(node, q);
				if(minStabilityDesc == Double.MAX_VALUE) continue;
				
				double stability = q[ascendant[node.getId()].getId()];
				
				if(q[node.getId()] < minStabilityDesc && q[node.getId()] < stability){
					mser[node.getId()] = true;
					list.add(node);
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
		descendants = new LinkedList[tree.getNumNode()];
		boolean mser[] = new boolean[tree.getNumNode()];
		
		for(NodeToS node: tree.getListNodes()){
			NodeToS n = getNodeAscendant(node, delta);
			if(n != null){
				if(descendants[n.getId()] == null)
					descendants[n.getId()] = new LinkedList<NodeToS>();
				
				descendants[n.getId()].add(node);
				ascendant[node.getId()] = n;
			}
		}
		
		Double q[] = new Double[tree.getNumNode()];

		for(NodeToS node: tree.getListNodes()){
			if(ascendant[node.getId()] != null && descendants[node.getId()] != null)
				q[node.getId()] = getStability(node);
		}
		
		for(NodeToS node: tree.getListNodes()){
			if(q[node.getId()] != null && q[ascendant[node.getId()].getId()] != null){
				double minStabilityDesc = getMinimalStabilityOfDescendants(node, q);
				if(minStabilityDesc == Double.MAX_VALUE) continue;
				
				double stability = q[ascendant[node.getId()].getId()];
				
				if(q[node.getId()] < minStabilityDesc && q[node.getId()] < stability){
					mser[node.getId()] = true;
					num += 1;
				}
			}
		}
		return mser;
		
	}
	
	
	public boolean[] getMappingNodesByMSER(int delta, InfoPrunedTree prunedTree){
		ascendant = new NodeToS[tree.getNumNode()];
		descendants = new LinkedList[tree.getNumNode()];
		boolean mser[] = new boolean[tree.getNumNode()];
		
		for(NodeToS node: tree.getListNodes()){
			if(prunedTree.wasPruned(node))
				continue;
			
			NodeToS n = getNodeAscendant(node, delta);
			if(n != null){
				if(descendants[n.getId()] == null)
					descendants[n.getId()] = new LinkedList<NodeToS>();
				
				descendants[n.getId()].add(node);
				ascendant[node.getId()] = n;
			}
		}
		
		Double q[] = new Double[tree.getNumNode()];

		for(NodeToS node: tree.getListNodes()){
			if(prunedTree.wasPruned(node))
				continue;
			
			if(ascendant[node.getId()] != null && descendants[node.getId()] != null)
				q[node.getId()] = getStability(node);
		}
		
		for(NodeToS node: tree.getListNodes()){
			if(prunedTree.wasPruned(node))
				continue;
			
			
			if(q[node.getId()] != null && q[ascendant[node.getId()].getId()] != null){
				double minStabilityDesc = getMinimalStabilityOfDescendants(node, q);
				if(minStabilityDesc == Double.MAX_VALUE) continue;
				
				double stability = q[ascendant[node.getId()].getId()];
				
				if(q[node.getId()] < minStabilityDesc && q[node.getId()] < stability){
					mser[node.getId()] = true;
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
		ColorImage img = ImageFactory.createColorImage(tree.getInputImage());
		for(NodeToS node: getNodesByMSER(delta)){
			for(int p: node.getPixelsOfCC()){
				img.setPixel(p, Color.RED.getRGB());
			}
		}
		return img;
	}

	public ColorImage getPointImageMSER(int delta){
		ColorImage img = ImageFactory.createColorImage(tree.getInputImage());
		for(NodeToS node: getNodesByMSER(delta)){
			for(int p: node.getPixels()){
				img.setPixel(p, Color.RED.getRGB());
			}
		}
		return img;
	}
	
}
