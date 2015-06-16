package mmlib4j.representation.tree.pruningStrategy;

import mmlib4j.representation.tree.IMorphologicalTreeFiltering;
import mmlib4j.representation.tree.attribute.Attribute;
import mmlib4j.representation.tree.componentTree.ComponentTree;
import mmlib4j.representation.tree.componentTree.NodeCT;
import mmlib4j.representation.tree.tos.NodeToS;
import mmlib4j.representation.tree.tos.TreeOfShape;


/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 */
public class PruningBasedMSERWithTextLocation extends PruningBasedMSER{
	
	public PruningBasedMSERWithTextLocation(IMorphologicalTreeFiltering tree, int delta){
		super(tree, delta);
	}
	
	public boolean[] getMappingSelectedNodes() {		
		boolean mser[] = super.getMappingSelectedNodes();
		boolean mserTextLocation[] = new boolean[mser.length];
		if(tree instanceof ComponentTree){
			ComponentTree tree = (ComponentTree) this.tree;
			
			for(NodeCT node: tree.getNodesMap()){
				if(mser[node.getId()] && isNodeTextLocation( node )){
					mserTextLocation[node.getId()] = true;
				}
			}
			return mserTextLocation;
		}
		else if(tree instanceof TreeOfShape){
			TreeOfShape tree = (TreeOfShape) this.tree;
			for(NodeToS node: tree.getNodesMap()){
				if(mser[node.getId()] && isNodeTextLocation( node )){
					mserTextLocation[node.getId()] = true;
				}
			}
			return mserTextLocation;
		}
		else{
			return null;
		}
	}
	
	


	public boolean isNodeTextLocation(NodeCT node){
		int widthNode = (int)node.getAttribute(Attribute.WIDTH).getValue();
		int heightNode = (int)node.getAttribute(Attribute.HEIGHT).getValue();
		int areaBB = widthNode * heightNode;
		double ratioAreaBB = node.getArea() / (double) areaBB;
		double ratioWH = Math.max(widthNode, heightNode) / (double) Math.min(widthNode, heightNode);
		//int numHoles= node.getNumHoles();
		
		int psiArea = 0;
		if(50 <= node.getArea() && node.getArea() < 100000){
			psiArea = 1;
		}
		
		int psiHeight = 0;
		if(8 <= heightNode && heightNode < 500){
			psiHeight = 1;
		}
		
		int psiWidth = 0;
		if(4 <= widthNode && widthNode < 400){
			psiWidth = 1;
		}
		
		//int psiHole = 0;
		//if(numHoles <= 3){
		//	psiHole = 1;
		//}
		
		int psiRect = 0;
		if(0.2 <= ratioAreaBB && ratioAreaBB <= 0.95)
			psiRect = 1;
		
		int psiRate = 0;
		if(ratioWH <= 8){
			psiRate = 1;
		}
		
		int psiColor = 0;
		if(node.getAttributeValue(Attribute.VARIANCE_LEVEL) <= 30)
			psiColor = 1;
		
		return psiArea + psiRect + psiRate + psiColor + psiHeight + psiWidth == 6;
	}
	
	


	public boolean isNodeTextLocation(NodeToS node){
		double widthNode = node.getAttributeValue(Attribute.WIDTH);
		double heightNode = node.getAttributeValue(Attribute.HEIGHT);
		double areaBB = widthNode * heightNode;
		double ratioAreaBB = node.getArea() / areaBB;
		double ratioWH = Math.max(widthNode, heightNode) / Math.min(widthNode, heightNode);
		int numHoles= node.getNumHoles();
		
		int psiArea = 0;
		if(50 <= node.getArea() && node.getArea() < 100000){
			psiArea = 1;
		}
		
		int psiHeight = 0;
		if(8 <= heightNode && heightNode < 500){
			psiHeight = 1;
		}
		
		int psiWidth = 0;
		if(4 <= widthNode && widthNode < 300){
			psiWidth = 1;
		}
		
		int psiHole = 0;
		if(numHoles <= 5){
			psiHole = 1;
		}
		
		int psiRect = 0;
		if(0.2 <= ratioAreaBB && ratioAreaBB <= 0.99)
			psiRect = 1;
		
		int psiRate = 0;
		if(ratioWH <= 8){
			psiRate = 1;
		}
		
		int psiColor = 0;
		if(node.getAttributeValue(Attribute.VARIANCE_LEVEL) <= 30)
			psiColor = 1;
		

		
		//System.out.printf("%d %d %d %d %d\n", psiHeight, psiWidth, psiRect, psiRate, psiColor);
		return psiArea + psiRect + psiHole + psiRate + psiColor + psiHeight + psiWidth == 7;
	}
	
}
