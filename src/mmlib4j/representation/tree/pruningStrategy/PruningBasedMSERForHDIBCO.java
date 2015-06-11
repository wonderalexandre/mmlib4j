package mmlib4j.representation.tree.pruningStrategy;

import java.awt.Color;

import mmlib4j.images.ColorImage;
import mmlib4j.images.impl.ImageFactory;
import mmlib4j.representation.tree.IMorphologicalTreeFiltering;
import mmlib4j.representation.tree.componentTree.ComponentTree;
import mmlib4j.representation.tree.componentTree.NodeCT;
import mmlib4j.representation.tree.tos.NodeToS;
import mmlib4j.representation.tree.tos.TreeOfShape;


/**
 * MMorph4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 */
public class PruningBasedMSERForHDIBCO extends PruningBasedMSER{
	
	
	
	public PruningBasedMSERForHDIBCO(IMorphologicalTreeFiltering tree, int delta){
		super(tree, delta);
	}


	public boolean[] getMappingSelectedNodes() {
		boolean mser[] = super.getMappingSelectedNodes();
		boolean mserText[] = new boolean[mser.length];
		if(tree instanceof ComponentTree){
			ComponentTree tree = (ComponentTree) this.tree;
			for(NodeCT node: tree.getNodesMap()){
				if(mser[node.getId()] && isNodeText( node ) ){
					mserText[node.getId()] = true;
				}
			}
			return mserText;
		}
		else if(tree instanceof TreeOfShape){
			TreeOfShape tree = (TreeOfShape) this.tree;
			for(NodeToS node: tree.getNodesMap()){
				if(mser[node.getId()] && isNodeText( node )){
					mserText[node.getId()] = true;
				}
			}
			return mserText;
		}
		else{
			return null;
		}
	}
	
	
	public boolean isNodeText(NodeCT node){
		double bbArea = node.getArea() / (double)(node.getWidthNode() * node.getHeightNode());
		double relationWH = Math.max(node.getWidthNode(), node.getHeightNode()) / Math.min(node.getWidthNode(), node.getHeightNode()); 
		if(node.getHeightNode() < tree.getInputImage().getHeight() * 0.3 && 
				relationWH > 0.4 &&
				node.getArea() > 60 //&&
				&& node.getLevelMean() > 20
				//node.getCompacity() < 0.9
				//node.getHeightNode() > 5 //&&
				//bbArea > 0.2 && bbArea < 0.9  
				){
			//System.out.println(node.mser);
			return true;
		}
		else 
			return false;
	}
	
	public boolean isNodeText(NodeToS node){
		return true;
	}
	
	public int getNumOfPruning(){
		return num;
	}

	

	public ColorImage getImageMSER(boolean b[]){
		ColorImage img = ImageFactory.createColorImage(tree.getInputImage());
		if(tree instanceof ComponentTree){
			ComponentTree tree = (ComponentTree) this.tree;
			for(NodeCT node: tree.getListNodes()){
				if(b[node.getId()]){
					for(int p: node.getPixelsOfCC()){
						img.setPixel(p, Color.RED.getRGB());
					}
				}
			}
		}else if(tree instanceof TreeOfShape){
			TreeOfShape tree = (TreeOfShape) this.tree;
			for(NodeToS node: tree.getListNodes()){
				if(b[node.getId()]){
					for(int p: node.getPixelsOfCC()){
						img.setPixel(p, Color.RED.getRGB());
					}
				}
			}
		}
		
		return img;
	}
	
	
	
}
