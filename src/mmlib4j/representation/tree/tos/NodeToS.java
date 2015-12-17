package mmlib4j.representation.tree.tos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import mmlib4j.datastruct.Queue;
import mmlib4j.datastruct.SimpleLinkedList;
import mmlib4j.filtering.binary.Contour;
import mmlib4j.images.BinaryImage;
import mmlib4j.images.GrayScaleImage;
import mmlib4j.images.impl.BitImage;
import mmlib4j.images.impl.ImageFactory;
import mmlib4j.representation.tree.NodeLevelSets;
import mmlib4j.representation.tree.attribute.Attribute;


/**
 * MMLib4J - Mathematical Morphology Library for Java 
 * @author Wonder Alexandre Luz Alves
 *
 */
public class NodeToS implements NodeLevelSets, Cloneable{
	int level;
	GrayScaleImage img;
	int canonicalPixel;
	int id;
	int heightNode;
	int numDescendent;
	int numDescendentLeaf;
	int numSiblings;
	
	int countPixelInFrame;
	boolean isNodeMaxtree;
	boolean isClone = false;
	NodeToS parent;
	List<NodeToS> children = new ArrayList<NodeToS>();
	SimpleLinkedList<Integer> pixels = new SimpleLinkedList<Integer>();
	
	
	//basic attribute node
	int xmin;
	int ymin;
	int xmax;
	int ymax;
	int sumX;
	int sumY;	
	int pixelXmax;
	int pixelXmin;
	int pixelYmin;
	int pixelYmax;
	int area;
	int volume;
	int countHoles;
	HashMap<Integer, Attribute> attributes = new HashMap<Integer, Attribute>();
	Contour contourE = null;
	

	public void addAttribute(int key, Attribute attr){
		attributes.put(key, attr);
	}
	public Attribute getAttribute(int key){
		return attributes.get(key);
	}
	
	public double getAttributeValue(int key){
		return attributes.get(key).getValue();
	}
	
	public boolean hasAttribute(int key){
		return attributes.containsKey(key);
	}
	
	public HashMap<Integer, Attribute>  getAttributes(){
		return attributes;
	}
	
	public NodeToS(int numCreate, int level, GrayScaleImage img, int canonicalPixel){
		this.id = numCreate;
		this.level = level; 
		this.img = img;
		this.canonicalPixel = canonicalPixel;
		xmin = ymin = Integer.MAX_VALUE;
		xmax = ymax = Integer.MIN_VALUE;
	}
	
	public int getId(){
		return id;
	}

	public NodeToS getParent(){
		return parent;
	}
	
	public List<NodeToS> getChildren(){
		return children;
	}
	
	public int getLevel(){
		return level;
	}
	
	public int getCanonicalPixel(){
		return canonicalPixel;
	}
	
	public boolean isLeaf(){
		return children.isEmpty();
	}
	
	public boolean isNodeMaxtree(){
		return isNodeMaxtree;
	}
	
	public int getXmin(){
		return xmin;
	}
	public int getYmin(){
		return ymin;
	}
	public int getXmax(){
		return xmax;
	}
	public int getYmax(){
		return ymax;
	}
	
	public int getNumHoles(){
		return countHoles;
	}
	public boolean isClone(){
		return isClone;
	}
	public NodeToS getClone(){
		try {
			NodeToS no = (NodeToS) this.clone();
			no.isClone = true;
			no.children = new ArrayList<NodeToS>();
			no.pixels = new SimpleLinkedList<Integer>();
			return no;
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public void addPixel(int p){		
		int x = p % img.getWidth();
		int y = p / img.getWidth();
		if(x < xmin){ 
			xmin = x;
			pixelXmin = p;
		}
		if(x > xmax) {
			xmax = x;
			pixelXmax = p;
		}
		if(y <= ymin) {
			if( y < ymin){
				ymin = y;
				pixelYmin = p;
			}
			else {
				if(x < pixelYmin % img.getWidth())
					pixelYmin = p;
			}
		}
		if(y > ymax){
			ymax = y;
			pixelYmax = p;
		}
		if(x == 0 || y == 0 || x == img.getWidth()-1 || y == img.getHeight()-1){
			countPixelInFrame++;
			if(x == 0 && y == 0)
				countPixelInFrame++;
			else if(x==0 && y == img.getHeight()-1)
				countPixelInFrame++;
			else if(x == img.getWidth()-1 && y == 0)
				countPixelInFrame++;
			else if(x == img.getWidth()-1 && y == img.getHeight()-1)
				countPixelInFrame++;
		}
		
		sumX += x;
		sumY += y;
		area += 1;
		volume += (level);
		pixels.add(p);
	}
	
	public int getNumPixelInFrame(){
		return countPixelInFrame;
	}
	
	public SimpleLinkedList<Integer> getCanonicalPixels(){
		return pixels;
	}

	public int getNumChildren(){
		return children.size();
	}
	
	public int getArea(){
		return area;
	}
	public int getVolume(){
		return volume;
	}
	
	public int getCentroid(){
		int xc = (sumX / getArea());
		int yc = (sumY / getArea());
		return xc + yc * img.getWidth();     
	}
	
	public Iterable<Integer> getPixelsOfCC(){
		final Queue<NodeToS> fifo = new Queue<NodeToS>();
		fifo.enqueue(this);
		
		return new Iterable<Integer>() {
			public Iterator<Integer> iterator() {
				return new Iterator<Integer>() {
					private Iterator<Integer> pixelNode = nextNode(); 
					public boolean hasNext() {
						if(pixelNode == null)
							pixelNode = nextNode();
						
						boolean b = pixelNode.hasNext();
						if(!b){
							pixelNode = nextNode();
							if(pixelNode == null)
								return false;
							else
								b = pixelNode.hasNext();
						}
						
						return b;
					}
					public Integer next() {
						return pixelNode.next();
					}
					public void remove() {}
					private Iterator<Integer> nextNode(){
						if(!fifo.isEmpty()){
							NodeToS no = fifo.dequeue();
							for(NodeToS son: no.children){
								fifo.enqueue(son);
							}
							return pixelNode = no.pixels.iterator();
						}
						return null;
					}
				};
			}
		};
		
	}
	


	public Iterable<NodeToS> getNodesDescendants(){
		final Queue<NodeToS> fifo = new Queue<NodeToS>();
		fifo.enqueue(this);
		
		return new Iterable<NodeToS>() {
			public Iterator<NodeToS> iterator() {
				return new Iterator<NodeToS>() {
					private NodeToS currentNode = nextNode(); 
					public boolean hasNext() {
						return currentNode != null;
					}
					public NodeToS next() {
						NodeToS tmp = currentNode;
						currentNode = nextNode(); 
						return tmp;
					}
					public void remove() {}
					private NodeToS nextNode(){
						if(!fifo.isEmpty()){
							NodeToS no = fifo.dequeue();
							for(NodeToS son: no.children){
								fifo.enqueue(son);
							}
							return no;
						}
						return null;
					}
				};
			}
		};
		
	}
	

	public boolean isComparable(NodeToS node){
		return isDescendant(node) || isAncestral(node);
	}
	
	public boolean isDescendant(NodeToS node){
		return node.isAncestral(this);
	}
	
	public boolean isAncestral(NodeToS nodeAncestral){
		NodeToS tmp = this;
		while(tmp != null){
			if(tmp == nodeAncestral){
				return true;
			}
			tmp = tmp.parent;
		}
		return  false;
	}
	
	
	public GrayScaleImage createImageSC(int levelNotPixelCanonical){
		GrayScaleImage imgOut = ImageFactory.createGrayScaleImage(img.getDepth(), img.getWidth(), img.getHeight());;
		Queue<NodeToS> fifo = new Queue<NodeToS>();
		fifo.enqueue(this);
		while(!fifo.isEmpty()){
			NodeToS no = fifo.dequeue();
			
			if(no.children != null){
				for(NodeToS son: no.children){
					fifo.enqueue(son);
				}
			}
			
			for(Integer p: no.pixels){
				if(img.getPixel(p) == level)
					imgOut.setPixel(p, 255);
				else
					imgOut.setPixel(p, levelNotPixelCanonical);
			}
			
		}
		return imgOut;
	}
	

	public GrayScaleImage createImageSC(){
		return createImageSC(level);
	}
	
	
	
	public BinaryImage createImage(){
		BinaryImage imgOut = new BitImage(img.getWidth(), img.getHeight());
		Queue<NodeToS> fifo = new Queue<NodeToS>();
		fifo.enqueue(this);
		while(!fifo.isEmpty()){
			NodeToS no = fifo.dequeue();
			
			if(no.children != null){
				for(NodeToS son: no.children){
					fifo.enqueue(son);
				}
			}
			
			for(Integer p: no.pixels){
				imgOut.setPixel(p, true);
			}
			
		}
		return imgOut;
	}
	
	public int hashCode(){
		return id;
	}
	

	public Iterable<NodeToS> getPathToRoot(){
		final NodeToS node = this;
		return new Iterable<NodeToS>() {
			public Iterator<NodeToS> iterator() {
				return new Iterator<NodeToS>() {
					NodeToS nodeRef = node;
					public boolean hasNext() {
						return nodeRef != null;
					}

					public NodeToS next() {
						NodeToS n = nodeRef;
						nodeRef = nodeRef.parent;
						return n;
					}

					public void remove() { }
					
				};
			}
		};
	}
	

	public void setXmin(int p) {
		xmin = p;
	}

	public void setYmin(int p) {
		ymin = p;
	}

	public void setXmax(int p) {
		xmax = p;
	}

	public void setYmax(int p) {
		ymax = p;
	}

	public void setPixelWithXmax(int p) {
		pixelXmax = p;
	}

	public void setPixelWithYmax(int p) {
		pixelYmax = p;
	}

	public void setPixelWithXmin(int p) {
		pixelXmin = p;
	}

	public void setPixelWithYmin(int p) {
		pixelYmin = p;
	}


	public int getPixelWithXmax() {
		return pixelXmax ;
	}

	public int getPixelWithYmax() {
		return pixelYmax;
	}

	public int getPixelWithXmin() {
		return pixelXmin;
	}

	public int getPixelWithYmin() {
		return pixelYmin;
	}
	
}
