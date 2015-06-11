package mmlib4j.representation.tree.pruningStrategy;

import mmlib4j.representation.tree.componentTree.ComponentTree;
import mmlib4j.representation.tree.componentTree.NodeCT;





/**
 * MMorph4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 */
public class ComputerTbmrCT {

	private ComponentTree tree;
	private int num;
	
	public ComputerTbmrCT(ComponentTree tree){
		this.tree = tree;
	}
	
	public boolean[] getSelectedNode(int tMin, int tMax){
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
	
	public int getNumTBMR(){
		return num;
	}
	
	
	
}
