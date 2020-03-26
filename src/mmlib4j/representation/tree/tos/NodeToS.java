package mmlib4j.representation.tree.tos;

import mmlib4j.datastruct.SimpleLinkedList;
import mmlib4j.images.GrayScaleImage;
import mmlib4j.representation.tree.NodeLevelSets;


/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 */
public class NodeToS extends NodeLevelSets implements Cloneable{
	
	//basic attribute node
	int countHoles;
	

	public NodeToS(int numCreate, int level, GrayScaleImage img, int canonicalPixel){
		this.id = numCreate;
		this.level = level; 
		this.img = img;
		this.canonicalPixel = canonicalPixel;
	}
	
	
	public int getNumHoles(){
		return countHoles;
	}
	
	
	public NodeLevelSets getClone(){
		try {
			NodeToS no = (NodeToS) this.clone();
			no.isClone = true;
			no.children = new SimpleLinkedList<NodeLevelSets>();
			no.pixels = new SimpleLinkedList<Integer>();
			return no;
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
}
