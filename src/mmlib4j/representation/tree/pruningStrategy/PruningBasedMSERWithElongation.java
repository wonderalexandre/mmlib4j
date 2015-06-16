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
public class PruningBasedMSERWithElongation extends PruningBasedMSER{

	
	private int delta;
	
	public PruningBasedMSERWithElongation(IMorphologicalTreeFiltering tree, int delta){
		super(tree, delta);
		this.delta = delta;
	}
	
	public boolean[] getMappingSelectedNodes2() {
		
		boolean mser[] = super.getMappingSelectedNodesRank();
		boolean mserElongation[] = new boolean[mser.length];
		if(tree instanceof ComponentTree){
			ComponentTree tree = (ComponentTree) this.tree;
			for(NodeCT node: tree.getNodesMap()){
				if(isNodeElongation( node ) || (mser[node.getId()] && node.getAttributeValue(Attribute.MOMENT_ELONGATION) < 0.7 && node.getArea() > 50)){
					mserElongation[node.getId()] = true;
				}
			}
			return mserElongation;
		}
		else if(tree instanceof TreeOfShape){
			TreeOfShape tree = (TreeOfShape) this.tree;
			for(NodeToS node: tree.getNodesMap()){
				if(mser[node.getId()] || isNodeElongation( node )){
					mserElongation[node.getId()] = true;
				}
			}
			return mserElongation;
		}
		else{
			return null;
		}
	}
	
	public boolean[] getMappingSelectedNodes() {
		if(delta > 0)
			return getMappingSelectedNodes2();
		boolean mser[] = new boolean[tree.getNumNode()];
		if(tree instanceof ComponentTree){
			ComponentTree tree = (ComponentTree) this.tree;
			for(NodeCT node: tree.getListNodes()){
				if(isNodeElongation( node )){
					mser[node.getId()] = true;
				}
			}
			return mser;
		}
		else if(tree instanceof TreeOfShape){
			TreeOfShape tree = (TreeOfShape) this.tree;
			for(NodeToS node: tree.getListNodes()){
				if(isNodeElongation( node )){
					mser[node.getId()] = true;
				}
			}
			return mser;
		}
		else{
			return null;
		}
	}
	


	public boolean isNodeElongation(NodeCT node){
		//System.out.println(node.moment.elongation());
		if(node.getAttributeValue(Attribute.MOMENT_ELONGATION) < 1 && node.getArea() > 1000 && node.getArea() < 23500){
			//WindowImages.show(node.createImageSC());
			return true;
		}
		else
			return false;
	}
	
	


	public boolean isNodeElongation(NodeToS node){
		if(node.getAttributeValue(Attribute.MOMENT_ELONGATION) < 0.15 && node.getArea() > 1000)
			return true;
		else
			return false;
	}
	
}
