package mmlib4j.representation.tree.tos;

import java.util.HashMap;
import java.util.Iterator;

import mmlib4j.datastruct.Queue;
import mmlib4j.datastruct.SimpleLinkedList;
import mmlib4j.filtering.binary.Contour;
import mmlib4j.images.BinaryImage;
import mmlib4j.images.GrayScaleImage;
import mmlib4j.images.impl.BitImage;
import mmlib4j.images.impl.MmlibImageFactory;
import mmlib4j.representation.tree.NodeLevelSets;
import mmlib4j.representation.tree.attribute.Attribute;


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
		xmin = ymin = Integer.MAX_VALUE;
		xmax = ymax = Integer.MIN_VALUE;
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
