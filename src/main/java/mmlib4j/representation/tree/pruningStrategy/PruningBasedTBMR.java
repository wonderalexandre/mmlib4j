package mmlib4j.representation.tree.pruningStrategy;

import mmlib4j.representation.tree.MorphologicalTreeFiltering;
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
public class PruningBasedTBMR implements MappingStrategyOfPruning{

	private MorphologicalTreeFiltering tree;
	private int tMin;
	private int tMax;
	private int num;
	
	public PruningBasedTBMR(MorphologicalTreeFiltering tree, int tmin, int tmax){
		this.tree = tree;
		this.tMin = tmin;
		this.tMax = tmax;
		tree.loadAttribute(Attribute.AREA);
	}
	
	
	public boolean[] getMappingSelectedNodes() {
		if(tree instanceof ComponentTree){
			ComponentTree tree = (ComponentTree) this.tree;
			boolean result[] = new boolean[tree.getNumNode()];
			num = 0;
			int numChildren[] = new int[tree.getNumNode()];
			for(NodeCT node: tree.getListNodes()){
				if(node.getArea() >= tMin && node.getParent() != null)
					++numChildren[node.getParent().getId()];
			}
			for(NodeCT node: tree.getListNodes()){
				if(node.getParent() != null && node.getArea() < tMax && numChildren[node.getId()] == 1 && numChildren[node.getParent().getId()] >= 2){
					result[node.getId()] = true;
					num += 1;
				}
			}
			return result;
		}
		else{
			
			TreeOfShape tree = (TreeOfShape) this.tree;
			boolean result[] = new boolean[tree.getNumNode()];
			num = 0;
			int numChildren[] = new int[tree.getNumNode()];
			for(NodeToS node: tree.getListNodes()){
				if(node.getArea() >= tMin && node.getParent() != null)
					++numChildren[node.getParent().getId()];
			}
			for(NodeToS node: tree.getListNodes()){
				if(node.getParent() != null && node.getArea() < tMax && numChildren[node.getId()] == 1 && numChildren[node.getParent().getId()] >= 2){
					result[node.getId()] = true;
					num += 1;
				}
			}
			return result;
		}
	}

	public int getNumOfPruning(){
		return num;
	}

}
