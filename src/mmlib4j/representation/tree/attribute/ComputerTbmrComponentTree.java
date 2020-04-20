package mmlib4j.representation.tree.attribute;

import mmlib4j.representation.tree.NodeLevelSets;
import mmlib4j.representation.tree.componentTree.ComponentTree;


/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 */
public class ComputerTbmrComponentTree {

	private ComponentTree tree;
	private int num;
	
	public ComputerTbmrComponentTree(ComponentTree tree){
		this.tree = tree;
	}
	
	public boolean[] getSelectedNode(int tMin, int tMax){
		boolean result[] = new boolean[tree.getNumNode()];
		num = 0;
		int numChildren[] = new int[tree.getNumNode()];
		for(NodeLevelSets node: tree.getListNodes()){
			if(node.getAttributeValue(Attribute.AREA) >= tMin && node.getParent() != null)
				++numChildren[node.getParent().getId()];
		}
		for(NodeLevelSets node: tree.getListNodes()){
			if(node.getParent() != null && node.getAttributeValue(Attribute.AREA) < tMax && numChildren[node.getId()] == 1 && numChildren[node.getParent().getId()] >= 2){
				result[node.getId()] = true;
				num += 1;
			}
		}
		return result;
	}
	
	public int getNumTBMR(){
		return num;
	}
	
	
	
}
