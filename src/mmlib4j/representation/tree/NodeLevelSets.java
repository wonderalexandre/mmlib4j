package mmlib4j.representation.tree;

import java.util.HashMap;
import java.util.List;

import mmlib4j.datastruct.SimpleLinkedList;
import mmlib4j.images.BinaryImage;
import mmlib4j.representation.tree.attribute.Attribute;


/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 */
public interface NodeLevelSets {
	
	public NodeLevelSets getParent();
	public List getChildren();
	public int getLevel();
	public boolean isNodeMaxtree();
	public boolean isLeaf();
	public int getId();
	public SimpleLinkedList<Integer> getCanonicalPixels();
	
	public int getArea();
	public int getVolume();
	public int getCentroid();
	
	public int getXmin();
	public int getYmin();
	public int getXmax();
	public int getYmax();
	public void setXmin(int p);
	public void setYmin(int p);
	public void setXmax(int p);
	public void setYmax(int p);
	
	public void setPixelWithXmax(int p);
	public void setPixelWithYmax(int p);
	public void setPixelWithXmin(int p);
	public void setPixelWithYmin(int p);
	public int getPixelWithXmax();
	public int getPixelWithYmax();
	public int getPixelWithXmin();
	public int getPixelWithYmin();
	
	
	public int getNumPixelInFrame();
	public void addAttribute(int key, Attribute attr);
	public Attribute getAttribute(int key);
	public double getAttributeValue(int key);
	public HashMap<Integer, Attribute>  getAttributes();

	public BinaryImage createImage();
	
}
