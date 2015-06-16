package mmlib4j.representation.tree.pruningStrategy;

import java.awt.Color;

import mmlib4j.images.ColorImage;
import mmlib4j.images.impl.ImageFactory;
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
public class PruningBasedMSERForDIBCO extends PruningBasedMSER{
	
	
	
	public PruningBasedMSERForDIBCO(IMorphologicalTreeFiltering tree, int delta){
		super(tree, delta);
	}


	public boolean[] getMappingSelectedNodes() {
		boolean mser[] = super.getMappingSelectedNodesRank();
		boolean mserText[] = new boolean[mser.length];
		if(tree instanceof ComponentTree){
			ComponentTree tree = (ComponentTree) this.tree;
			for(NodeCT node: tree.getListNodes()){
				if(mser[node.getId()] && isNodeText( node ) ){
					mserText[node.getId()] = true;
				}
			}
			return mserText;
		}
		else if(tree instanceof TreeOfShape){
			TreeOfShape tree = (TreeOfShape) this.tree;
			for(NodeToS node: tree.getListNodes()){
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
		double bbArea = node.getArea() / (double)(node.getAttribute(Attribute.WIDTH).getValue() * node.getAttribute(Attribute.HEIGHT).getValue());
		double relationWH = Math.max(node.getAttribute(Attribute.WIDTH).getValue(), node.getAttribute(Attribute.HEIGHT).getValue()) / (double)Math.min(node.getAttribute(Attribute.WIDTH).getValue(), node.getAttribute(Attribute.HEIGHT).getValue()); 
		if(node.getAttribute(Attribute.HEIGHT).getValue() < tree.getInputImage().getHeight() * 0.55  
				&& 1 <= relationWH && relationWH <= 8 
				&& 15 <= node.getArea()   
				&& 5 <= node.getAttribute(Attribute.HEIGHT).getValue()
				&& node.getAttribute(Attribute.WIDTH).getValue() < 210
				&& 0.18 <= bbArea && bbArea <= 0.8
				//&& node.getCircularity() < 0.1
				){
			//System.out.println(node.moment.);
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
