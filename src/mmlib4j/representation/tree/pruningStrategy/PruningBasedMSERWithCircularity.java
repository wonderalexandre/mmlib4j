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
public class PruningBasedMSERWithCircularity extends PruningBasedMSER{
	
	public PruningBasedMSERWithCircularity(IMorphologicalTreeFiltering tree, int delta){
		super(tree, delta);
	}
	
	public boolean[] getMappingSelectedNodes2() {
		boolean mser[] = super.getMappingSelectedNodes();
		boolean mserCircle[] = new boolean[mser.length];
		if(tree instanceof ComponentTree){
			ComponentTree tree = (ComponentTree) this.tree;
			for(NodeCT node: tree.getNodesMap()){
				if(mser[node.getId()] && isNodeCircle( node )){
					mserCircle[node.getId()] = true;
				}
			}
			return mserCircle;
		}
		else if(tree instanceof TreeOfShape){
			TreeOfShape tree = (TreeOfShape) this.tree;
			for(NodeToS node: tree.getNodesMap()){
				if(mser[node.getId()] && isNodeCircle( node )){
					mserCircle[node.getId()] = true;
				}
			}
			return mserCircle;
		}
		else{
			return null;
		}
	}
	
	public boolean[] getMappingSelectedNodes() {
		boolean mser[] = new boolean[tree.getNumNode()];
		if(tree instanceof ComponentTree){
			ComponentTree tree = (ComponentTree) this.tree;
			for(NodeCT node: tree.getListNodes()){
				if(isNodeCircle( node )){
					mser[node.getId()] = true;
				}
			}
			return mser;
		}
		else if(tree instanceof TreeOfShape){
			TreeOfShape tree = (TreeOfShape) this.tree;
			for(NodeToS node: tree.getListNodes()){
				if(isNodeCircle( node )){
					mser[node.getId()] = true;
				}
			}
			return mser;
		}
		else{
			return null;
		}
	}
	


	public boolean isNodeCircle(NodeCT node){
		if(node.getAttributeValue(Attribute.PERIMETER_EXTERNAL) < 50)
			return false;
		else if (node.getArea() < 200)
			return false;
		else if(node.getAttributeValue(Attribute.CIRCULARITY) < 0.4)
			return false;
		else
			return true;
	}
	
	


	public boolean isNodeCircle(NodeToS node){
		if(node.getAttributeValue(Attribute.PERIMETER_EXTERNAL) < 50)
			return false;
		else if (node.getArea() < 200)
			return false;
		else if(node.getAttributeValue(Attribute.CIRCULARITY) < 0.4)
			return false;
		else
			return true;
	}
	
}
