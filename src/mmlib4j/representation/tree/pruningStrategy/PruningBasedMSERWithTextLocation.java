package mmlib4j.representation.tree.pruningStrategy;

import mmlib4j.representation.tree.MorphologicalTreeFiltering;
import mmlib4j.representation.tree.NodeLevelSets;
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
	
	public PruningBasedMSERWithTextLocation(MorphologicalTreeFiltering tree, int delta){
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
	
	


	public boolean isNodeTextLocation(NodeLevelSets node){
		
		if((100 <= node.getAttributeValue(Attribute.AREA) && node.getAttributeValue(Attribute.AREA) < 500000)
				&& (20 <= node.getAttributeValue(Attribute.HEIGHT) && node.getAttributeValue(Attribute.HEIGHT) < 400)
				&& (10 <= node.getAttributeValue(Attribute.WIDTH) && node.getAttributeValue(Attribute.WIDTH) < 400)
				&& (node.getAttributeValue(Attribute.NUM_HOLES) <= 3)
				&& (0.25 <= node.getAttributeValue(Attribute.RECTANGULARITY) && node.getAttributeValue(Attribute.RECTANGULARITY) <= 0.95)
				&& (node.getAttributeValue(Attribute.RATIO_WIDTH_HEIGHT) <= 10)
				&& (node.getAttributeValue(Attribute.VARIANCE_LEVEL) <= 40)
				//&& (node.getAttributeValue(Attribute.ALTITUDE) < 100) 
			)
			return true;
		else
			return false;
		
	}
	
	
	
}

