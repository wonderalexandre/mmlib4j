package mmlib4j.representation.tree;

import java.util.Iterator;
import java.util.List;

import mmlib4j.datastruct.Queue;
import mmlib4j.datastruct.SimpleLinkedList;
import mmlib4j.images.BinaryImage;
import mmlib4j.images.GrayScaleImage;
import mmlib4j.images.impl.ImageFactory;
import mmlib4j.representation.tree.attribute.Attribute;
import mmlib4j.representation.tree.componentTree.NodeCT;


/**
 * MMorph4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 */
public interface INodeTree {
	
	public INodeTree getParent();
	public List getChildren();
	public int getLevel();
	public boolean isNodeMaxtree();
	public boolean isLeaf();
	public int getId();
	public SimpleLinkedList<Integer> getCanonicalPixels();
	
	public int getArea();
	public int getCentroid();
	public int getXmin();
	public int getYmin();
	public int getXmax();
	public int getYmax();
	public int getNumPixelInFrame();
	
	public void addAttribute(int key, Attribute attr);
	public Attribute getAttribute(int key);
	public double getAttributeValue(int key);
	
	
	


	public BinaryImage createImage();
	
}
